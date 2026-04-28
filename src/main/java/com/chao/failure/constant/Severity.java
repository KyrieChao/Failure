package com.chao.failure.constant;

import com.chao.failure.util.I18n;
import lombok.Getter;

import java.util.Locale;

@Getter
public enum Severity {
    DEBUG("{error.severity.debug}", false, false, 1),
    INFO("{error.severity.info}", false, true, 2),
    WARNING("{error.severity.warning}", true, true, 3),
    ERROR("{error.severity.error}", true, true, 4),
    CRITICAL("{error.severity.critical}", true, true, 5);

    private final String labelKey;
    private final boolean fillStackTrace;
    private final boolean logRequired;
    private final int weight;

    Severity(String labelKey, boolean fillStackTrace, boolean logRequired, int weight) {
        this.labelKey = labelKey;
        this.fillStackTrace = fillStackTrace;
        this.logRequired = logRequired;
        this.weight = weight;
    }

    public String getLabel() {
        return I18n.get(labelKey);
    }

    public static Severity from(String raw, Severity fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Severity.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}
