package com.chao.failure.config.mapping;

import com.chao.failure.config.properties.FailureProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Error code mapping configuration - Support configurable HTTP status mapping.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
@Component
@Slf4j
public class CodeMappingConfig {

    private final FailureProperties properties;
    private final Map<Integer, HttpStatus> DEFAULT_MAPPINGS;

    /**
     * Constructor to initialize CodeMappingConfig instance.
     *
     * @param properties Configuration properties containing failure fast settings
     */
    public CodeMappingConfig(FailureProperties properties) {
        this.properties = properties;
        Map<Integer, HttpStatus> temp = new HashMap<>();
        initializeDefaultMappings(temp);
        loadCustomMappings(temp);
        this.DEFAULT_MAPPINGS = Collections.unmodifiableMap(temp);
    }

    /**
     * Initialize default HTTP status code mappings.
     *
     * @param map Map collection used to store error code and HTTP status code mappings
     */
    private void initializeDefaultMappings(Map<Integer, HttpStatus> map) {
        // 4xx client error status codes
        map.put(40000, HttpStatus.BAD_REQUEST);        // 40000: Bad request
        map.put(40100, HttpStatus.UNAUTHORIZED);       // 40100: Unauthorized
        map.put(40300, HttpStatus.FORBIDDEN);          // 40300: Forbidden
        map.put(40400, HttpStatus.NOT_FOUND);          // 40400: Not found
        map.put(40500, HttpStatus.METHOD_NOT_ALLOWED); // 40500: Method not allowed
        map.put(40800, HttpStatus.REQUEST_TIMEOUT);    // 40800: Request timeout
        map.put(40900, HttpStatus.CONFLICT);           // 40900: Conflict
        map.put(41000, HttpStatus.GONE);               // 41000: Gone
        map.put(41300, HttpStatus.PAYLOAD_TOO_LARGE);  // 41300: Payload too large
        map.put(41500, HttpStatus.UNSUPPORTED_MEDIA_TYPE); // 41500: Unsupported media type
        map.put(42200, HttpStatus.UNPROCESSABLE_ENTITY);   // 42200: Unprocessable entity
        map.put(42900, HttpStatus.TOO_MANY_REQUESTS);      // 42900: Too many requests
        // 5xx server error status codes
        map.put(50000, HttpStatus.INTERNAL_SERVER_ERROR); // 50000: Internal server error
        map.put(50100, HttpStatus.NOT_IMPLEMENTED);       // 50100: Not implemented
        map.put(50200, HttpStatus.BAD_GATEWAY);           // 50200: Bad gateway
        map.put(50300, HttpStatus.SERVICE_UNAVAILABLE);   // 50300: Service unavailable
        map.put(50400, HttpStatus.GATEWAY_TIMEOUT);       // 50400: Gateway timeout
    }

    /**
     * Load custom status code mappings.
     *
     * @param map Map collection used to store status code mappings
     */
    private void loadCustomMappings(Map<Integer, HttpStatus> map) {
        properties.getCodeMapping().getHttpStatus().forEach((key, status) -> {
            try {
                int code = Integer.parseInt(key.trim());
                HttpStatus resolved = resolveHttpStatusEnum(status);
                if (resolved == null) {
                    throw new IllegalArgumentException();
                }
                map.put(code, resolved);
            } catch (NumberFormatException e) {
                log.warn("Invalid business code '{}', must be integer", key);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid HTTP status code {} for business code {}", status, key);
            }
        });
    }



    /**
     * Resolve HTTP status corresponding to error code.
     *
     * @param code Business error code
     * @return Corresponding HttpStatus object
     */
    public HttpStatus resolveHttpStatus(int code) {
        if (code >= 100 && code <= 599) {
            HttpStatus status = resolveHttpStatusEnum(code);
            if (status != null) {
                return status;
            }
        }
        HttpStatus exact = DEFAULT_MAPPINGS.get(code);
        if (exact != null) return exact;
        int rangeStart = (code / 100) * 100;
        HttpStatus rangeStatus = DEFAULT_MAPPINGS.get(rangeStart);
        if (rangeStatus != null) return rangeStatus;
        if (code >= 40000 && code < 50000) return HttpStatus.BAD_REQUEST;

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private static HttpStatus resolveHttpStatusEnum(int code) {
        for (HttpStatus s : HttpStatus.values()) {
            if (s.value() == code) {
                return s;
            }
        }
        return null;
    }
}
