package com.chao.failfast.internal;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.core.ResponseCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * 批量业务异常 - 非FailFast模式下收集的所有错误
 * 用于在严格模式下收集多个验证错误，统一抛出
 */
@Getter
public class MultiBusiness extends Business {
    /**
     * 默认最大错误数量限制，防止内存溢出
     */
    private static final int MAX_ERRORS = 50;

    /**
     * 收集的业务异常列表
     */
    private final List<Business> errors;

    /**
     * 构造函数
     * 根据错误数量创建适当地响应信息
     *
     * @param errors 业务异常列表
     */
    public MultiBusiness(List<Business> errors) {
        super(ResponseCode.of(
                        FailureConst.SYSTEM_CODE, FailureConst.MULTIPLE_VALIDATION_ERRORS, errors.size() > MAX_ERRORS ?
                                FailureConst.TOO_MANY_ERRORS : FailureConst.VALIDATION_ERROR_PREFIX + errors.size() + FailureConst.ERROR_ITEM_SUFFIX
                ), FailureConst.VALIDATION_ERROR_PREFIX + errors.size() + FailureConst.ERROR_ITEM_SUFFIX,
                null, null, HttpStatus.INTERNAL_SERVER_ERROR,null
        );

        // 限制错误数量，防止内存问题
        if (errors.size() > MAX_ERRORS) {
            this.errors = List.copyOf(errors.subList(0, MAX_ERRORS));
        } else {
            this.errors = List.copyOf(errors);
        }
    }

    /**
     * 重写toString方法，提供格式化的批量错误输出
     *
     * @return 格式化的错误信息字符串
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Multi={\n");
        for (int i = 0; i < errors.size(); i++) {
            Business ex = errors.get(i);
            sb.append("  ").append(i + 1).append(". ").append(ex.toString());
            if (i < errors.size() - 1) sb.append(",\n");
            else sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
