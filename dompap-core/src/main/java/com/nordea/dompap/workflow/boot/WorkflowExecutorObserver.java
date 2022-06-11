package com.nordea.dompap.workflow.boot;

import com.nordea.dompap.workflow.Workflow;

/**
 * Observer invoked before and after execution of the given workflow
 */
public interface WorkflowExecutorObserver {

    <T> void preExecute(Workflow<T> workFlow);
    <T> void postExecute(Workflow<T> workFlow);

}
