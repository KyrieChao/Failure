package com.chao.failfast.exception;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.util.I18n;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Batch business exception - Collects all errors in non-FailFast mode.
 *
 * @author Kyrie Chao
 * @version 1.3.0
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
    private final boolean truncated;
    private final int totalErrors;

    /**
     * Constructor.
     *
     * @param errors List of business exceptions
     */
    public MultiBusiness(List<Business> errors) {
        this(errors, false);
    }

    public MultiBusiness(List<Business> errors, boolean truncatedByChain) {
        super(ResponseCode.of(
                        FailureConst.SYSTEM_CODE, FailureConst.MULTIPLE_VALIDATION_ERRORS, buildDetail(errors.size(), truncatedByChain)
                ), buildDetail(errors.size(), truncatedByChain),
                null, null, errors.isEmpty() ? HttpStatus.INTERNAL_SERVER_ERROR : errors.get(0).getHttpStatus(), null, null
        );
        this.totalErrors = errors.size();
        this.truncated = truncatedByChain || errors.size() > MAX_ERRORS;
        if (this.truncated) {
            this.errors = List.copyOf(errors.subList(0, Math.min(errors.size(), MAX_ERRORS)));
        } else {
            this.errors = List.copyOf(errors);
        }
    }

    private static String buildDetail(int size, boolean truncatedByChain) {
        if (truncatedByChain || size > MAX_ERRORS) {
            return FailureConst.TOO_MANY_ERRORS;
        }
        return I18n.get(FailureConst.MULTIPLE_VALIDATION_ERRORS_COUNT, size);
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
