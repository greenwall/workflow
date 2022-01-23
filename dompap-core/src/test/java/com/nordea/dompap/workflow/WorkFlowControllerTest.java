package com.nordea.dompap.workflow;

import com.nordea.dompap.workflow.config.WorkFlowConfig;
import com.nordea.dompap.workflow.config.WorkFlowContext;
import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.UserId;
import org.apache.commons.lang.reflect.MethodUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.resource.ResourceException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = com.nordea.dompap.config.WorkFlowContextSpring.class)
@ActiveProfiles("retrytest_local")
public class WorkFlowControllerTest extends TestWithMemoryDB {

	@Autowired
	WorkFlowConfig workFlowConfig;
	@Autowired
	WorkFlowManager workFlowManager;

    @SuppressWarnings("unused")
	public void methodA() {    	
    }

    @SuppressWarnings("unused")
	public void methodB() {    	
    }
    
	@SuppressWarnings("unchecked")
    @Test
	public void testGiveUp() throws ResourceException {
		WorkFlowManager workflowManagerMock = Mockito.mock(WorkFlowManager.class);
	    DefaultWorkFlowController ctrl = new DefaultWorkFlowController(workFlowConfig, workflowManagerMock);

        WorkFlow<?> workflow = new WorkFlow<>(UUID.randomUUID(), "key", "no-class", null, (BranchId)null, new Date(), null);
        Method methodExecuted = MethodUtils.getAccessibleMethod(this.getClass(), "methodA", new Class[] {});

        ctrl.onFail(workflow, methodExecuted, new NullPointerException());
        assertEquals(1, ctrl.retries);

        ctrl.onFail(workflow, methodExecuted, new NullPointerException());
        assertEquals(2, ctrl.retries);

        ctrl.onFail(workflow, methodExecuted, new NullPointerException());
        assertEquals(3, ctrl.retries);

        ctrl.onFail(workflow, methodExecuted, new NullPointerException());
        assertEquals(4, ctrl.retries);

        ctrl.onFail(workflow, methodExecuted, new NullPointerException());
        assertEquals(5, ctrl.retries);

//        verify(alertManager, times(5)).retry(eq(workflow), any(Method.class), anyString());
        verify(workflowManagerMock, times(5)).retryAt(eq(workflow), any(Method.class), any(Date.class), any(Throwable.class));
//        verify(alertManager, times(0)).fail(eq(workflow), any(Method.class), anyString());

        ctrl.onFail(workflow, methodExecuted, new NullPointerException());
        if(workflow.getContent()!=null){
//            verify(alertManager, times(1)).fail(eq(workflow), eq(methodExecuted), anyString());
        }else{
//            verify(alertManager, times(0)).fail(eq(workflow), eq(methodExecuted), anyString());
        }
	}

	@Test
	public void testGiveUp_workflow_method() throws ResourceException {
		WorkFlowManager workflowManager = Mockito.mock(WorkFlowManager.class);
		DefaultWorkFlowController ctrl = new DefaultWorkFlowController(workFlowConfig, workflowManager);

		WorkFlow<TestWorkFlow> workflow = new WorkFlow<>(UUID.randomUUID(), "key", "com.nordea.branchchannel.dpap.workflow.TestWorkFlow", null, (BranchId) null, new Date(), null);
		workflow.setMethodName("doB");
		Method methodExecuted = MethodUtils.getAccessibleMethod(TestWorkFlow.class, "doB", new Class[]{});
		Method otherMethod = MethodUtils.getAccessibleMethod(TestWorkFlow.class, "doX", new Class[]{});
		ctrl.onFail(workflow, methodExecuted, new NullPointerException());
		assertEquals(1, ctrl.retries);

		ctrl.onFail(workflow, methodExecuted, new NullPointerException());
		assertEquals(2, ctrl.retries);

		ctrl.onFail(workflow, methodExecuted, new NullPointerException());
		assertEquals(3, ctrl.retries);

		ctrl.onFail(workflow, otherMethod, new NullPointerException());
		assertEquals(1, ctrl.retries);


//	        verify(alertManager, times(3)).retry(eq(workflow), any(Method.class), anyString());
		verify(workflowManager, times(3)).retryAt(eq(workflow), eq(methodExecuted), any(Date.class), any(Throwable.class));
//        verify(alertManager, times(0)).fail(eq(workflow), any(Method.class), anyString());

		ctrl.onFail(workflow, methodExecuted, new NullPointerException());
/*
	        if(workflow.getContent()!=null){
	            verify(alertManager, times(1)).fail(eq(workflow), eq(methodExecuted), anyString());
	        }else{
	            verify(alertManager, times(0)).fail(eq(workflow), eq(methodExecuted), anyString());
	        }
*/
	}

