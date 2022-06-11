package com.nordea.dompap.workflow.event;

import javax.resource.ResourceException;
import java.util.UUID;

public interface WorkflowEventService {

	/**
	 * Create an event. If workflowId is given the event is queued for execution by the workflow.
	 * @param id the UUID for the event
	 * @param eventType source of the events (i.e. NDS3 ...)
	 * @param workflowId id of target workflow - may be null and updated later
	 * @param eventName the name of the event (presumably parsed from the content - may be null and updated later 
	 */
	WorkflowEvent createEvent(UUID id, byte[] content, String eventType, UUID workflowId, String eventName) throws ResourceException;
	
	/**
	 * Create an event. If workflowId is given the event is queued for execution by the workflow.
	 * @param workFlowEventBuilder  for the event
	 */
	WorkflowEvent createEvent(WorkflowEventBuilder workFlowEventBuilder) throws ResourceException;
	
	
	/**
	 * Add additional information to an existing event for searching purposes.
	 * If workflowId is given the event is queued for execution by the workflow.
	 */
	WorkflowEvent updateEventInfo(WorkflowEvent event, UUID workflowId, String eventName, String userId, String applicationId, String technicalUserId, String requestId, String requestDomain, String sessionId) throws ResourceException;
	
	/**
	 * Get the specified event or null if none found.
	 */
	WorkflowEvent getEvent(UUID uuid) throws ResourceException;

	/**
	 * Get the oldest/first event waiting for (not processed) the given workflow.
	 */
	WorkflowEvent getFirstEventFor(UUID workflowId) throws ResourceException;

	/**
	 * Get the last processed event in the given workflow.
	 */
	WorkflowEvent getLastEventFor(UUID workflowId) throws ResourceException;
	
	/**
	 * Mark this event as processed, so it will not be returned from getFirstEvent again.
	 */
	void processedEvent(WorkflowEvent event) throws ResourceException;
	
	/**
	 * Move the event to the archive tables.
	 */
	void archiveEvent(WorkflowEvent event) throws ResourceException;

}
