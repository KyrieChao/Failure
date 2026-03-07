package com.chao.failfast.internal.chain;

import com.chao.failfast.annotation.FastValidator.ValidationContext;
import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.core.ResponseCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 验证链核心 - 管理状态（failFast, alive, errors, context）
 */
public abstract class ChainCore<S extends ChainCore<S>> {
    @Getter
    protected final boolean failFast;
    @Getter
    protected boolean alive = true;
    // 动态跳过状态 (true=执行, false=跳过)
    private boolean conditionState = true;
    // OR 状态
    private boolean orMode = false;
    private boolean orHasSuccess = false;
    protected final ValidationContext context;
    protected final List<Business> errors = new ArrayList<>();

    protected ChainCore(boolean failFast, ValidationContext context) {
        this.failFast = failFast;
        this.context = context;
    }

    /**
     * 动态控制是否执行后续校验
     * 当 condition 为 false 时，后续的 check/or 等操作将被跳过，直到再次调用 when(true)
     */
    public S when(boolean condition) {
        this.conditionState = condition;
        return self();
    }

    public S or() {
        if (!conditionState) return self();
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

    public boolean shouldSkip() {
        // 如果当前被动态跳过，或 context 停止，则跳过
        if (!conditionState) return true;
        if (context != null && context.isStopped()) return true;
        return (!alive && failFast);
    }

    @SuppressWarnings("unchecked")
    protected S self() {
        return (S) this;
    }

    /**
     * 延迟计算校验 - 支持 Supplier
     */
    public S check(Supplier<Boolean> conditionSupplier, ResponseCode code, String detail) {
        if (shouldSkip()) return self();
        return check(conditionSupplier.get(), code, detail);
    }

    /**
     * 统一校验入口 - 支持配置
     */
    public S check(boolean condition, ResponseCode code, String detail) {
        return check(condition, code, detail, null);
    }

    /**
     * 统一校验入口 - 支持配置和值快照
     */
    public S check(boolean condition, ResponseCode code, String detail, Object value) {
        if (shouldSkip()) return self();

        if (orMode) {
            // or模式：计算组合结果
            orMode = false;  // 消费掉or状态
            boolean finalSuccess = orHasSuccess || condition;

            if (!finalSuccess) {
                // 左右都失败，报错
                addError(code, detail, value);
                if (failFast) alive = false;
            } else {
                // 有一个成功，整个or通过，清除错误
                alive = true;
                // errors已经在or()时清空了
            }
        } else {
            // 普通模式
            if (!condition) {
                addError(code, detail, value);
                if (failFast) alive = false;
            }
        }
        return self();
    }

    /**
     * 无配置校验 - 使用默认错误
     */
    public S check(boolean condition) {
        return check(condition, null, null);
    }

    private void addError(ResponseCode code, String detail, Object value) {
        Business business = buildBusiness(code, detail, value);

        if (context != null) {
            context.reportError(business);
            if (failFast) context.stop();
        } else {
            errors.add(business);
        }
    }

    private Business buildBusiness(ResponseCode code, String detail, Object value) {
        Business.Fabricator fabricator = Business.compose().invalidValue(value);
        if (code != null && detail != null) {
            return fabricator.responseCode(code).detail(detail).materialize();
        }
        return fabricator.responseCode(Objects.requireNonNullElse(code, ResponseCode.VALIDATION_ERROR_500_DYNAMIC)).materialize();
    }

    /**
     * 获取核心实例（供接口默认方法使用）
     */
    public S core() {
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
        if (context != null) {
            return context.isValid() && alive;
        }
        return errors.isEmpty() && alive;
    }

    /**
     * 如果当前有错误，则停止后续校验（将 conditionState 设为 false）
     * 用于 strict 模式下防止 NPE（需配合 defer 使用）
     */
    public S stopOnFail() {
        if (!conditionState) return self();
        if (isValid()) return self();
        return when(false);
    }

    /**
     * 恢复校验（将 conditionState 设为 true）
     */
    public S resume() {
        return when(true);
    }
}
