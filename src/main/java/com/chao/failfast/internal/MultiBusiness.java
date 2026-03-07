package com.chao.failfast.internal;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.core.ResponseCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Batch business exception - Collects all errors in non-FailFast mode.
 *
 * @author Kyrie Chao
 * @version 1.0.0
 */
@Getter
public class MultiBusiness extends Business {
    /**
     * Default maximum error count limit.
     */
    private static final int MAX_ERRORS = 50;

    /**
     * Collected list of business exceptions.
     */
    private final List<Business> errors;

    /**
     * Constructor.
     *
     * @param errors List of business exceptions
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
     * Override toString method to provide formatted batch error output.
     *
     * @return Formatted error message string
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
