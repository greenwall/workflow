package com.nordea.dompap.boot;

import com.nordea.dompap.workflow.TestWithMemoryDB;
import com.nordea.dompap.workflow.WorkFlowBuilder;
import com.nordea.dompap.workflow.WorkFlowManager;
import com.nordea.dompap.workflow.boot.ThreadPoolWorkFlowExecutor;
import com.nordea.dompap.workflow.config.WorkFlowConfig;
import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.UserId;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.resource.ResourceException;
import java.io.IOException;

@SpringBootTest(classes = com.nordea.dompap.config.WorkFlowContextSpring.class)
@ActiveProfiles("executortest")
public class WorkFlowExecutorTest extends TestWithMemoryDB {

    UserId userId = new UserId("Test2");
    BranchId branchId = new BranchId("0000");

    @Autowired
    WorkFlowManager workFlowManager;

    @Autowired
    WorkFlowConfig workFlowConfig;

    @Test
    public void testStartEngine() throws InterruptedException, ResourceException, IOException {

        WorkFlowBuilder builder = new WorkFlowBuilder();
        builder.workflow(new BootTestWorkFlow("X"))
                    .externalKey("x")
                    .methodName("stepA");

        workFlowManager.start(builder);

        ThreadPoolWorkFlowExecutor executor = new ThreadPoolWorkFlowExecutor(workFlowManager, workFlowConfig);
        executor.startWorkFlowEngine();

        Thread.sleep(1000*60);
    }
}
