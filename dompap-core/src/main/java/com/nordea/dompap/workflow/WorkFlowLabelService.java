package com.nordea.dompap.workflow;

import com.nordea.next.dompap.domain.UserId;
import org.joda.time.Interval;

import javax.resource.ResourceException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface WorkFlowLabelService {

	/**
	 * Create a label.
	 * @param createdBy must be non null
	 * @return the created Label
	 */
	WorkFlowLabel create(UserId createdBy, Date expireTime, String name, int ignoreWorkflows, String description) throws ResourceException;

	/**
	 * Stores the updated label.
	 */
	void update(WorkFlowLabel label) throws ResourceException;

	/**
	 * Loads the specified label.
	 */
	WorkFlowLabel getLabel(UUID id) throws ResourceException;

	/**
	 * Searches for labels with the given properties.
	 * @param createdBy userId to match or any if null
	 * @param creationTime Interval for creationTime or any if null.
	 * @param expireTime Interval for expireTime or any if null.
	 * @param name Exact or like matching if name contains % or any if blank.
	 */
	List<WorkFlowLabel> searchLabels(UserId createdBy, Interval creationTime, Interval expireTime, String name)
			throws ResourceException;

	/**
	 * Adds the label to the given list of workflow ids.
	 * If one of the listed workflows has another label this is overwritten by the given.
	 * @return the number of workflows the label was added to
	 */
	int addLabelToWorkFlows(WorkFlowLabel label, List<UUID> workflowIds) throws ResourceException;
	
	/**
	 * Removes the label from the given list of workflow ids.
	 */
	int removeLabelFromWorkFlows(WorkFlowLabel label, List<UUID> workflowIds) throws ResourceException;

	/**
	 * Deletes the label and removes it from any workflows.
	 */
	void deleteLabel(WorkFlowLabel label) throws ResourceException;


}
