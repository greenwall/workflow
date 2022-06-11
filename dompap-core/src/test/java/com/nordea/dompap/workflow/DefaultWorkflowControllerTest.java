package com.nordea.dompap.workflow;

import com.nordea.dompap.workflow.config.WorkflowConfig;
import com.nordea.next.dompap.domain.BranchId;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@SpringBootTest(classes = com.nordea.dompap.config.WorkFlowContextSpring.class)
@ActiveProfiles("retrytest")
public class DefaultWorkflowControllerTest {

	@Autowired
	private WorkflowConfig workFlowConfig;
	@Autowired
	private WorkflowManager workFlowManager;

	// TODO Fix
	private DefaultWorkflowController defaultWorkflowController; // = new DefaultWorkFlowController(null, null);

	private final Method retryMethod = WorkflowUtil.getMethod(getClass(), "retryMethod");
	private final Method methodA = WorkflowUtil.getMethod(getClass(), "methodA");
	private final Method methodB = WorkflowUtil.getMethod(getClass(), "methodB");
	private final Method methodC = WorkflowUtil.getMethod(getClass(), "methodC");
	private final Workflow<DefaultWorkflowControllerTest> workflow = new Workflow<>(null, null, getClass().getName(), null, (BranchId)null, null, null);

	private final Method failingMethod = WorkflowUtil.getMethod(getClass(), "failingMethod");
	private final Method failingMethod2 = WorkflowUtil.getMethod(getClass(), "failingMethod2");
	
    @BeforeEach
	public void init() {
		defaultWorkflowController = new DefaultWorkflowController(workFlowConfig, workFlowManager);
		workflow.setContent(this);
	}

    public Method failingMethod() {
		return null; 
	}

    public Method failingMethod2() {
		return null; 
	}

    public Method retryMethod() {
		return null; 
	}
	
	public Method methodA() {
		return null; 
	}

	public Method methodB() {
		return null; 
	}
	
	public Method methodC() {
		return null; 
	}

	@Test
	public void testRetryOnExceptionWithMessage() {
//    	Assertions.assertNotNull(context);
    	Assertions.assertNotNull(workFlowConfig);
		//DefaultWorkFlowController.TestWorkFlow.doB.retryMinutes=1,2,3,4
		Assertions.assertEquals("1,2,3,4", workFlowConfig.getControllerConfig("DefaultWorkflowController.TestWorkFlow.doB.retryMinutes"));
    	System.out.println(workFlowConfig);

		//DefaultWorkFlowController.onFail.DefaultWorkFlowControllerTest.failingMethod(RuntimeException).resumeAt=some-message:retryMethod
		Assertions.assertEquals(retryMethod, defaultWorkflowController.getOnFail(workflow, failingMethod, new RuntimeException("some-message"), failingMethod));

		//Assert.assertEquals(methodC, DefaultWorkFlowController.getOnFail(workflow, failingMethod, new RuntimeException("some-other-message"), failingMethod));
	}

	@Test
	public void testRetryOnException() {
		//DefaultWorkFlowController.onFail.DefaultWorkFlowControllerTest.failingMethod(IllegalArgumentException).resumeAt=methodA
		assertEquals(methodA, defaultWorkflowController.getOnFail(workflow, failingMethod, new IllegalArgumentException(), failingMethod));
	}

	@Ignore
	public void testRetryOnMessage() {
		//DefaultWorkFlowController.onFail.DefaultWorkFlowControllerTest.failingMethod.resumeAt=message\=42:methodB
		assertEquals(methodB, defaultWorkflowController.getOnFail(workflow, failingMethod, new ResourceException("message=42"), failingMethod));
	}
	
	@Test
	public void testDisableRetryOnExceptionWithMessage() {
		//DefaultWorkFlowController.onFail.DefaultWorkFlowControllerTest.failingMethod(NullPointerException).resumeAt=message2:
		assertEquals(null, defaultWorkflowController.getOnFail(workflow, failingMethod, new NullPointerException("message2"), failingMethod));
	}
	
	@Test
	public void testDisableRetryOnException() {
		//DefaultWorkFlowController.onFail.DefaultWorkFlowControllerTest.failingMethod(NoSuchMethodException).resumeAt=
		assertNull(defaultWorkflowController.getOnFail(workflow, failingMethod, new NoSuchMethodException(), failingMethod));
	}

