package com.nordea.dompap.boot;

import com.nordea.dompap.workflow.*;
import com.nordea.dompap.workflow.boot.ThreadPoolWorkFlowExecutor;
import com.nordea.dompap.workflow.config.WorkFlowConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.resource.ResourceException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = com.nordea.dompap.config.WorkFlowContextSpring.class)
@ActiveProfiles("executortest")
public class WorkFlowExecutorTest extends TestWithMemoryDB {

    @Autowired
    WorkFlowManager workFlowManager;

    @Autowired
    WorkFlowConfig workFlowConfig;

    @Autowired
    WorkFlowService workFlowService;

    @Test
    public void testStartEngine() throws InterruptedException, ResourceException, IOException {

        workFlowManager.start(new WorkFlowBuilder<>().workflow(new BootTestWorkFlow("0")).methodName("stepA"));
        workFlowManager.start(new WorkFlowBuilder<>().workflow(new BootTestWorkFlow("1")).methodName("stepA"));
        workFlowManager.start(new WorkFlowBuilder<>().workflow(new BootTestWorkFlow("2")).methodName("stepA"));

        ThreadPoolWorkFlowExecutor executor = new ThreadPoolWorkFlowExecutor(workFlowManager, workFlowConfig);
        executor.startWorkFlowEngine();

        Thread.sleep(1000*3);

        workFlowManager.start(new WorkFlowBuilder<>().workflow(new BootTestWorkFlow("3")).methodName("stepA"));
        workFlowManager.start(new WorkFlowBuilder<>().workflow(new BootTestWorkFlow("4")).methodName("stepA"));
        workFlowManager.start(new WorkFlowBuilder<>().workflow(new BootTestWorkFlow("5")).methodName("stepA"));
        workFlowManager.start(new WorkFlowBuilder<>().workflow(new BootTestWorkFlow("6")).methodName("stepA"));
        workFlowManager.start(new WorkFlowBuilder<>().workflow(new BootTestWorkFlow("7")).methodName("stepA"));

        Thread.sleep(1000*3);

        WorkFlowSearch search = new WorkFlowSearch();
        WorkFlowSearchResult result = workFlowService.searchWorkFlows(search, 0, 10, false);
        assertEquals(8, result.totalWorkflows);
        assertEquals(8, result.workflows.size());
        for (WorkFlow w: result.workflows) {
            assertEquals("!stepC", w.getMethodName());
        }

    }
}
