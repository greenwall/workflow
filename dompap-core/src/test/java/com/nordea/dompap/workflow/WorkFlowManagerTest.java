package com.nordea.dompap.workflow;

import com.nordea.dompap.config.WorkFlowConfigSpring;
import com.nordea.dompap.workflow.util.WorkflowProcessingStatus;
import com.nordea.dompap.workflow.config.WorkflowConfig;
import com.nordea.dompap.workflow.mock.MockWorkflowService;
import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.resource.ResourceException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = com.nordea.dompap.config.WorkFlowContextSpring.class)
@ActiveProfiles("springtest")
public class WorkFlowManagerTest extends TestWithMemoryDB {

	UserId userId = new UserId("Test2");
	BranchId branchId = new BranchId("0000");

	@Autowired
	WorkflowManager workFlowManager;

	@Test
	public void testStartImmediateFlowWithExceptionInA() throws ResourceException, IOException {
		byte[] blob = new byte[100];
		
		TestWorkFlow workflow = new TestWorkFlow(0, blob);
		Method method = WorkflowUtil.getMethod(workflow, "doA");
	
		Workflow<TestWorkFlow> task = workFlowManager.startImmediate(null, null, userId, "requestDomain", branchId, workflow, method);
		
		assertEquals("java.lang.NullPointerException", task.getExceptionName());
	}
	
	@Test
	public void testStartImmediateFlows() throws ResourceException, IOException {
		byte[] blob = new byte[100000];
		for (int n=0; n<blob.length; n++) {
			blob[n] = (byte) (n%200);
		}
		
		for (int n=0; n<10; n++) {
			TestWorkFlow workflow = new TestWorkFlow(n, blob);
			Method method = TestWorkFlow.doA;
		
			Workflow<TestWorkFlow> task = workFlowManager.startImmediate(null, null, userId, "requestDomain", branchId, workflow, method);
		}		
	}

	@Test
	public void testCreateFlow() throws ResourceException, IOException {
		String workflow = "something";

		Workflow<String> task = workFlowManager.create(UUID.randomUUID(), null, userId,
				"requestDomain", branchId, workflow.getClass().getName(), null, workflow, "start", null, null);

	}

	@Test
	public void testStartFlows() throws ResourceException, IOException {
		byte[] blob = new byte[100000];
		for (int n=0; n<blob.length; n++) {
			blob[n] = (byte) (n%200);
		}
		
		for (int n=0; n<10; n++) {
			TestWorkFlow workflow = new TestWorkFlow(n, blob);
			Method method = TestWorkFlow.doA;
		
			Workflow<TestWorkFlow> task = workFlowManager.start(null, null, userId, "requestDomain", branchId, workflow, null, method, null, null);
		}		
	}

	@Test
	public void testStartFlowAndAddExternalKey() throws ResourceException, IOException, ClassNotFoundException {
		byte[] blob = new byte[100000];
		for (int n=0; n<blob.length; n++) {
			blob[n] = (byte) (n%200);
		}
		
		TestWorkFlow workflow = new TestWorkFlow(41, blob);
		Method method = TestWorkFlow.doA;
		
		UUID id = UUID.randomUUID();
				
		Workflow<TestWorkFlow> task = workFlowManager.start(id, null, userId, "requestDomain", branchId, workflow, null, method, null, null);
		
		assertEquals(id, task.getId());
		
		String externalKey = "my-key-"+UUID.randomUUID();
		workFlowManager.updateExternalKey(task.getId(), externalKey);
		
		Workflow task2 = workFlowManager.getWorkFlow(task.getId());
		
		assertEquals(externalKey, task2.externalKey);
	}

	@Test
	public void testExceptionFromFlow() throws ResourceException, IOException {
		byte[] blob = new byte[100000];
		for (int n=0; n<blob.length; n++) {
			blob[n] = (byte) (n%200);
		}
		
		TestWorkFlow workflow = new TestWorkFlow(40, blob);
		Method method = TestWorkFlow.doA;
		
		UUID id = UUID.randomUUID();
				
		Workflow<TestWorkFlow> task = workFlowManager.start(id, null, userId, "requestDomain", branchId, workflow, null, method, null, null);
		
		assertEquals(id, task.getId());
	}

