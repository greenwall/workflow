package com.nordea.dompap.boot;

import com.nordea.dompap.workflow.*;
import com.nordea.dompap.workflow.boot.ThreadPoolWorkflowExecutor;
import com.nordea.dompap.workflow.boot.WorkflowContentInitializer;
import com.nordea.dompap.workflow.config.WorkflowConfig;
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
    WorkflowManager workFlowManager;

    @Autowired
    WorkflowConfig workFlowConfig;

    @Autowired
    WorkflowService workFlowService;

    @Test
    public void testStartEngine() throws InterruptedException, ResourceException, IOException {

        workFlowManager.start(new WorkflowBuilder<>().workflow(new BootTestWorkFlow("0")).methodName("stepA"));
        workFlowManager.start(new WorkflowBuilder<>().workflow(new BootTestWorkFlow("1")).methodName("stepA"));
        workFlowManager.start(new WorkflowBuilder<>().workflow(new BootTestWorkFlow("2")).methodName("stepA"));

        ThreadPoolWorkflowExecutor executor = new ThreadPoolWorkflowExecutor(workFlowManager, workFlowConfig);
        executor.startWorkFlowEngine();

        Thread.sleep(1000*3);

        workFlowManager.start(new WorkflowBuilder<>().workflow(new BootTestWorkFlow("3")).methodName("stepA"));
        workFlowManager.start(new WorkflowBuilder<>().workflow(new BootTestWorkFlow("4")).methodName("stepA"));
        workFlowManager.start(new WorkflowBuilder<>().workflow(new BootTestWorkFlow("5")).methodName("stepA"));
        workFlowManager.start(new WorkflowBuilder<>().workflow(new BootTestWorkFlow("6")).methodName("stepA"));
        workFlowManager.start(new WorkflowBuilder<>().workflow(new BootTestWorkFlow("7")).methodName("stepA"));

        Thread.sleep(1000*3);

        WorkflowSearch search = new WorkflowSearch();
        WorkflowSearchResult result = workFlowService.searchWorkFlows(search, 0, 10, false);
        assertEquals(8, result.totalWorkflows);
        assertEquals(8, result.workflows.size());
        for (Workflow w: result.workflows) {
            assertEquals("!stepC", w.getMethodName());
        }

    }

    @Test
    public void testStartEngineWithInitializer() throws InterruptedException, ResourceException, IOException {

        workFlowManager.start(new WorkflowBuilder<>().workflow(new BootTestWorkFlow("0")).methodName("stepA1"));

        ThreadPoolWorkflowExecutor executor = new ThreadPoolWorkflowExecutor(workFlowManager, workFlowConfig);
        executor.addWorkFlowInitializer(BootTestWorkFlow.class.getName(), (WorkflowContentInitializer<BootTestWorkFlow>) content -> content.x = "injected");
        executor.startWorkFlowEngine();

        Thread.sleep(1000*3);

        WorkflowSearch search = new WorkflowSearch();
        WorkflowSearchResult result = workFlowService.searchWorkFlows(search, 0, 10, false);
        assertEquals(1, result.totalWorkflows);
        assertEquals(1, result.workflows.size());
        for (Workflow w: result.workflows) {
            assertEquals("!stepC", w.getMethodName());
        }

    }

}
