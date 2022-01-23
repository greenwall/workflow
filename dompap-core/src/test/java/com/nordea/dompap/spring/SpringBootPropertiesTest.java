package com.nordea.dompap.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(classes = {SpringBootPropertiesTest.class})
@ConfigurationPropertiesScan
@ActiveProfiles("foo")
//@ConfigurationProperties
//@Configuration
//@SpringBootTest(classes = {SpringBootPropertiesTest.class, TestConfig.class})
//@SpringBootTest(classes = {SpringBootPropertiesTest.class, TestConfig.class}, properties = {"foo=bar3","testConfig.foo=bar3"})
//@Import(TestConfig.class)
//@TestPropertySource("classpath:application-foo.properties")
public class SpringBootPropertiesTest {

    @Value("${foo}")
    String foo;

    @Autowired
    TestConfig testConfig;

    @Test
    void test(){
        assertThat(foo).isEqualTo("bar31");
    }

    @Test
    void test2(){
        assertThat(testConfig.getFoo()).isEqualTo("bar3");
    }

    @Test
    void test3(){
        assertNotNull(testConfig.getFoo());
    }

}
