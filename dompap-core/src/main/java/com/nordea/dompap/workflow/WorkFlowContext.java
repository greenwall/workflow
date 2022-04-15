package com.nordea.dompap.workflow;

/**
 * Marker interface to support passing in context to workflow execution.
 * Declaring parameter of this interface type on workflow methods, will cause engine to pass the context on execution.
 *
 * This allows passing an context (i.e. Spring bean etc) upon engine initialization,
 * which will then be passed to workflows methods.
 */
public interface WorkFlowContext {
}
