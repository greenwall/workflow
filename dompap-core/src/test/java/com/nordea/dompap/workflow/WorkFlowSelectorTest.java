package com.nordea.dompap.workflow;

import com.nordea.dompap.workflow.config.WorkflowConfig;
import com.nordea.dompap.workflow.event.WorkflowEventService;
import com.nordea.dompap.workflow.selector.ExecutorInfo;
import com.nordea.dompap.workflow.selector.WorkflowSelector;
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
	private WorkflowConfig config;

	@Autowired
	private WorkflowService workFlowService;

	@Autowired
	private WorkflowManager workFlowManager;

	@Autowired
	private WorkflowSelector selector;

	@Autowired
	private WorkflowEventService eventService;

	@Autowired
	private MetadataService metadataService;

	private ExecutorInfo executorInto = new ExecutorInfo("localhost", "-");


	@Test
	public void testSelectWorkflow() throws ResourceException, IOException {
		// In memory DB is cleared between tests, so propertyType map is invalid.
		((MetadataServiceImpl)metadataService).initPropertyTypes();

		MyWorkflow wf = new MyWorkflow();
		UUID id = UUID.randomUUID();
		Metadata wfMetadata = new Metadata(null);
		wfMetadata.putProperty(new PropertyType("PROPERTY", null), "VALUE");
		DefaultWorkflowController wfController = new DefaultWorkflowController(config, workFlowManager);
		workFlowService.insertWorkFlow(id, "TEST1", new UserId("X00000"), "requestDomain", new BranchId("1111"), wf, WorkflowUtil.getMethod(MyWorkflow.class, "doStuff"), new Date(), wfMetadata , wfController);
		
		
		WorkflowSearch search = new WorkflowSearch();
		WorkflowSearchResult result = workFlowService.searchWorkFlows(search, 0, 100);
		assertEquals(1, result.totalWorkflows);
				
		Workflow<?> selected = selector.pickReadyWorkFlow(MyWorkflow.class.getName(), null, executorInto);
		assertEquals(id, selected.getId());
		
	}

	@Test
	public void testReadyMethodOrEvent() throws ResourceException, IOException {
		MyWorkflow myworkflow = new MyWorkflow();

		UUID id = UUID.randomUUID();
		Metadata wfMetadata = new Metadata(null);
		DefaultWorkflowController wfController = new DefaultWorkflowController(config, workFlowManager);
		Method doStuff = WorkflowUtil.getMethod(MyWorkflow.class, "doStuff");
		String subType = null;
		Workflow<MyWorkflow> workflow = workFlowService.insertWorkFlow(id, "TEST1", new UserId("X00000"), "requestDomain", new BranchId("1111"), myworkflow, subType, doStuff, new Date(), wfMetadata , wfController);
		
		WorkflowSearch search = new WorkflowSearch();
		WorkflowSearchResult result = workFlowService.searchWorkFlows(search, 0, 100);
		assertEquals(1, result.totalWorkflows);
		
		// First time workflow is ready 
		Workflow<?> selected = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), null, executorInto);
		assertEquals(id, selected.getId());

		// When workflow updated to NOT allow events it will not be selected
//		Method method = WorkFlowUtil.getMethod(wf, "doStuff");
		workFlowService.updateWorkFlow(selected, (String)null, null, new Date(), new Date(), null, wfMetadata, false);
		
		// Second time workflow is not ready 
		Workflow<?> selected2 = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), null, executorInto);
		assertNull(selected2);
		
		UUID eventId = UUID.randomUUID();
		eventService.createEvent(eventId, "test".getBytes(), "test", id, "name");
		
		workFlowService.queueEvent(workflow, eventId);
		
		Workflow<?> updated = workFlowService.getWorkFlow(id);
		assertEquals(1, updated.getEventsQueued());
		
		// Third time workflow is not ready with pending event because it has PROCESS_EVENTS==false.
		Workflow<?> selected3 = selector.pickReadyWorkFlow(MyWorkflow.class.getName(), null, executorInto);
		assertNull(selected3);

