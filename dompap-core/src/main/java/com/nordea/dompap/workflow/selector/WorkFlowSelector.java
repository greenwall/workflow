package com.nordea.dompap.workflow.selector;

import com.nordea.dompap.workflow.WorkFlow;

import javax.resource.ResourceException;
import java.util.List;

/**
 * Support for selecting a ready workflow for execution and locking it to prevent other threads/applications from selecting it.
 * Ready workflows are workflows with either:
 *  - Pending method execution now or later (METHOD_STARTED==NULL or START_WHEN<=now)
 *  - or event queued and allowed to process events (possibly interrupting pending method execution).
 */
public interface WorkFlowSelector {
	
    /**
     * Selects a workflow ready for execution.
     * Ensures that only one worker picks a ready workflow.
     */
    <T> WorkFlow<T> pickReadyWorkFlow(String workflowClassName, String subType, ExecutorInfo executorInfo) throws ResourceException;
    
    /**
     * Selects a number of workflows ready for execution.
     * Ensures that only one worker picks a ready workflow.
     */
    <T> List<WorkFlow<T>> pickReadyWorkFlows(String workflowClassName, String subType, ExecutorInfo executorInfo, int maxCount) throws ResourceException;
}
