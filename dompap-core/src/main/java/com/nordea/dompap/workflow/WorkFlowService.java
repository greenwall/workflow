package com.nordea.dompap.workflow;

import com.nordea.dompap.workflow.content.WorkFlowContentSerializer;
import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.UserId;
import org.joda.time.Interval;

import javax.resource.ResourceException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface WorkFlowService {

	<T> WorkFlow<T> getWorkFlow(UUID uuid, Class<T> interfaceClass) throws ResourceException;

	<T> WorkFlow<T> getWorkFlow(UUID uuid) throws ResourceException;

	<T> WorkFlow<T> findWorkFlowByExternalKey(String externalKey) throws ResourceException;

//	String getWorkflowContent(UUID uuid, String dataSource) throws ResourceException;
	
	/**
	 * @deprecated Use loadWorkFlowContent(workflow, contentSerializer) or use {@link WorkFlowManager#getWorkFlow(UUID)} loads the workflow and content. 
	 */
	<T> T loadWorkFlowContent(WorkFlow<T> workflow) throws ResourceException, ClassNotFoundException, IOException;

    <T> T loadWorkFlowContent(WorkFlow<T> workflow, WorkFlowContentSerializer contentSerializer) throws ResourceException, ClassNotFoundException, IOException;    	    	
	
	<T> WorkFlowController loadWorkFlowController(WorkFlow<T> workflow) throws ResourceException, ClassNotFoundException, IOException;

	<T> void saveWorkFlowController(WorkFlow<T> workflow, WorkFlowController controller) throws ResourceException;
	
	<T> WorkFlow<T> insertWorkFlow(WorkFlowBuilder<T> workflowBuilder) throws ResourceException, IOException;

	/**
	 * @deprecated Use insertWorkFlow(WorkFlowBuilder) 
	 */
	<T> WorkFlow<T> insertWorkFlow(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId, T workflowInstance, Method method, Date startWhen, Metadata metadata, WorkFlowController controller) throws ResourceException, IOException;

	/**
	 * @deprecated Use insertWorkFlow(WorkFlowBuilder) 
	 */
	<T> WorkFlow<T> insertWorkFlow(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId, T workflowInstance, String subType, Method method, Date startWhen, Metadata metadata, WorkFlowController controller) throws ResourceException, IOException;

	/**
	 * @deprecated Use insertWorkFlow(WorkFlowBuilder) 
	 */
	<T> WorkFlow<T> insertWorkFlow(
			UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId, String workflowClassName, 
			WorkFlowContentSerializer contentSerializer, T workflowInstance, String methodName, 
			Date startWhen, Metadata metadata, WorkFlowController controller) throws ResourceException, IOException;

	/**
	 * Allow creation of a workflow without requiring class to be available.
	 * TODO Generic parameter T makes no sense, since we don't have the actual class any way.
	 * @deprecated Use insertWorkFlow(WorkFlowBuilder)
	 */
	<T> WorkFlow<T> insertWorkFlow(
			UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId, String workflowClassName, String subType, 
			WorkFlowContentSerializer contentSerializer, T workflowInstance, String methodName, 
			Date startWhen, Metadata metadata, WorkFlowController controller) throws ResourceException, IOException;
	
	/**
	 * @deprecated use {@link WorkFlowService}{@link #saveWorkFlowContent(WorkFlow, WorkFlowContentSerializer)} with serializer 
	 */
	<T> void saveWorkFlowContent(WorkFlow<T> workflow) throws ResourceException;

	<T> void saveWorkFlowContent(WorkFlow<T> workflow, WorkFlowContentSerializer contentSerializer) throws ResourceException;
	
	<T> Metadata loadWorkFlowMetadata(WorkFlow<T> workflow) throws ResourceException;
	
	<T> void saveWorkFlowMetadata(WorkFlow<T> workflow, Metadata metadata) throws ResourceException;

	<T> void updateWorkFlow(WorkFlow<T> workflow, String methodName, Date startWhen, Date methodStarted, Date methodEnded, Throwable methodException, Metadata metadata) throws ResourceException;

	<T> void updateWorkFlow(WorkFlow<T> workflow, Method method, Date startWhen, Date methodStarted, Date methodEnded, Throwable methodException, Metadata metadata) throws ResourceException;
	
	<T> void updateWorkFlow(WorkFlow<T> workflow, String methodName, Date startWhen, Date methodStarted, Date methodEnded, Throwable methodException, Metadata metadata, boolean processEvents) throws ResourceException;

	<T> void updateWorkFlow(WorkFlow<T> workflow, Method method, Date startWhen, Date methodStarted, Date methodEnded, Throwable methodException, Metadata metadata, boolean processEvents) throws ResourceException;

	<T> void stepExecutionCompleted(WorkFlow<T> workflow, Method method, Date startWhen, Date methodStarted, Date methodEnded, Throwable methodException, Metadata metadata, boolean processEvents) throws ResourceException;

	<T> void queueEvent(WorkFlow<T> workflow, UUID eventId) throws ResourceException;
	
	/**
	 * Selects a non-executing workflow and marks it as executing (so it's not picked again before ready). 
	 */
	<T> WorkFlow<T> pickReadyWorkFlow(String workflowClassName, String subType) throws ResourceException;
	
	/**
	 * Selects a number of non-executing workflow, marks them all as executing and returns them.
	 * @param maxCount Max number of workflows to execute - must be 1 or more
	 */
    <T> List<WorkFlow<T>> pickReadyWorkFlows(String workflowClassName, String subType, int maxCount) throws ResourceException;
	
	@SuppressWarnings("rawtypes")
	List<WorkFlow> getWorkFlows(UserId userId, BranchId branchId, Interval creationTime, Integer startRow, Integer maxRows) throws ResourceException;

	/**
	 * 
	 * @deprecated use {@link WorkFlowService#searchWorkFlowsAggregated}
	 */
    WorkFlowSearchResult searchWorkFlows(WorkFlowSearch search, Integer startRow, Integer maxRows) throws ResourceException;  
    
    /**
     * Selects workflows with associated metadata aggregated in a column
     * @param startRow The first workflow row to fetch (used for paging)
     * @param maxRows Max number of workflows to fetch
     * @param totalRows The total number of rows. Null when first page is read, and filled out in subsequent pages
     * @return {@link WorkFlowSearchResult}
     */
	WorkFlowSearchResult searchWorkFlowsAggregated(WorkFlowSearch search, Integer startRow, Integer maxRows, Integer totalRows) throws ResourceException;	
	/**
	 * 
	 * @param search Search criterias
	 * @param startRow first index
	 * @param maxRows max records
	 * @param supportBackwardCompatibility this flag will decide whether search records contains those records as well where request domain don't have any value
	 */
	WorkFlowSearchResult searchWorkFlows(WorkFlowSearch search, Integer startRow, Integer maxRows,boolean supportBackwardCompatibility) throws ResourceException;   

	/**
	 * Set label to all workflows matching the search criterias.
	 * Updating with null clears the label on the workflows.
	 * It is safe to update workflows with label during processing, since only the label will be affected. 
	 * @return the number of workflows updated with the label.
	 */
	int updateWorkFlowLabel(WorkFlowSearch search, WorkFlowLabel label) throws ResourceException;
	
	/**
	 * Update the external key allowing retrieving a workflow instance.
	 * The reason for updating the external key is that it may initially be unavailable to be generated by a step in workflow and used for later retrieval.
	 */
	void updateWorkFlow(UUID workflowId, String externalKey) throws ResourceException;
	
	<T> List<WorkFlowStepResult> getWorkFlowHistory(WorkFlow<T> workflow) throws ResourceException;

	/**
	 * @deprecated use WorkFlowStatusService
	 */
	List<WorkFlowMethodCount> getWorkflowStatus(int[] periodsInMinutes) throws ResourceException;
	/**
	 * @deprecated use WorkFlowStatusService
	 */
	List<WorkFlowMethodCount> getWorkflowStatus(int[] periods, String workflowClass) throws ResourceException;
	/**
	 * @deprecated use WorkFlowStatusService
	 */
    List<WorkFlowMethodCount> getWorkflowStatus(WorkFlowStatusQuery query) throws ResourceException;
	
	/**
	 * Archives a workflow, so afterwards it will not be searchable nor executable. 
	 * It will however remain in archive tables until deleted.
	 */
	<T> void archiveWorkFlow(WorkFlow<T> workflow) throws ResourceException;

	/**
	 * Method to resume either a list of workflowIds or based on a query
	 * @param search The query for searching the workflows to resume.
	 * @param workFlowIdList The list of workflowIds. 	
	 * @param methodName The methodname to resume in
	 * @return	The number of updated workflows or -1 if update failed.
	 */
	int resumeWorkFlows(WorkFlowSearch search, List<String> workFlowIdList, String methodName) throws ResourceException;

}
