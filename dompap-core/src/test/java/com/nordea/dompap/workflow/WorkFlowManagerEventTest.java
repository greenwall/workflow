package com.nordea.dompap.workflow;

import com.nordea.dompap.workflow.config.WorkFlowConfig;
import com.nordea.dompap.workflow.event.WorkFlowEventService;
import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.UserId;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.resource.ResourceException;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = com.nordea.dompap.config.WorkFlowContextSpring.class)
@ActiveProfiles("springtest")
public class WorkFlowManagerEventTest extends TestWithMemoryDB {

	@Autowired
	WorkFlowConfig config;

	@Autowired
	WorkFlowService service;

	@Autowired
	WorkFlowManager mgr;

	@Autowired
	WorkFlowEventService workFlowEventService;

	DefaultWorkFlowController wfController = new DefaultWorkFlowController(config, mgr);

	/**
	 * TestWorkflow2 that sends an event to itself and schedules stepC for execution after 30 days, but expect the event to execute instead. 
	 */
	@Test
	public void testReadyMethodOrEvent() throws ResourceException, IOException, ClassNotFoundException {

		UUID id = UUID.randomUUID();
		TestWorkflow2 myworkflow = new TestWorkflow2();
		myworkflow.id = id;
		DateTime now = DateTime.now();
		
		String subType = null;
		WorkFlow<?> workflow = service.insertWorkFlow(id, "TEST1", new UserId("X00000"), "NO", new BranchId("1111"), myworkflow, subType, TestWorkflow2.stepA.method, new Date(), null, wfController);
		
		WorkFlowSearch search = new WorkFlowSearch();
		WorkFlowSearchResult result = service.searchWorkFlows(search, 0, 100, false);
		assertEquals(1, result.totalWorkflows);
		
//		WorkFlowSelector selector = new WorkFlowSelector();
		
		// First time workflow is ready 
//		WorkFlow<?> selected = selector.pickReadyWorkFlow(TestWorkflow2.class.getName());
//		Assert.assertEquals(id, selected.getId());

		mgr.pickAndExecute(TestWorkflow2.class.getName(), null);
		
		WorkFlow<?> updated = service.getWorkFlow(id);
		assertEquals(0, updated.getEventsQueued());
		assertEquals("stepC", updated.getMethodName());
		assertTrue(now.plusDays(30).isBefore(new DateTime(updated.getStartWhen())));

		UUID eventId = UUID.randomUUID();
		workFlowEventService.createEvent(eventId, "test".getBytes(), "test", id, "test");
		service.queueEvent(workflow, eventId);
		
		// Third time workflow is ready with pending event.
//		WorkFlow<?> selected4 = selector.pickReadyWorkFlow(TestWorkflow2.class.getName(), null);
		mgr.pickAndExecute(TestWorkflow2.class.getName(), null);
		WorkFlow<TestWorkflow2> selected4 = mgr.getWorkFlow(id);
		assertEquals(id, selected4.getId());
		assertEquals("onEvent", selected4.getMethodName());
		assertEquals("Some.onEvent invoked", selected4.getContent().some);
	}
	
}
