package com.nordea.dompap.workflow;

import com.nordea.dompap.workflow.config.WorkFlowConfig;
import com.nordea.dompap.workflow.event.WorkFlowEventService;
import com.nordea.dompap.workflow.selector.ExecutorInfo;
import com.nordea.dompap.workflow.selector.WorkFlowSelector;
import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.resource.ResourceException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = com.nordea.dompap.config.WorkFlowContextSpring.class)
@ActiveProfiles("springtest")
public class WorkFlowSelectorTest extends TestWithMemoryDB {

	@Autowired
	private WorkFlowConfig config;

	@Autowired
	private WorkFlowService workFlowService;

	@Autowired
	private WorkFlowManager workFlowManager;

	@Autowired
	private WorkFlowSelector selector;

	@Autowired
	private WorkFlowEventService eventService;

	private ExecutorInfo executorInto = new ExecutorInfo("localhost", "-");


	@Test
	public void testSelectWorkflow() throws ResourceException, IOException {
		MyWorkflow wf = new MyWorkflow();
		UUID id = UUID.randomUUID();
		Metadata wfMetadata = new Metadata(null);
		wfMetadata.putProperty(new PropertyType("PROPERTY", null), "VALUE");
		DefaultWorkFlowController wfController = new DefaultWorkFlowController(config, workFlowManager);
		workFlowService.insertWorkFlow(id, "TEST1", new UserId("X00000"), "requestDomain", new BranchId("1111"), wf, WorkFlowUtil.getMethod(MyWorkflow.class, "doStuff"), new Date(), wfMetadata , wfController);
		
		
		WorkFlowSearch search = new WorkFlowSearch();
		WorkFlowSearchResult result = workFlowService.searchWorkFlows(search, 0, 100);
		assertEquals(1, result.totalWorkflows);
				
		WorkFlow<?> selected = selector.pickReadyWorkFlow(MyWorkflow.class.getName(), null, executorInto);
		assertEquals(id, selected.getId());
		
	}

	@Test
	public void testReadyMethodOrEvent() throws ResourceException, IOException {
		MyWorkflow myworkflow = new MyWorkflow();

		UUID id = UUID.randomUUID();
		Metadata wfMetadata = new Metadata(null);
		DefaultWorkFlowController wfController = new DefaultWorkFlowController(config, workFlowManager);
		Method doStuff = WorkFlowUtil.getMethod(MyWorkflow.class, "doStuff");
		String subType = null;
		WorkFlow<MyWorkflow> workflow = workFlowService.insertWorkFlow(id, "TEST1", new UserId("X00000"), "requestDomain", new BranchId("1111"), myworkflow, subType, doStuff, new Date(), wfMetadata , wfController);
		
		WorkFlowSearch search = new WorkFlowSearch();
		WorkFlowSearchResult result = workFlowService.searchWorkFlows(search, 0, 100);
		assertEquals(1, result.totalWorkflows);
		
		// First time workflow is ready 
		WorkFlow<?> selected = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), null, executorInto);
		assertEquals(id, selected.getId());

		// When workflow updated to NOT allow events it will not be selected
//		Method method = WorkFlowUtil.getMethod(wf, "doStuff");
		workFlowService.updateWorkFlow(selected, (String)null, null, new Date(), new Date(), null, wfMetadata, false);
		
		// Second time workflow is not ready 
		WorkFlow<?> selected2 = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), null, executorInto);
		assertNull(selected2);
		
		UUID eventId = UUID.randomUUID();
		eventService.createEvent(eventId, "test".getBytes(), "test", id, "name");
		
		workFlowService.queueEvent(workflow, eventId);
		
		WorkFlow<?> updated = workFlowService.getWorkFlow(id);
		assertEquals(1, updated.getEventsQueued());
		
		// Third time workflow is not ready with pending event because it has PROCESS_EVENTS==false.
		WorkFlow<?> selected3 = selector.pickReadyWorkFlow(MyWorkflow.class.getName(), null, executorInto);
		assertNull(selected3);

