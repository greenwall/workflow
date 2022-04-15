package com.nordea.dompap.spring;

import com.nordea.dompap.config.WorkFlowContextSpring;
import com.nordea.dompap.workflow.WorkFlowManager;
import com.nordea.dompap.workflow.config.WorkFlowConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = WorkFlowContextSpring.class)
//@SpringBootTest(classes = {WorkFlowContextSpring.class, WorkFlowConfigSpring.class, WorkFlowDataSourceConfiguration.class})
//@SpringBootTest(classes = {WorkFlowContextSpringTest.class, WorkFlowContextSpring.class, WorkFlowConfigSpring.class, WorkFlowDataSourceConfiguration.class})
//@SpringBootTest(classes = {WorkFlowContextSpringTest.class, WorkFlowContextSpring.class})
//@ConfigurationPropertiesScan
@ActiveProfiles("springtest")
public class WorkFlowContextSpringTest {

    @Autowired
    private WorkFlowContextSpring workFlowContext;

    @Autowired
    private WorkFlowManager workFlowManager;
/*
    @Test
    public void givenInScopeComponents_whenSearchingInApplicationContext_thenFindThem() throws SQLException {
        WorkFlowContext context = workFlowContext;
        assertNotNull(context);

        assertNotNull(context.getWorkFlowConfig());

        assertNotNull(context.getDataSource());
        assertNotNull(context.getDataSource().getConnection());

        assertNotNull(context.getWorkFlowManager());
        assertNotNull(context.getWorkFlowService());
        assertNotNull(context.getMetadataService());
        assertNotNull(context.getWorkFlowSelector());
        assertNotNull(context.getWorkFlowStatusService());
    }
*/
    @Test
    public void testBeans() {
        assertNotNull(workFlowManager);
    }

    @Test
    public void testContextInjected() {
        assertNotNull(workFlowContext);
    }

    @Test
    public void testWorkFlowConfig() {
        WorkFlowConfig config = workFlowContext.getWorkFlowConfig();
        assertEquals("application_springtest", config.getSchedule());

        assertTrue(Arrays.asList(config.getJobs()).contains("job2"));
        assertTrue(Arrays.asList(config.getJobs()).contains("job1"));

        assertFalse(config.isSelectorSkipLocked());
    }

}
