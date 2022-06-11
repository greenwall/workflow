package com.nordea.dompap.workflow;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.UUID;

import javax.resource.ResourceException;

import org.joda.time.DateTime;

import static com.nordea.dompap.workflow.WorkflowUtil.*;

/**
 * TestWorkflow2 that sends an event to itself and schedules stepC for execution after 30 days, but expect the event to execute instead. 
 */
public class TestWorkflow2 implements Serializable {
	private static final long serialVersionUID = -8430339138038156819L;
	public String some = "Some";
	UUID id;

	public static final WhenMethod stepA = getWhenMethod(TestWorkflow2.class, "stepA", null);
	public WhenMethod stepA() {
		return stepB;
	}

	private static final WhenMethod stepB = getWhenMethod(TestWorkflow2.class, "stepB", null);
	public WhenMethod stepB(Workflow workflow) throws ResourceException  {
		// Send an event and schedule execution of stepC - event should get executed
//		WorkFlow<?> workflow = ServiceFactory.getService(WorkFlowService.class).getWorkFlow(id);

		UUID eventId = UUID.randomUUID();
		// TODO fix how to inject services in workflow instance ??
//		ServiceFactory.getService(WorkFlowEventService.class).createEvent(eventId, "test".getBytes(),"test", id, "fromTestWorkflow2.stepB");
//		ServiceFactory.getService(WorkFlowService.class).queueEvent(workflow, eventId);
		
		return stepC.when(DateTime.now().plusDays(30)).orEvent();
	}

	private static final WhenMethod stepC = getWhenMethod(TestWorkflow2.class, "stepC", null);
	public WhenMethod stepC() {
		return null;
	}
	
	public Method onEvent() {
		some += ".onEvent invoked";
		return null;
	}
	
}