//		Method method = WorkFlowUtil.getMethod(wf, "doStuff");
		workFlowService.updateWorkFlow(selected, (String)null, null, new Date(), new Date(), null, wfMetadata, true);
		
		// Third time workflow is ready with pending event.
		WorkFlow<?> selected4 = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), null, executorInto);
		assertEquals(id, selected4.getId());
		assertEquals(eventId, selected4.getCurrentEventId());
		assertEquals("onEvent", selected4.getMethodName());
		
	}

	@Test
	public void testReadyWithAnySubType() throws ResourceException, IOException {
		MyWorkflow myworkflow = new MyWorkflow();

		UUID id = UUID.randomUUID();
		Metadata wfMetadata = new Metadata(null);
		DefaultWorkFlowController wfController = new DefaultWorkFlowController(config, workFlowManager);
		workFlowService.insertWorkFlow(id, "TEST1", new UserId("X00000"), "requestDomain", new BranchId("1111"), myworkflow, "subtype", WorkFlowUtil.getMethod(MyWorkflow.class, "doStuff"), new Date(), wfMetadata , wfController);
				
		// Select workflow with other subtype 
		WorkFlow<?> selected = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), "other", executorInto);
		assertNull(selected);

		// Select workflow with any subtype 
		WorkFlow<?> selectedAny = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), null, executorInto);
		assertEquals(id, selectedAny.getId());
	}	
	
	@Test
	public void testReadyWithSpecificSubType() throws ResourceException, IOException {
		MyWorkflow myworkflow = new MyWorkflow();

		UUID id = UUID.randomUUID();
		Metadata wfMetadata = new Metadata(null);
		DefaultWorkFlowController wfController = new DefaultWorkFlowController(config, workFlowManager);
		workFlowService.insertWorkFlow(id, "TEST1", new UserId("X00000"), "requestDomain", new BranchId("1111"), myworkflow, "subtype", WorkFlowUtil.getMethod(MyWorkflow.class, "doStuff"), new Date(), wfMetadata , wfController);
		
		// Select workflow with other subtype 
		WorkFlow<?> selected = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), "other", executorInto);
		assertNull(selected);

		// Select workflow with any subtype 
		WorkFlow<?> selectedSubType = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), "subtype", executorInto);
		assertEquals(id, selectedSubType.getId());
		
	}	

	@Test
	public void testReadyWithNotNullSubType() throws ResourceException, IOException {
		MyWorkflow myworkflow = new MyWorkflow();

		UUID id = UUID.randomUUID();
		Metadata wfMetadata = new Metadata(null);
		DefaultWorkFlowController wfController = new DefaultWorkFlowController(config, workFlowManager);
		workFlowService.insertWorkFlow(id, "TEST1", new UserId("X00000"), "requestDomain", new BranchId("1111"), myworkflow, "mysubtype", WorkFlowUtil.getMethod(MyWorkflow.class, "doStuff"), new Date(), wfMetadata , wfController);
		
		{
			// Select workflow with other subtype 
			WorkFlow<?> selected = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), "", executorInto);
			assertNull(selected);
	
			// Select workflow with any subtype 
			WorkFlow<?> selectedAny = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), null, executorInto);
			assertEquals(id, selectedAny.getId());
		}
		
		UUID id2 = UUID.randomUUID();
		workFlowService.insertWorkFlow(id2, "TEST1", new UserId("X00000"), "requestDomain", new BranchId("1111"), myworkflow, null, WorkFlowUtil.getMethod(MyWorkflow.class, "doStuff"), new Date(), wfMetadata , wfController);
		{
			// Select workflow with other subtype 
			WorkFlow<?> selected = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), "", executorInto);
			assertEquals(id2, selected.getId());
		}

		UUID id3 = UUID.randomUUID();
		workFlowService.insertWorkFlow(id3, "TEST1", new UserId("X00000"), "requestDomain", new BranchId("1111"), myworkflow, null, WorkFlowUtil.getMethod(MyWorkflow.class, "doStuff"), new Date(), wfMetadata , wfController);
		{
			// Select workflow with other subtype 
			WorkFlow<?> selected = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), null, executorInto);
			assertEquals(id3, selected.getId());
		}
		
	}	

	@Test
	public void testReadyWithAnyNonEmptySubType() throws ResourceException, IOException {
		MyWorkflow myworkflow = new MyWorkflow();

		UUID id = UUID.randomUUID();
		Metadata wfMetadata = new Metadata(null);
		DefaultWorkFlowController wfController = new DefaultWorkFlowController(config, workFlowManager);
		workFlowService.insertWorkFlow(id, "TEST1", new UserId("X00000"), "requestDomain", new BranchId("1111"), myworkflow, "mysubtype", WorkFlowUtil.getMethod(MyWorkflow.class, "doStuff"), new Date(), wfMetadata , wfController);
		
		{
			// Select workflow with other subtype 
			WorkFlow<?> selected = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), "", executorInto);
			assertNull(selected);
	
			// Select workflow with any subtype 
			WorkFlow<?> selectedAny = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), "%", executorInto);
			assertEquals(id, selectedAny.getId());
		}
	}	
	
}
