package com.nordea.dompap.workflow;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Timer to log time between construction and logTime calls. 
 * Allows selecting timing information from WorkFlowTimer log entries or to push timings to statistics.
 */
@Slf4j
public class WorkflowTimer {
    private final String workflowClassName;
	private final String methodName;
	private final UUID id;
	private final long startTime;
	
	public WorkflowTimer(Workflow<?> workflow, Method step) {
		this.workflowClassName = workflow.getWorkflowClassName();
		this.methodName = step.getName();
		this.id = workflow.getId();
		this.startTime = System.currentTimeMillis();
	}

	public WorkflowTimer(Workflow<?> workflow) {
		this.workflowClassName = workflow.getWorkflowClassName();
		this.methodName = workflow.getMethodName();
		this.id = workflow.getId();
		this.startTime = System.currentTimeMillis();
	}

	/**
	 * Allow timer before knowing which workflow instance to execute.
	 */
	public WorkflowTimer(String workflowClassName) {
		this.workflowClassName = workflowClassName;
		this.methodName = null;
		this.id = null;
		this.startTime = System.currentTimeMillis();
	}
	
	public void logTime(String name) {
        log.info("{}:id={}, {}:{}, time={}", workflowClassName, id, name, methodName, System.currentTimeMillis()-startTime);
	}

	/**
	 * When workflow instance is only known after timer creation it must be provided when logging. 
	 */
	public void logTime(String name, Workflow<?> workflow) {
        log.info("{}:id={}, {}:{}, time={}",
        		workflowClassName, 
        		workflow!=null ? workflow.getId(): null, 
				name, 
				workflow!=null ? workflow.getMethodName(): null, 
				System.currentTimeMillis()-startTime);    		
	}
	
	public static void logTimeFirstNonNull(String name, WorkflowTimer... timers) {
		for (WorkflowTimer timer: timers) {
			if (timer!=null) {
				timer.logTime(name);
				return;
			}
		}
	}
}

