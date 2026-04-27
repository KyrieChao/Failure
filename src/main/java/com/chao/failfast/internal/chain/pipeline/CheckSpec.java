package com.chao.failfast.internal.chain.pipeline;

import com.chao.failfast.internal.core.ResponseCode;

/**
 * Check specification record class, used to represent check result information
 * Contains three properties: response code, detailed information, and invalid value
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public record CheckSpec(ResponseCode code, String detail, Object invalidValue) {
    /**
     * Create a check specification instance without invalid value
     * @param code Response code indicating check result status
     * @param detail Detailed information describing check result specifics
     * @return Returns a new CheckSpec instance with null invalidValue
     */
    public static CheckSpec of(ResponseCode code, String detail) {
        return new CheckSpec(code, detail, null);
    }

    /**
     * Create a check specification instance with invalid value
     * @param code Response code indicating check result status
     * @param detail Detailed information describing check result specifics
     * @param invalidValue Invalid value that caused check failure
     * @return Returns a new CheckSpec instance containing the specified invalidValue
     */
    public static CheckSpec of(ResponseCode code, String detail, Object invalidValue) {
        return new CheckSpec(code, detail, invalidValue);
    }
}

