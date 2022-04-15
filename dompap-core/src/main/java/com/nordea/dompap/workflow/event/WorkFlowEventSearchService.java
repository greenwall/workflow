package com.nordea.dompap.workflow.event;

import javax.resource.ResourceException;

/**
 * Allows searching events.
 */
public interface WorkFlowEventSearchService {

	/**
	 * Search events based on search returning a list beginning with startRow and maximum of maxRows.
	 */
	WorkFlowEventSearchResult searchEvents(WorkFlowEventSearch search, Integer startRow, Integer maxRows) throws ResourceException;
}
