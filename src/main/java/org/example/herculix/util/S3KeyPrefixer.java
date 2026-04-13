package org.example.herculix.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class S3KeyPrefixer {

    @Value("${aws.s3.prefix:}")
    private String prefix;

    public String applyPrefix(String key) {
        if (key == null || key.isBlank()) {
            return key;
        }

        String normalizedPrefix = prefix != null ? prefix.trim() : "";
        if (normalizedPrefix.isEmpty()) {
            return normalizeKey(key);
        }

        normalizedPrefix = stripSlashes(normalizedPrefix);
        String normalizedKey = normalizeKey(key);

        if (normalizedKey.startsWith(normalizedPrefix + "/")) {
            return normalizedKey;
        }

        return normalizedPrefix + "/" + normalizedKey;
    }

    private String normalizeKey(String key) {
        String normalized = key.trim();
        return normalized.startsWith("/") ? normalized.substring(1) : normalized;
    }

    private String stripSlashes(String value) {
        String result = value;
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
