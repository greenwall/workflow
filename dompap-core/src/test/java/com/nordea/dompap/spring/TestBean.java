package com.nordea.dompap.spring;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestBean {
    @Getter
    private final TestConfig config;
}
