package com.nordea.dompap.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {SpringTest.class, TestBean.class})
@ConfigurationPropertiesScan
@ActiveProfiles("springtest")
public class SpringTest {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TestConfig testConfig;

    @Autowired
    private TestBean bean;

    @Test
    public void givenInScopeComponents_whenSearchingInApplicationContext_thenFindThem() {
        TestConfig config = applicationContext.getBean(TestConfig.class);
        assertNotNull(config);
        assertThat(config.getFoo()).isEqualTo("bar33");
    }

    @Test
    public void testContextInjected() {
        assertNotNull(testConfig);
        assertThat(testConfig.getFoo()).isEqualTo("bar33");
    }

    @Test
    public void testConfig() {
        TestConfig config = applicationContext.getBean(TestConfig.class);
        assertThat(config.getFoo()).isEqualTo("bar33");
    }

    @Test
    public void testConfigNested() {
        TestConfig config = applicationContext.getBean(TestConfig.class);
        assertThat(config.getNested().getProp()).isEqualTo("nestedProp");
    }

    @Test
    public void testBean() {
        assertNotNull(bean);
        assertNotNull(bean.getConfig());
        assertThat(bean.getConfig().getFoo()).isEqualTo("bar33");
    }

}
