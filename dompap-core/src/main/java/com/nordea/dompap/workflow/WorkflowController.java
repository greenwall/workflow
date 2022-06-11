package com.nordea.dompap.workflow;

import javax.resource.ResourceException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * Each WorkFlow has it's own WorkFlowController instance.
 * The Controller gets invoked before and after execution of a method in the workflow.
 * The Controller state is read (deserialized) before invoking onStart.
 * The state is stored (serialized) after returning from onComplete or onFail
 *  
 * The Controller must be Serializable in order for its state to be persisted.
 */
public interface WorkflowController extends Serializable {
	
	/**
	 * Invoked prior to invoking a method on the workflow.
	 * Return null if no state was changed in Controller - return instance if state was changed.
	 * @param methodToBeExecuted to be executed.
	 */
	<T> WorkflowController onStart(Workflow<T> workflow, Method methodToBeExecuted) throws ResourceException ;
	
	/**
	 * Invoked after successfully executing a method in the workflow.
	 * Return null if no state was changed in Controller - return instance if state was changed.
	 */
	<T> WorkflowController onComplete(Workflow<T> workflow, Method methodExecuted, Method nextMethod, Date startWhen) throws ResourceException;
	
	/**
	 * Invoked after exception from executing a method in the workflow.
	 * Return null if no state was changed in Controller - return instance if state was changed.
	 */
	<T> WorkflowController onFail(Workflow<T> workflow, Method methodExecuted, Throwable exception) throws ResourceException;
}