	 @Test
     public void testGiveUp_workflow_method_with_subType() throws ResourceException {
		 WorkFlowManager workflowManagerMock = Mockito.mock(WorkFlowManager.class);
         DefaultWorkFlowController ctrl = new DefaultWorkFlowController(workFlowConfig, workflowManagerMock);

         WorkFlow<TestWorkFlow> workflow = new WorkFlow<>(UUID.randomUUID(), "key", "com.nordea.branchchannel.dpap.workflow.TestWorkFlow", "mylocal", null, (BranchId) null, new Date(), null);
         workflow.setMethodName("doB");
         Method methodExecuted = WorkFlowUtil.getMethod(TestWorkFlow.class, "doB");
         Method otherMethod = WorkFlowUtil.getMethod(TestWorkFlow.class, "doX");
         ctrl.onFail(workflow, methodExecuted, new NullPointerException());
         assertEquals(1, ctrl.retries);

         ctrl.onFail(workflow, methodExecuted, new NullPointerException());
         assertEquals(2, ctrl.retries);

         ctrl.onFail(workflow, methodExecuted, new NullPointerException());
         assertEquals(2, ctrl.retries);
         
         ctrl.onFail(workflow, methodExecuted, new NullPointerException());
         assertEquals(2, ctrl.retries);
         
         ctrl.onFail(workflow, otherMethod, new NullPointerException());
         assertEquals(1, ctrl.retries);


//         verify(alertManager, times(3)).retry(eq(workflow), any(Method.class), anyString());
         verify(workflowManagerMock, times(3)).retryAt(eq(workflow), any(Method.class), any(Date.class), any(Throwable.class));
//         verify(alertManager, times(0)).fail(eq(workflow), any(Method.class), anyString());

         ctrl.onFail(workflow, methodExecuted, new NullPointerException());
/*
         if(workflow.getContent()!=null){
             verify(alertManager, times(1)).fail(eq(workflow), eq(methodExecuted), anyString());
         }else{
             verify(alertManager, times(0)).fail(eq(workflow), eq(methodExecuted), anyString());
         }
  */
     }
	
	@SuppressWarnings("unchecked")
    @Test
	public void testResetRetryOnDifferentMethod() throws ResourceException {
		WorkFlowManager workflowManagerMock = Mockito.mock(WorkFlowManager.class);
		DefaultWorkFlowController ctrl = new DefaultWorkFlowController(workFlowConfig, workflowManagerMock);

		WorkFlow<?> workflow = new WorkFlow<>(UUID.randomUUID(), "key", "no-class", null, (BranchId)null, new Date(), null);
		Method methodA = MethodUtils.getAccessibleMethod(this.getClass(), "methodA", new Class[] {});
		Method methodB = MethodUtils.getAccessibleMethod(this.getClass(), "methodB", new Class[] {});
				
		assertEquals(0, ctrl.retries);
		ctrl.onFail(workflow, methodA, new NullPointerException());
		assertEquals(1, ctrl.retries);
		ctrl.onFail(workflow, methodB, new NullPointerException());
		assertEquals(1, ctrl.retries);
				
//		Mockito.verify(alertManager, times(2)).retry(eq(workflow), any(Method.class), anyString());
		Mockito.verify(workflowManagerMock, times(2)).retryAt(eq(workflow), any(Method.class), any(Date.class), any(Throwable.class));
//		Mockito.verify(alertManager, times(0)).fail(eq(workflow), any(Method.class), anyString());

	}

	@Test
	public void controller_onComplete_invoked() throws ResourceException, IOException {
		WorkFlowController ctrl = Mockito.mock(WorkFlowController.class);

		TestWorkFlow wf1 = new TestWorkFlow(5, new byte[5]);
		UserId userId = new UserId("test0");
		BranchId branchId = new BranchId("3456");
		Method doX = MethodUtils.getAccessibleMethod(wf1.getClass(), "doX", new Class[] {});

		WorkFlowBuilder<TestWorkFlow> builder = new WorkFlowBuilder<>();
		builder.id(UUID.randomUUID())
				.workflow(wf1)
				.externalKey("key")
				.userId(userId)
				.requestDomain(null)
				.branchId(branchId)
				.subType(null)
				.methodName("doX")
				.startWhen(null)
				.metadata(null)
				.controller(ctrl);

//		ServiceFactory.getService(WorkFlowManager.class).start(UUID.randomUUID(), "key", userId, reqDomain, branchId, wf1, subType, doA, startWhen, metadata);
		WorkFlow<TestWorkFlow> wf = workFlowManager.startImmediate(builder);

		Mockito.verify(ctrl).onStart(wf, doX);
		Mockito.verify(ctrl).onComplete(wf, doX, null, null);
	}

