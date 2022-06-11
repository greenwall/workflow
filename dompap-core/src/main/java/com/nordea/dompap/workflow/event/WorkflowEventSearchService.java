package com.nordea.dompap.workflow.event;

import javax.resource.ResourceException;

/**
 * Allows searching events.
 */
public interface WorkflowEventSearchService {

	/**
	 * Search events based on search returning a list beginning with startRow and maximum of maxRows.
	 */
	WorkflowEventSearchResult searchEvents(WorkflowEventSearch search, Integer startRow, Integer maxRows) throws ResourceException;
}