//		Method method = WorkFlowUtil.getMethod(wf, "doStuff");
		workFlowService.updateWorkFlow(selected, (String)null, null, new Date(), new Date(), null, wfMetadata, true);
		
		// Third time workflow is ready with pending event.
		Workflow<?> selected4 = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), null, executorInto);
		assertEquals(id, selected4.getId());
		assertEquals(eventId, selected4.getCurrentEventId());
		assertEquals("onEvent", selected4.getMethodName());
		
	}

	@Test
	public void testReadyWithAnySubType() throws ResourceException, IOException {
		MyWorkflow myworkflow = new MyWorkflow();

		UUID id = UUID.randomUUID();
		Metadata wfMetadata = new Metadata(null);
		DefaultWorkflowController wfController = new DefaultWorkflowController(config, workFlowManager);
		workFlowService.insertWorkFlow(id, "TEST1", new UserId("X00000"), "requestDomain", new BranchId("1111"), myworkflow, "subtype", WorkflowUtil.getMethod(MyWorkflow.class, "doStuff"), new Date(), wfMetadata , wfController);
				
		// Select workflow with other subtype 
		Workflow<?> selected = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), "other", executorInto);
		assertNull(selected);

		// Select workflow with any subtype 
		Workflow<?> selectedAny = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), null, executorInto);
		assertEquals(id, selectedAny.getId());
	}	
	
	@Test
	public void testReadyWithSpecificSubType() throws ResourceException, IOException {
		MyWorkflow myworkflow = new MyWorkflow();

		UUID id = UUID.randomUUID();
		Metadata wfMetadata = new Metadata(null);
		DefaultWorkflowController wfController = new DefaultWorkflowController(config, workFlowManager);
		workFlowService.insertWorkFlow(id, "TEST1", new UserId("X00000"), "requestDomain", new BranchId("1111"), myworkflow, "subtype", WorkflowUtil.getMethod(MyWorkflow.class, "doStuff"), new Date(), wfMetadata , wfController);
		
		// Select workflow with other subtype 
		Workflow<?> selected = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), "other", executorInto);
		assertNull(selected);

		// Select workflow with any subtype 
		Workflow<?> selectedSubType = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), "subtype", executorInto);
		assertEquals(id, selectedSubType.getId());
		
	}	

	@Test
	public void testReadyWithNotNullSubType() throws ResourceException, IOException {
		MyWorkflow myworkflow = new MyWorkflow();

		UUID id = UUID.randomUUID();
		Metadata wfMetadata = new Metadata(null);
		DefaultWorkflowController wfController = new DefaultWorkflowController(config, workFlowManager);
		workFlowService.insertWorkFlow(id, "TEST1", new UserId("X00000"), "requestDomain", new BranchId("1111"), myworkflow, "mysubtype", WorkflowUtil.getMethod(MyWorkflow.class, "doStuff"), new Date(), wfMetadata , wfController);
		
		{
			// Select workflow with other subtype 
			Workflow<?> selected = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), "", executorInto);
			assertNull(selected);
	
			// Select workflow with any subtype 
			Workflow<?> selectedAny = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), null, executorInto);
			assertEquals(id, selectedAny.getId());
		}
		
		UUID id2 = UUID.randomUUID();
		workFlowService.insertWorkFlow(id2, "TEST2", new UserId("X00000"), "requestDomain", new BranchId("1111"), myworkflow, null, WorkflowUtil.getMethod(MyWorkflow.class, "doStuff"), new Date(), wfMetadata , wfController);
		{
			// Select workflow with other subtype 
			Workflow<?> selected = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), "", executorInto);
			assertEquals(id2, selected.getId());
		}

		UUID id3 = UUID.randomUUID();
		workFlowService.insertWorkFlow(id3, "TEST3", new UserId("X00000"), "requestDomain", new BranchId("1111"), myworkflow, null, WorkflowUtil.getMethod(MyWorkflow.class, "doStuff"), new Date(), wfMetadata , wfController);
		{
			// Select workflow with other subtype 
			Workflow<?> selected = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), null, executorInto);
			assertEquals(id3, selected.getId());
		}
		
	}	

	@Test
	public void testReadyWithAnyNonEmptySubType() throws ResourceException, IOException {
		MyWorkflow myworkflow = new MyWorkflow();

		UUID id = UUID.randomUUID();
		Metadata wfMetadata = new Metadata(null);
		DefaultWorkflowController wfController = new DefaultWorkflowController(config, workFlowManager);
		workFlowService.insertWorkFlow(id, "TEST1", new UserId("X00000"), "requestDomain", new BranchId("1111"), myworkflow, "mysubtype", WorkflowUtil.getMethod(MyWorkflow.class, "doStuff"), new Date(), wfMetadata , wfController);
		
		{
			// Select workflow with other subtype 
			Workflow<?> selected = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), "", executorInto);
			assertNull(selected);
	
			// Select workflow with any subtype 
			Workflow<?> selectedAny = selector.pickReadyWorkFlow(myworkflow.getClass().getName(), "%", executorInto);
			assertEquals(id, selectedAny.getId());
		}
	}	
	
}
