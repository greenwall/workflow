package com.nordea.dompap.workflow;

import com.nordea.dompap.workflow.content.WorkflowContentSerializer;
import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.UserId;
import org.joda.time.Interval;

import javax.resource.ResourceException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface WorkflowService {

	<T> Workflow<T> getWorkFlow(UUID uuid, Class<T> interfaceClass) throws ResourceException;

	<T> Workflow<T> getWorkFlow(UUID uuid) throws ResourceException;

	<T> Workflow<T> findWorkFlowByExternalKey(String externalKey) throws ResourceException;

//	String getWorkflowContent(UUID uuid, String dataSource) throws ResourceException;
	
	/**
	 * @deprecated Use loadWorkFlowContent(workflow, contentSerializer) or use {@link WorkflowManager#getWorkFlow(UUID)} loads the workflow and content.
	 */
	<T> T loadWorkFlowContent(Workflow<T> workflow) throws ResourceException, ClassNotFoundException, IOException;

    <T> T loadWorkFlowContent(Workflow<T> workflow, WorkflowContentSerializer contentSerializer) throws ResourceException, ClassNotFoundException, IOException;
	
	<T> WorkflowController loadWorkFlowController(Workflow<T> workflow) throws ResourceException, ClassNotFoundException, IOException;

	<T> void saveWorkFlowController(Workflow<T> workflow, WorkflowController controller) throws ResourceException;
	
	<T> Workflow<T> insertWorkFlow(WorkflowBuilder<T> workflowBuilder) throws ResourceException, IOException;

	/**
	 * @deprecated Use insertWorkFlow(WorkFlowBuilder) 
	 */
	<T> Workflow<T> insertWorkFlow(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId, T workflowInstance, Method method, Date startWhen, Metadata metadata, WorkflowController controller) throws ResourceException, IOException;

	/**
	 * @deprecated Use insertWorkFlow(WorkFlowBuilder) 
	 */
	<T> Workflow<T> insertWorkFlow(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId, T workflowInstance, String subType, Method method, Date startWhen, Metadata metadata, WorkflowController controller) throws ResourceException, IOException;

	/**
	 * @deprecated Use insertWorkFlow(WorkFlowBuilder) 
	 */
	<T> Workflow<T> insertWorkFlow(
			UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId, String workflowClassName,
			WorkflowContentSerializer contentSerializer, T workflowInstance, String methodName,
			Date startWhen, Metadata metadata, WorkflowController controller) throws ResourceException, IOException;

	/**
	 * Allow creation of a workflow without requiring class to be available.
	 * TODO Generic parameter T makes no sense, since we don't have the actual class any way.
	 * @deprecated Use insertWorkFlow(WorkFlowBuilder)
	 */
	<T> Workflow<T> insertWorkFlow(
			UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId, String workflowClassName, String subType,
			WorkflowContentSerializer contentSerializer, T workflowInstance, String methodName,
			Date startWhen, Metadata metadata, WorkflowController controller) throws ResourceException, IOException;
	
	/**
	 * @deprecated use {@link WorkflowService}{@link #saveWorkFlowContent(Workflow, WorkflowContentSerializer)} with serializer
	 */
	<T> void saveWorkFlowContent(Workflow<T> workflow) throws ResourceException;

	<T> void saveWorkFlowContent(Workflow<T> workflow, WorkflowContentSerializer contentSerializer) throws ResourceException;
	
	<T> Metadata loadWorkFlowMetadata(Workflow<T> workflow) throws ResourceException;
	
	<T> void saveWorkFlowMetadata(Workflow<T> workflow, Metadata metadata) throws ResourceException;

	<T> void updateWorkFlow(Workflow<T> workflow, String methodName, Date startWhen, Date methodStarted, Date methodEnded, Throwable methodException, Metadata metadata) throws ResourceException;

	<T> void updateWorkFlow(Workflow<T> workflow, Method method, Date startWhen, Date methodStarted, Date methodEnded, Throwable methodException, Metadata metadata) throws ResourceException;
	
	<T> void updateWorkFlow(Workflow<T> workflow, String methodName, Date startWhen, Date methodStarted, Date methodEnded, Throwable methodException, Metadata metadata, boolean processEvents) throws ResourceException;

	<T> void updateWorkFlow(Workflow<T> workflow, Method method, Date startWhen, Date methodStarted, Date methodEnded, Throwable methodException, Metadata metadata, boolean processEvents) throws ResourceException;

	<T> void stepExecutionCompleted(Workflow<T> workflow, Method method, Date startWhen, Date methodStarted, Date methodEnded, Throwable methodException, Metadata metadata, boolean processEvents) throws ResourceException;

	<T> void queueEvent(Workflow<T> workflow, UUID eventId) throws ResourceException;
	
	/**
	 * Selects a non-executing workflow and marks it as executing (so it's not picked again before ready). 
	 */
	<T> Workflow<T> pickReadyWorkFlow(String workflowClassName, String subType) throws ResourceException;
	
	/**
	 * Selects a number of non-executing workflow, marks them all as executing and returns them.
	 * @param maxCount Max number of workflows to execute - must be 1 or more
	 */
    <T> List<Workflow<T>> pickReadyWorkFlows(String workflowClassName, String subType, int maxCount) throws ResourceException;
	
	@SuppressWarnings("rawtypes")
	List<Workflow> getWorkFlows(UserId userId, BranchId branchId, Interval creationTime, Integer startRow, Integer maxRows) throws ResourceException;

	/**
	 * 
	 * @deprecated use {@link WorkflowService#searchWorkFlowsAggregated}
	 */
    WorkflowSearchResult searchWorkFlows(WorkflowSearch search, Integer startRow, Integer maxRows) throws ResourceException;
    
    /**
     * Selects workflows with associated metadata aggregated in a column
     * @param startRow The first workflow row to fetch (used for paging)
     * @param maxRows Max number of workflows to fetch
     * @param totalRows The total number of rows. Null when first page is read, and filled out in subsequent pages
     * @return {@link WorkflowSearchResult}
     */
	WorkflowSearchResult searchWorkFlowsAggregated(WorkflowSearch search, Integer startRow, Integer maxRows, Integer totalRows) throws ResourceException;
	/**
	 * 
	 * @param search Search criterias
	 * @param startRow first index
	 * @param maxRows max records
	 * @param supportBackwardCompatibility this flag will decide whether search records contains those records as well where request domain don't have any value
	 */
	WorkflowSearchResult searchWorkFlows(WorkflowSearch search, Integer startRow, Integer maxRows, boolean supportBackwardCompatibility) throws ResourceException;

	/**
	 * Set label to all workflows matching the search criterias.
	 * Updating with null clears the label on the workflows.
	 * It is safe to update workflows with label during processing, since only the label will be affected. 
	 * @return the number of workflows updated with the label.
	 */
	int updateWorkFlowLabel(WorkflowSearch search, WorkflowLabel label) throws ResourceException;
	
	/**
	 * Update the external key allowing retrieving a workflow instance.
	 * The reason for updating the external key is that it may initially be unavailable to be generated by a step in workflow and used for later retrieval.
	 */
	void updateWorkFlow(UUID workflowId, String externalKey) throws ResourceException;
	
	<T> List<WorkflowStepResult> getWorkFlowHistory(Workflow<T> workflow) throws ResourceException;

	/**
	 * @deprecated use WorkFlowStatusService
	 */
	List<WorkflowMethodCount> getWorkflowStatus(int[] periodsInMinutes) throws ResourceException;
	/**
	 * @deprecated use WorkFlowStatusService
	 */
	List<WorkflowMethodCount> getWorkflowStatus(int[] periods, String workflowClass) throws ResourceException;
	/**
	 * @deprecated use WorkFlowStatusService
	 */
    List<WorkflowMethodCount> getWorkflowStatus(WorkflowStatusQuery query) throws ResourceException;
	
	/**
	 * Archives a workflow, so afterwards it will not be searchable nor executable. 
	 * It will however remain in archive tables until deleted.
	 */
	<T> void archiveWorkFlow(Workflow<T> workflow) throws ResourceException;

	/**
	 * Method to resume either a list of workflowIds or based on a query
	 * @param search The query for searching the workflows to resume.
	 * @param workFlowIdList The list of workflowIds. 	
	 * @param methodName The methodname to resume in
	 * @return	The number of updated workflows or -1 if update failed.
	 */
	int resumeWorkFlows(WorkflowSearch search, List<String> workFlowIdList, String methodName) throws ResourceException;

}
