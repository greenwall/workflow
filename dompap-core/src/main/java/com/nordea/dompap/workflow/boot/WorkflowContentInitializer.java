package com.nordea.dompap.workflow.boot;

/**
 * After loading workflow content a WorkFlowContentInitializer allows customizable initialization.
 * This may be used to inject dependencies into workflow content instances.
 * @param <T> Class of workflow content
 */
public interface WorkflowContentInitializer<T> {
    void initialize(T content);
}
