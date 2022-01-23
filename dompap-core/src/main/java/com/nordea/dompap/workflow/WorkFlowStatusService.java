package com.nordea.dompap.workflow;

import javax.resource.ResourceException;
import java.util.List;

/**
 * Status service for counting workflows of different classes, methods in time periods.
 */
public interface WorkFlowStatusService {

	List<WorkFlowMethodCount> getWorkflowStatus(int[] periods) throws ResourceException;

	List<WorkFlowMethodCount> getWorkflowStatus(int[] periods, String workflowClass) throws ResourceException;

	List<WorkFlowMethodCount> getWorkflowStatus(WorkFlowStatusQuery query) throws ResourceException;

}
