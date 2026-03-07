package com.chao.failfast.config;

import com.chao.failfast.constant.FailureConst;
import com.chao.failfast.internal.core.FailureProperties;
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
 * @version 1.0.0
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
        // 将传入的properties赋值给实例变量
        this.properties = properties;
        // 创建一个临时的HashMap用于存储状态码映射
        Map<Integer, HttpStatus> temp = new HashMap<>();
        // 初始化默认的状态码映射
        initializeDefaultMappings(temp);
        // 加载自定义的状态码映射
        loadCustomMappings(temp);
        // 将不可修改的映射赋值给DEFAULT_MAPPINGS实例变量
        this.DEFAULT_MAPPINGS = Collections.unmodifiableMap(temp);
        // 解析组范围配置
        parseGroupRanges();
    }

    /**
     * Initialize default HTTP status code mappings.
     *
     * @param map Map collection used to store error code and HTTP status code mappings
     */
    private void initializeDefaultMappings(Map<Integer, HttpStatus> map) {
        // 4xx 客户端错误状态码
        map.put(40000, HttpStatus.BAD_REQUEST);        // 40000: 请求错误
        map.put(40100, HttpStatus.UNAUTHORIZED);       // 40100: 未授权
        map.put(40300, HttpStatus.FORBIDDEN);          // 40300: 禁止访问
        map.put(40400, HttpStatus.NOT_FOUND);          // 40400: 资源不存在
        map.put(40500, HttpStatus.METHOD_NOT_ALLOWED); // 40500: 方法不允许
        map.put(40800, HttpStatus.REQUEST_TIMEOUT);    // 40800: 请求超时
        map.put(40900, HttpStatus.CONFLICT);           // 40900: 冲突
        map.put(41000, HttpStatus.GONE);               // 41000: 资源已消失
        map.put(41300, HttpStatus.PAYLOAD_TOO_LARGE);  // 41300: 负载过大
        map.put(41500, HttpStatus.UNSUPPORTED_MEDIA_TYPE); // 41500: 不支持的媒体类型
        map.put(42200, HttpStatus.UNPROCESSABLE_ENTITY);   // 42200: 无法处理的实体
        map.put(42900, HttpStatus.TOO_MANY_REQUESTS);      // 42900: 请求过多
        // 5xx 服务器错误状态码
        map.put(50000, HttpStatus.INTERNAL_SERVER_ERROR); // 50000: 内部服务器错误
        map.put(50100, HttpStatus.NOT_IMPLEMENTED);       // 50100: 未实现
        map.put(50200, HttpStatus.BAD_GATEWAY);           // 50200: 网关错误
        map.put(50300, HttpStatus.SERVICE_UNAVAILABLE);   // 50300: 服务不可用
        map.put(50400, HttpStatus.GATEWAY_TIMEOUT);       // 50400: 网关超时
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
                map.put(code, HttpStatus.valueOf(status));
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
        // 从属性中获取代码映射的组信息
        var groups = properties.getCodeMapping().getGroups();
        // 如果组信息为空，则直接返回
        if (groups == null) return;
        // 遍历每个组条目
        for (var entry : groups.entrySet()) {
            String groupName = entry.getKey(); // 获取组名称
            List<Object> rawList = entry.getValue(); // 获取原始范围列表
            // 创建代码范围列表
            List<CodeRange> ranges = new ArrayList<>();
            // 遍历原始范围列表中的每个元素
            for (Object raw : rawList) {
                // 如果元素是数字类型，则创建单值代码范围
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
        // 使用正则表达式模式匹配输入字符串
        Matcher matcher = FailureConst.Range.matcher(input.trim());
        if (matcher.matches()) {
            // 解析起始行号，如果第一个捕获组不为null则使用它，否则使用第三个捕获组
            int start = matcher.group(1) != null
                    ? Integer.parseInt(matcher.group(1))
                    : Integer.parseInt(matcher.group(3));
            // 解析结束行号，如果第二个捕获组不为null则使用它，否则使用第四个捕获组
            int end = matcher.group(2) != null
                    ? Integer.parseInt(matcher.group(2))
                    : Integer.parseInt(matcher.group(4));

            // 创建CodeRange对象，确保较小的值作为起始行号，较大的值作为结束行号
            return new CodeRange(Math.min(start, end), Math.max(start, end));
        }
        // 如果输入格式不匹配，返回null
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
        // 1. 标准 HTTP 状态码优先精确匹配（100-599）
        if (code >= 100 && code <= 599) {
            try {
                return HttpStatus.valueOf(code);
            } catch (IllegalArgumentException ignored) {
                // 非标准 HTTP 码（如 499），继续后续匹配
            }
        }
        // 2. 精确匹配自定义映射
        HttpStatus exact = DEFAULT_MAPPINGS.get(code);
        if (exact != null) return exact;

        // 3. 范围匹配（如 40001~40099 匹配 40000）
        int rangeStart = (code / 100) * 100;
        HttpStatus rangeStatus = DEFAULT_MAPPINGS.get(rangeStart);
        if (rangeStatus != null) return rangeStatus;

        // 4. 大类匹配（业务码 4xxxx/5xxxx）
        if (code >= 40000 && code < 50000) return HttpStatus.BAD_REQUEST;
        // 5. 兜底
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }


    private record CodeRange(int start, int end) {
    }
}
