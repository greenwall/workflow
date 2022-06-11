package com.nordea.dompap.workflow;

import javax.resource.ResourceException;
import java.util.List;

/**
 * Status service for counting workflows of different classes, methods in time periods.
 */
public interface WorkflowStatusService {

	List<WorkflowMethodCount> getWorkflowStatus(int[] periods) throws ResourceException;

	List<WorkflowMethodCount> getWorkflowStatus(int[] periods, String workflowClass) throws ResourceException;

	List<WorkflowMethodCount> getWorkflowStatus(WorkflowStatusQuery query) throws ResourceException;

}
