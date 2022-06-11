package com.nordea.dompap.workflow.event;

import com.nordea.dompap.workflow.Workflow;

import javax.resource.ResourceException;
import java.lang.reflect.Method;

/**
 * Implemented by workflow processes that wants to be activated when an event is received.
 * EventHandlers will receive events and store them in WorkFlowEventService (DB table WFLW_EVENT).
 * EventHandlers will then queue events to the corresponding workflow. 
 */
public interface WorkflowEventListener {
	/**
	 * An event is waiting (in table WFLW_EVENT) for processing.
	 * The first (oldest) event is available in workflow.currentEventId, and WorkFlow.loadCurrentEvent
	 */
	Method onEvent(Workflow<?> workflow) throws ResourceException;
}