	@Test
	public void controller_onComplete_invoked_with_executedMethod() throws ResourceException, IOException {
		WorkFlowController ctrl = Mockito.mock(WorkFlowController.class);

		TestWorkFlow wf1 = new TestWorkFlow(2, new byte[5]);
		UserId userId = new UserId("test0");
		BranchId branchId = new BranchId("3456");
		Method doA = WorkFlowUtil.getMethod(wf1, "doA");
		Method doB = WorkFlowUtil.getMethod(wf1, "doB");
		Method doC = WorkFlowUtil.getMethod(wf1, "doC");

		WorkFlowBuilder<TestWorkFlow> builder = new WorkFlowBuilder<>();
		builder.id(UUID.randomUUID())
				.workflow(wf1)
				.externalKey("key")
				.userId(userId)
				.requestDomain(null)
				.branchId(branchId)
				.subType(null)
				.methodName("doA")
				.startWhen(null)
				.metadata(null)
				.controller(ctrl);

//		ServiceFactory.getService(WorkFlowManager.class).start(UUID.randomUUID(), "key", userId, reqDomain, branchId, wf1, subType, doA, startWhen, metadata);
		WorkFlow<TestWorkFlow> wf = workFlowManager.startImmediate(builder);

		Assertions.assertNotNull(wf);
		Mockito.verify(ctrl, times(1)).onStart(wf, doA);
		Mockito.verify(ctrl, times(1)).onComplete(wf, doA, doB, null);
		Mockito.verify(ctrl, times(1)).onStart(wf, doB);
		Mockito.verify(ctrl, times(1)).onComplete(wf, doB, doC, null);
		Mockito.verify(ctrl, times(1)).onStart(wf, doC);
		Mockito.verify(ctrl, times(1)).onComplete(wf, doC, null, null);
	}

	@Test
	public void controller_onFail_invoked() throws ResourceException, IOException {
		WorkFlowController ctrl = Mockito.mock(WorkFlowController.class);

		TestWorkFlow wf1 = new TestWorkFlow(5, new byte[5]);
		UserId userId = new UserId("test0");
		BranchId branchId = new BranchId("3456");
		Method doExpection = MethodUtils.getAccessibleMethod(wf1.getClass(), "doException", new Class[] {});

		String subType = null;
		Date startWhen = null;
		Metadata metadata = null;
		String reqDomain = null;

		WorkFlowBuilder<TestWorkFlow> builder = new WorkFlowBuilder<>();
		builder.id(UUID.randomUUID())
				.workflow(wf1)
				.externalKey("key")
				.userId(userId)
				.requestDomain(reqDomain)
				.branchId(branchId)
				.subType(subType)
				.methodName("doException")
				.startWhen(startWhen)
				.metadata(metadata)
				.controller(ctrl);

//		ServiceFactory.getService(WorkFlowManager.class).start(UUID.randomUUID(), "key", userId, reqDomain, branchId, wf1, subType, doA, startWhen, metadata);
		WorkFlow<TestWorkFlow> wf = workFlowManager.startImmediate(builder);

		Mockito.verify(ctrl).onStart(wf, doExpection);

		ArgumentCaptor<WorkFlow> argumentCaptor1 = ArgumentCaptor.forClass(WorkFlow.class);
		ArgumentCaptor<Method> argumentCaptor2 = ArgumentCaptor.forClass(Method.class);
		ArgumentCaptor<Throwable> argumentCaptor3 = ArgumentCaptor.forClass(Throwable.class);
		Mockito.verify(ctrl).onFail(argumentCaptor1.capture(), argumentCaptor2.capture(), argumentCaptor3.capture());
		Assertions.assertEquals(wf, argumentCaptor1.getValue());
		Assertions.assertEquals(doExpection, argumentCaptor2.getValue());
		Assertions.assertEquals(IllegalArgumentException.class, argumentCaptor3.getValue().getClass());
	}

	
}
