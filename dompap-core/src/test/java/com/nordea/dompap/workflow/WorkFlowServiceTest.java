package com.nordea.dompap.workflow;

import com.nordea.dompap.workflow.util.WorkflowProcessingStatus;
import com.nordea.dompap.workflow.content.DefaultWorkFlowContentSerializer;
import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.UserId;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.resource.ResourceException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = com.nordea.dompap.config.WorkFlowContextSpring.class)
@ActiveProfiles("springtest")
public class WorkFlowServiceTest extends TestWithMemoryDB {

	@Autowired
	WorkFlowService workFlowService;

	@Autowired
	WorkFlowManager workFlowManager;

	@Test
	public void testStoreRetrieve() throws ResourceException, IOException, ClassNotFoundException {

		UserId userId = new UserId("g93283");
		BranchId branchId = new BranchId("1234");
		byte[] blob = new byte[100000];
		for (int n=0; n<blob.length; n++) {
			blob[n] = (byte) (n%200);
		}
		TestWorkFlow instance = new TestWorkFlow(4, blob);
		Method method = WorkFlowUtil.getMethod(instance, "doA");

		WorkFlowBuilder<TestWorkFlow> builder = new WorkFlowBuilder<TestWorkFlow>()
				.userId(userId)
				.requestDomain("requestDomain")
				.branchId(branchId)
				.workflow(instance)
				.methodName(method.getName())
				.contentSerializer(new DefaultWorkFlowContentSerializer());

		WorkFlow<TestWorkFlow> task1 = workFlowService.insertWorkFlow(builder);

//		WorkFlow<TestWorkFlow> task1 = workFlowService.insertWorkFlow(null, null, userId, "requestDomain", branchId, instance, method, null, null, null);
				
		WorkFlow<TestWorkFlow> task2 = workFlowService.getWorkFlow(task1.getId(), TestWorkFlow.class);
		
		assertEquals(task1.getId(), task2.getId());
		assertEquals(task1.getUserId(), task2.getUserId());
		assertEquals(task1.getBranchId(), task2.getBranchId());
		assertEquals(task1.getLastUpdateTime(), task2.getLastUpdateTime());
		assertEquals(task1.getCreationTime(), task2.getCreationTime());
		assertEquals(null, task2.getContent());

		TestWorkFlow instance2 = workFlowService.loadWorkFlowContent(task2);
		assertEquals(instance.s, instance2.s);
		assertEquals(instance.n, instance2.n);
		assertEquals(null, instance2.t);
		assertEquals(instance.list, instance2.list);
		assertEquals(instance.map, instance2.map);
		assertEquals(instance.bytes.length, instance2.bytes.length);
		
	}

	//@Test
	public void testStartImmediateFlowWithExceptionInA() throws ResourceException, IOException {
		UserId userId = new UserId("g93283");
		BranchId branchId = new BranchId("1234");
		byte[] blob = new byte[100];
		
		TestWorkFlow workflow = new TestWorkFlow(0, blob);
		Method method = WorkFlowUtil.getMethod(workflow, "doA");
	
		WorkFlow<TestWorkFlow> task = workFlowManager.startImmediate(null, null, userId, "requestDomain", branchId, workflow, method);
	}
	
	//@Test
	public void testStartImmediateFlows() throws ResourceException, IOException {
		UserId userId = new UserId("g93283");
		BranchId branchId = new BranchId("1234");
		byte[] blob = new byte[100000];
		for (int n=0; n<blob.length; n++) {
			blob[n] = (byte) (n%200);
		}
		
		for (int n=0; n<10; n++) {
			TestWorkFlow workflow = new TestWorkFlow(n, blob);
			Method method = WorkFlowUtil.getMethod(workflow, "doA");
		
			WorkFlow<TestWorkFlow> task = workFlowManager.startImmediate(null, null, userId, "requestDomain", branchId, workflow, method);
		}		
	}

//	@Test
	public void testStartFlows() throws ResourceException, IOException {
		UserId userId = new UserId("test");
		BranchId branchId = new BranchId("1234");
		byte[] blob = new byte[100000];
		for (int n=0; n<blob.length; n++) {
			blob[n] = (byte) (n%200);
		}
		
		for (int n=0; n<10; n++) {
			TestWorkFlow workflow = new TestWorkFlow(n, blob);
			Method method = WorkFlowUtil.getMethod(workflow, "doA");
		
			WorkFlow<TestWorkFlow> task = workFlowManager.start(null, null, userId, "requestDomain", branchId, workflow, null, method, null, null);
		}		
	}

//	@Test
	public void testStartFlowAndAddExternalKey() throws ResourceException, IOException, ClassNotFoundException {
		UserId userId = new UserId("g93283");
		BranchId branchId = new BranchId("1234");
		byte[] blob = new byte[100000];
		for (int n=0; n<blob.length; n++) {
			blob[n] = (byte) (n%200);
		}
		
		TestWorkFlow workflow = new TestWorkFlow(41, blob);
		Method method = WorkFlowUtil.getMethod(workflow, "doA");
		
		UUID id = UUID.randomUUID();
				
		WorkFlow<TestWorkFlow> task = workFlowManager.start(id, null, userId, "requestDomain", branchId, workflow, null, method, null, null);
		
		assertEquals(id, task.getId());
		
		String externalKey = "my-key-"+UUID.randomUUID();
		workFlowManager.updateExternalKey(task.getId(), externalKey);
		
		WorkFlow task2 = workFlowManager.getWorkFlow(task.getId());
		
		assertEquals(externalKey, task2.externalKey);
	}

