package com.nordea.dompap.workflow;

import com.nordea.dompap.workflow.util.WorkflowProcessingStatus;
import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.UserId;

import javax.resource.ResourceException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface WorkFlowManager {

    /**
	 * Stores a new workflow instance and executes this in the current thread before returning.
	 */
	<T> WorkFlow<T> startImmediate(WorkFlowBuilder<T> builder) throws ResourceException, IOException;

    /**
     * @deprecated Use startImmediate(WorkFlowBuilder)
     */
	<T> WorkFlow<T> startImmediate(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId, T content, Method entry) throws ResourceException, IOException;
	
	/**
	 * Stores a new workflow instance marking it ready and returns without executing it. 
	 * Threads/background jobs will call pickAndExecute to obtain and execute any ready workflow instance (one at a time). 
	 */
	<T> WorkFlow<T> start(WorkFlowBuilder<T> builder) throws ResourceException, IOException;

    /**
     * @deprecated Use start(WorkFlowBuilder)
     */
	<T> WorkFlow<T> start(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId, T content, String subType, Method entry, Date startWhen, Metadata metadata) throws ResourceException, IOException;

	/**
	 * Create a workflow without knowing the class, accepting any workflow class name and starting method. 
	 */
	<T> WorkFlow<T> create(WorkFlowBuilder<T> builder) throws ResourceException, IOException;

    /**
     * @deprecated Use create(WorkFlowBuilder)
     */
	<T> WorkFlow<T> create(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId, String workflowClassName, String subType,
			T content, String startMethod, Date startWhen, Metadata metadata)
			throws ResourceException, IOException;
	
	/**
	 * Picks a ready workflow instance (starttime==null) and executes it until a method returns null (or throws exception).
	 * This instance will then be updated with end time and any method exception. 
	 * So instances with endtime != null and exception ==null have run to completion or is waiting for external resume.
	 */
	WorkflowProcessingStatus pickAndExecute(String workflowClassName, String subType) throws ResourceException;

	/**
	 * Picks a workflow, which is ready for execution, when returned, it is marked as executing.
	 * 
	 * @param workflowClassName Workflow classname
	 * @param subType Subtype
	 * @return workflow, or null if no workflow was ready
	 */
	<T> WorkFlow<T> pick(String workflowClassName, String subType) throws ResourceException;
	
	/**
	 * Picks a number of workflows, and marks them as executing
	 * 
	 * @param workflowClassName Workflow classname
	 * @param subType Subtype
	 * @param maxCount Maximum number of workflows to pickup
	 * @return workflow, or null if no workflow was ready
	 */
	<T> List<WorkFlow<T>> pick(String workflowClassName, String subType, int maxCount) throws ResourceException;
	
	/**
	 * Executes a previously picked workflow
	 * @param workflow The workflow to execute
	 */
	<T> WorkflowProcessingStatus execute(WorkFlow<T> workflow) throws ResourceException;
	
	
	/**
	 * @see WorkFlowManager#resumeAt(WorkFlow, String, Date)
	 */
	<T> WorkFlow<T> resumeAt(WorkFlow<T> workflow, Method method, Date startWhen) throws ResourceException;

	/**
	 * Resume the given workflow at the given method, scheduling at startWhen or now if null. 
	 */
	<T> WorkFlow<T> resumeAt(WorkFlow<T> workflow, String method, Date startWhen) throws ResourceException;

	/**
	 * Queue an event for the workflow, when it's ready to process it. 
	 */
	<T> void queueEvent(WorkFlow<T> workflow, UUID eventId) throws ResourceException;
	
	/**
	 * Move workflow "as if" method had just been executed - without executing it. 
	 * Intended for moving workflows to correct state so status querying does not count it in wrong state.  
	 */
	<T> WorkFlow<T> moveAfter(WorkFlow<T> workflow, String method) throws ResourceException;
	
    /**
     * Used when method failed and controller decides to retry.
     * In contrast to resumeAt the exception from the failing method is kept, to allow status querying on methods being retried.
     * Consider to leave the exception unchanged instead of writing the same exception again to database.
     */
	<T> WorkFlow<T> retryAt(WorkFlow<T> workflow, Method method, Date startWhen, Throwable methodException) throws ResourceException;

	<T> WorkFlow<T> getWorkFlow(UUID uuid) throws ResourceException, ClassNotFoundException, IOException;

	<T> WorkFlow<T> getWorkFlowOnly(UUID uuid) throws ResourceException;
	
	void updateExternalKey(UUID workflowId, String externalKey) throws ResourceException;
	
	<T> WorkFlow<T> findWorkFlowByExternalKey(String externalKey) throws ResourceException;
}
