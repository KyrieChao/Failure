package com.chao.failure.config.mapping;

import com.chao.failure.constant.FailureConst;
import com.chao.failure.config.properties.FailureProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Error code mapping configuration - Support configurable HTTP status mapping.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
@Component
@Slf4j
public class CodeMappingConfig {

    private final FailureProperties properties;
    private final Map<Integer, HttpStatus> DEFAULT_MAPPINGS;
    private final Map<String, List<CodeRange>> groupRanges = new HashMap<>();

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
        parseGroupRanges();
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
     * Parse code group ranges.
     */
    private void parseGroupRanges() {
        // Get group information for code mapping from properties
        var groups = properties.getCodeMapping().getGroups();
        // If group information is empty, return directly
        if (groups == null) return;
        // Iterate through each group entry
        for (var entry : groups.entrySet()) {
            String groupName = entry.getKey(); // Get group name
            List<Object> rawList = entry.getValue(); // Get raw range list
            // Create code range list
            List<CodeRange> ranges = new ArrayList<>();
            // Iterate through each element in the raw range list
            for (Object raw : rawList) {
                // If element is a number type, create a single-value code range
                if (raw instanceof Number num) {
                    int code = num.intValue();
                    ranges.add(new CodeRange(code, code));
                } else if (raw instanceof String str) {
                    CodeRange range = parseRange(str);
                    if (range != null) {
                        ranges.add(range);
                    } else {
                        try {
                            int code = Integer.parseInt(str.trim());
                            ranges.add(new CodeRange(code, code));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
            groupRanges.put(groupName, ranges);
        }
    }

    /**
     * Parse code range string and convert to CodeRange object.
     *
     * @param input Code range string input, e.g., "1-5" or "5-1"
     * @return Parsed CodeRange object, return null if format is incorrect
     */
    private CodeRange parseRange(String input) {
        // Use regular expression pattern to match input string
        Matcher matcher = FailureConst.Range.matcher(input.trim());
        if (matcher.matches()) {
            // Parse start line number, use first capture group if not null, otherwise use third capture group
            int start = matcher.group(1) != null
                    ? Integer.parseInt(matcher.group(1))
                    : Integer.parseInt(matcher.group(3));
            // Parse end line number, use second capture group if not null, otherwise use fourth capture group
            int end = matcher.group(2) != null
                    ? Integer.parseInt(matcher.group(2))
                    : Integer.parseInt(matcher.group(4));

            // Create CodeRange object, ensure smaller value as start line number, larger value as end line number
            return new CodeRange(Math.min(start, end), Math.max(start, end));
        }
        // If input format doesn't match, return null
        return null;
    }

    /**
     * Determine if error code belongs to specified group (support range + exact value).
     *
     * @param code Error code
     * @param groupName Group name
     * @return True if in group, false otherwise
     */
    public boolean isInGroup(int code, String groupName) {
        List<CodeRange> ranges = groupRanges.get(groupName);
        if (ranges == null || ranges.isEmpty()) return false;

        for (CodeRange r : ranges) {
            if (code >= r.start && code <= r.end) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all error codes for specified group (only exact values, range expansion not returned).
     *
     * @param groupName Group name
     * @return List of error codes
     */
    public List<Integer> getGroupCodes(String groupName) {
        return properties.getCodeMapping().getGroups()
                .getOrDefault(groupName, Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(v -> v instanceof Integer i ? i : null)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Get all error codes for group (expanded list).
     *
     * @param groupName Group name
     * @return Expanded string representation of group codes
     */
    public String getGroupCodesExpanded(String groupName) {
        return getGroupCodesExpanded(groupName, 5);
    }

    /**
     * Get expanded group code list.
     *
     * @param groupName Group name
     * @param n Maximum number of items to display
     * @return Formatted string representation of code list
     */
    public String getGroupCodesExpanded(String groupName, int n) {
        List<CodeRange> ranges = groupRanges.get(groupName);
        if (ranges == null || ranges.isEmpty() || n <= 0) {
            return "[]";
        }

        Set<Integer> expanded = new TreeSet<>();
        for (CodeRange r : ranges) {
            for (int i = r.start(); i <= r.end(); i++) {
                expanded.add(i);
            }
        }

        if (expanded.size() <= n) {
            return expanded.toString();
        }

        List<Integer> list = new ArrayList<>(expanded);
        String middle = list.subList(1, list.size() - 1).stream()
                .limit(3)
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        return String.format("[%s, %s, ..., %s]", list.get(0), middle, list.get(list.size() - 1));
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

    private record CodeRange(int start, int end) {
    }
}
