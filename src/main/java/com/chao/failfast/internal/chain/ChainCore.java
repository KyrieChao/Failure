package com.chao.failfast.internal.chain;

import com.chao.failfast.annotation.FastValidator.ValidationContext;
import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.internal.core.ViolationSpec;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 验证链核心 - 管理状态（failFast, alive, errors, context）
 */
public abstract class ChainCore<S extends ChainCore<S>> {
    @Getter
    protected final boolean failFast;
    @Getter
    protected boolean alive = true;
    // OR 状态
    private boolean orMode = false;
    private boolean orHasSuccess = false;
    protected final ValidationContext context;
    protected final List<Business> errors = new ArrayList<>();

    protected ChainCore(boolean failFast, ValidationContext context) {
        this.failFast = failFast;
        this.context = context;
    }

    protected S or() {
        if (context != null && context.isStopped()) return self();
        this.orMode = true;
        this.orHasSuccess = isValid();   // 如果当前已通过，标记成功

        // 如果是failFast且已经失败，or给了第二次机会，重置alive
        if (failFast && !alive) {
            alive = true;
        }
        // 清空当前错误，准备收集or右侧的错误
        if (!orHasSuccess) {
            errors.clear();
        }
        return self();
    }

    protected boolean shouldSkip() {
        if (context != null && context.isStopped()) return true;
        return (!alive && failFast);
    }

    @SuppressWarnings("unchecked")
    protected S self() {
        return (S) this;
    }

    /**
     * 统一校验入口 - 支持配置
     */
    protected S check(boolean condition, Consumer<ViolationSpec> configurer) {
        if (shouldSkip()) return self();

        if (orMode) {
            // or模式：计算组合结果
            orMode = false;  // 消费掉or状态
            boolean finalSuccess = orHasSuccess || condition;

            if (!finalSuccess) {
                // 左右都失败，报错
                ViolationSpec spec = new ViolationSpec();
                configurer.accept(spec);
                addError(spec);
                if (failFast) alive = false;
            } else {
                // 有一个成功，整个or通过，清除错误
                alive = true;
                // errors已经在or()时清空了
            }
        } else {
            // 普通模式
            if (!condition) {
                ViolationSpec spec = new ViolationSpec();
                configurer.accept(spec);
                addError(spec);
                if (failFast) alive = false;
            }
        }
        return self();
    }

    /**
     * 无配置校验 - 使用默认错误
     */
    protected S check(boolean condition) {
        return check(condition, FailureConst.NO_OP);
    }

    private void addError(ViolationSpec spec) {
        Business business = buildBusiness(spec);

        if (context != null) {
            context.reportError(business);
            if (failFast) context.stop();
        } else {
            errors.add(business);
        }
    }

    private Business buildBusiness(ViolationSpec spec) {
        if (spec.hasFabricator()) {
            Business.Fabricator fab = Business.compose();
            spec.getFabricator().accept(fab);
            return fab.materialize();
        }
        if (spec.getCode() != null && spec.getDetail() != null) {
            return Business.of(spec.getCode(), spec.getDetail());
        }
        if (spec.getCode() != null) {
            return Business.of(spec.getCode());
        }
        return Business.of(ResponseCode.VALIDATION_ERROR_500_DYNAMIC);
    }

    /**
     * 获取核心实例（供接口默认方法使用）
     */
    protected S core() {
        return self();
    }

    /**
     * 获取业务原因列表
     * 该方法返回一个新的ArrayList，包含所有的错误信息
     *
     * @return 返回一个Business类型的列表，包含所有错误信息
     */
    public List<Business> getCauses() {
        return new ArrayList<>(errors);
    }

    /**
     * 检查当前对象是否有效的公共方法
     *
     * @return 如果错误集合为空且对象处于活跃状态则返回true，否则返回false
     */
    public boolean isValid() {
        return errors.isEmpty() && alive;
    }
}