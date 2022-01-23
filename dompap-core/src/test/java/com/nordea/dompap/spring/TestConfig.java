package com.nordea.dompap.spring;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Must have private property and getters/setters for spring configuration injection to work.
 */
@Data
@ConfigurationProperties("test")
public class TestConfig {
    private String foo;

    private NestedConfig nested;

    @Data
    public static class NestedConfig {
        private String prop;
    }
}