	//@Test(expected=RuntimeException.class)
	public void testExceptionFromFlow() throws ResourceException, IOException {
		UserId userId = new UserId("g93283");
		BranchId branchId = new BranchId("1234");
		byte[] blob = new byte[100000];
		for (int n=0; n<blob.length; n++) {
			blob[n] = (byte) (n%200);
		}
		
		TestWorkFlow workflow = new TestWorkFlow(40, blob);
		Method method = WorkFlowUtil.getMethod(workflow, "doA");
		
		UUID id = UUID.randomUUID();
				
		WorkFlow<TestWorkFlow> task = workFlowManager.start(id, null, userId, "requestDomain", branchId, workflow, null, method, null, null);
		
		assertEquals(id, task.getId());
	}

	//@Test
	public void testMultipleFlowsReady() throws ResourceException, IOException {

		UserId userId = new UserId("test1");
		BranchId branchId = new BranchId("1234");

		byte[] blob = new byte[100];
		for (int n=0; n<blob.length; n++) {
			blob[n] = (byte) (n%200);
		}
		TestWorkFlow wf1 = new TestWorkFlow(5, blob);
		TestWorkFlow wf2 = new TestWorkFlow(2, blob);
		TestWorkFlow wf3 = new TestWorkFlow(8, blob);
		TestWorkFlow wf4 = new TestWorkFlow(11, blob);
		TestWorkFlow wf5 = new TestWorkFlow(14, blob);

		Method doA = WorkFlowUtil.getMethod(TestWorkFlow.class, "doA");
		
		WorkFlow<TestWorkFlow> task1 = workFlowManager.start(null, null, userId, "requestDomain", branchId, wf1, null, doA, null, null);
		WorkFlow<TestWorkFlow> task2 = workFlowManager.start(null, null, userId, "requestDomain", branchId, wf2, null, doA, null, null);
		WorkFlow<TestWorkFlow> task3 = workFlowManager.start(null, null, userId, "requestDomain", branchId, wf3, null, doA, null, null);
		WorkFlow<TestWorkFlow> task4 = workFlowManager.start(null, null, userId, "requestDomain", branchId, wf4, null, doA, null, null);
		WorkFlow<TestWorkFlow> task5 = workFlowManager.start(null, null, userId, "requestDomain", branchId, wf5, null, doA, null, null);
		
	}

	//@Test
	public void testWrappedException() throws ResourceException, IOException {

		UserId userId = new UserId("test1");
		BranchId branchId = new BranchId("1234");

		byte[] blob = new byte[100];
		for (int n=0; n<blob.length; n++) {
			blob[n] = (byte) (n%200);
		}
		TestWorkFlow wf1 = new TestWorkFlow(5, blob);

		Method doA = WorkFlowUtil.getMethod(TestWorkFlow.class, "doException");
		
		WorkFlow<TestWorkFlow> task1 = workFlowManager.start(null, null, userId, "requestDomain", branchId, wf1, null, doA, null, null);
	}
	
	//@Test
	public void testPickAndExecuteAll() throws ResourceException {
	    WorkflowProcessingStatus ps;
		do {
			ps = workFlowManager.pickAndExecute(TestWorkFlow.class.getName(), null);
		} while (ps!=WorkflowProcessingStatus.idle);
	}
	
	@Test
	public void testPickFirst() throws ResourceException, IOException {
		UserId userId = new UserId("test1");
		BranchId branchId = new BranchId(""+RandomUtils.nextInt(9999));
		byte[] blob = new byte[0];
		
		Method doA = WorkFlowUtil.getMethod(TestWorkFlow.class, "doA");
		long timestamp = System.currentTimeMillis();
		TestWorkFlow wf1 = new TestWorkFlow(5, blob);
		
		WorkFlow<?> w1 = workFlowService.insertWorkFlow(null, null, userId, "requestDomain", branchId, wf1, null, doA, null, null, null);
        		
		WorkFlow<?> wf = workFlowService.pickReadyWorkFlow(TestWorkFlow.class.getName(), null);
		
		assertNotNull(wf);
	}
	
}