	@Test
	public void testMultipleFlowsReady() throws ResourceException, IOException {
		byte[] blob = new byte[100];
		for (int n=0; n<blob.length; n++) {
			blob[n] = (byte) (n%200);
		}
		TestWorkFlow wf1 = new TestWorkFlow(5, blob);
		TestWorkFlow wf2 = new TestWorkFlow(2, blob);
		TestWorkFlow wf3 = new TestWorkFlow(8, blob);
		TestWorkFlow wf4 = new TestWorkFlow(11, blob);
		TestWorkFlow wf5 = new TestWorkFlow(14, blob);

		Method doA = TestWorkFlow.doA;
		
		String reqDomain = null;
		Workflow<TestWorkFlow> task1 = workFlowManager.start(null, null, userId, reqDomain, branchId, wf1, null, doA, null, null);
		Workflow<TestWorkFlow> task2 = workFlowManager.start(null, null, userId, reqDomain, branchId, wf2, null, doA, null, null);
		Workflow<TestWorkFlow> task3 = workFlowManager.start(null, null, userId, reqDomain, branchId, wf3, null, doA, null, null);
		Workflow<TestWorkFlow> task4 = workFlowManager.start(null, null, userId, reqDomain, branchId, wf4, null, doA, null, null);
		Workflow<TestWorkFlow> task5 = workFlowManager.start(null, null, userId, reqDomain, branchId, wf5, null, doA, null, null);
	}

	@Test
	public void testWrappedException() throws ResourceException, IOException {
		byte[] blob = new byte[100];
		for (int n=0; n<blob.length; n++) {
			blob[n] = (byte) (n%200);
		}
		TestWorkFlow wf1 = new TestWorkFlow(5, blob);

		Method doException = TestWorkFlow.doException;
		
		Workflow<TestWorkFlow> task1 = workFlowManager.start(null, null, userId, "requestDomain", branchId, wf1, null, doException, null, null);
	}

	//@Test
	public void testPickAndExecuteAll() throws ResourceException {
		WorkflowProcessingStatus ps;
		do {
			ps = workFlowManager.pickAndExecute(TestWorkFlow.class.getName(), null, null);
		} while (ps!=WorkflowProcessingStatus.idle);
	}

	// select for update skip locked not working on HSQLDB
	@Test
	public void testPickAndExecuteFirst() throws ResourceException {
        WorkflowProcessingStatus ps;
		ps = workFlowManager.pickAndExecute("com.nordea.documentbox.workflow.fi.DocumentProcessFI", null, null);
	}
	
	@Test 
	public void testMethodLookup() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		TestWorkFlow content = new TestWorkFlow(5, "test".getBytes());
		Method doA = WorkflowUtil.getMethod(TestWorkFlow.class, "doA");
		assertEquals("doA", doA.getName());

		Method doA2 = WorkflowUtil.getMethod(content, "doA");
		assertEquals("doA", doA2.getName());
		assertEquals(doA, doA2);
		
		Method doB = WorkflowUtil.getMethod(TestWorkFlow.class, "doB");
		assertEquals("doB", doB.getName());
				
		Workflow<?> wf = new Workflow<>();

//    	assertEquals(new Class[] {WorkFlow.class}, doA.getParameterTypes());
		assertEquals(Workflow.class, doA.getParameterTypes()[0]);
    	doA.invoke(content, wf);
    	
    	if (Arrays.equals(doA.getParameterTypes(), new Class[] {Workflow.class})) {
    		                		
    	} else {
    		fail("doA has one formal parameter of type WorkFlow.class");
    	}
    	assertEquals(0, doB.getParameterTypes().length);
	}
	
	@Test
	public void testExecute() throws ResourceException {
		WorkflowService workFlowService = new MockWorkflowService();
		WorkflowConfig config = new WorkFlowConfigSpring();
		//WorkFlowContext context = new WorkFlowContext();
		//context.setWorkFlowService(workFlowService);


		TestWorkFlow content = new TestWorkFlow(5, "test".getBytes());
		UUID id = UUID.randomUUID();
		String externalKey = null;
		String subType = null;
		Date creationTime = new Date();
		Date lastUpdateTime = new Date();
		Workflow<TestWorkFlow> wf = new Workflow<>(id, externalKey, content.getClass().getName(), subType, userId, branchId, creationTime, lastUpdateTime);
		wf.setContent(content);
		Method doA = TestWorkFlow.doA;

		WorkflowManagerImpl workFlowManager = new WorkflowManagerImpl(config, workFlowService);
		WorkflowController controller = new DefaultWorkflowController(config, workFlowManager);
//		wf.setMethodStarted(new Date());
		wf.setMethodName(doA.getName());


		workFlowManager.execute(wf, doA, controller );
		
		assertEquals(id.toString(), content.myId);
		assertEquals("step B did: [The number 5.doA(5)]", content.s);
		assertEquals("doB", wf.getMethodName());
	}
	
}