	@Ignore
	public void testDisableRetryOnMessage() {
		//DefaultWorkFlowController.onFail.DefaultWorkFlowControllerTest.failingMethod.resumeAt=message\=42:
		assertNull(defaultWorkflowController.getOnFail(workflow, failingMethod, new ResourceException("message=42"), failingMethod));
	}
	
    @Test
    public void testMultipleMatching() {
        //DefaultWorkFlowController.onFail.DefaultWorkFlowControllerTest.failingMethod.resumeAt=message\=42:
        assertEquals(methodA, defaultWorkflowController.getOnFail(workflow, failingMethod, new NotSupportedException("errorCode=6001"), failingMethod));
        assertEquals(methodB, defaultWorkflowController.getOnFail(workflow, failingMethod, new NotSupportedException("errorCode=6003"), failingMethod));
        assertEquals(methodC, defaultWorkflowController.getOnFail(workflow, failingMethod, new NotSupportedException("errorCode=6005"), failingMethod));
    }
    
	@Test
	public void testRetryOnAnyException() {
		//DefaultWorkFlowController.onFail.DefaultWorkFlowControllerTest.failingMethod.resumeAt=methodC
		assertEquals(methodC, defaultWorkflowController.getOnFail(workflow, failingMethod, new SQLException(), failingMethod));
	}

	@Test
	public void testRetryOnAnyExceptionAnyWorkflow() {
		//DefaultWorkFlowController.onFail.failingMethod.resumeAt=methodC
		assertEquals(methodA, defaultWorkflowController.getOnFail(workflow, failingMethod2, new SQLException(), failingMethod2));
	}
	
	@Test
	public void testRetryOnSpecificExceptionAnyWorkflow() {
		//DefaultWorkFlowController.onFail.failingMethod.resumeAt=methodC
		assertEquals(methodB, defaultWorkflowController.getOnFail(workflow, failingMethod2, new IOException(), failingMethod2));
	}
	//Assert.assertEquals(methodB, DefaultWorkFlowController.getOnFail(workflow, failingMethod, new NoSuchAlgorithmException(), failingMethod));
	@Test
	public void test_property_name_with_workflowName_subType_method() throws ResourceException {
        Workflow workflow = new Workflow(UUID.randomUUID(), "key", "com.nordea.branchchannel.dpap.workflow.TestWorkFlow","mylocal", null, (BranchId)null, new Date(), null);
        workflow.setMethodName("doB");
        assertEquals("1,2,3", defaultWorkflowController.getRetryValue(workflow));
	}
	@Test
    public void test_property_name_with_workflowName_method() throws ResourceException {
        Workflow workflow = new Workflow(UUID.randomUUID(), "key", "com.nordea.branchchannel.dpap.workflow.TestWorkFlow", null, (BranchId)null, new Date(), null);
        workflow.setMethodName("doB");
        assertEquals("1,2,3,4", defaultWorkflowController.getRetryValue(workflow));
    }
	@Test
    public void test_property_name_with_workflowName_subType() throws ResourceException {
        Workflow workflow = new Workflow(UUID.randomUUID(), "key", "com.nordea.branchchannel.dpap.workflow.TestWorkFlow","mylocal", null, (BranchId)null, new Date(), null);
        assertEquals("1,2,3,4,5", defaultWorkflowController.getRetryValue(workflow));
    }
	@Test
    public void test_property_name_with_workflowName() throws ResourceException {
        Workflow workflow = new Workflow(UUID.randomUUID(), "key", "com.nordea.branchchannel.dpap.workflow.TestWorkFlow", null, (BranchId)null, new Date(), null);
        assertEquals("1,2,3,4,5,6", defaultWorkflowController.getRetryValue(workflow));
    }
	@Test
    public void test_property_name_with_default_property() throws ResourceException {
        Workflow workflow = new Workflow(UUID.randomUUID(), "key", "com.nordea.branchchannel.dpap.workflow.TestWorkFlow1", null, (BranchId)null, new Date(), null);
        assertEquals("1,5,30,180,720", defaultWorkflowController.getRetryValue(workflow));
    }
}
