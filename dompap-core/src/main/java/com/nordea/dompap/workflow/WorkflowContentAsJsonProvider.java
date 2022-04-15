package com.nordea.dompap.workflow;

import javax.resource.ResourceException;

/**
 * The interface for getting the workflow content in a formatted way
 */
public interface WorkflowContentAsJsonProvider {
    /**
     * Returns the content as a json pretty formatted string for showing in a text area.
     */
    String getContent() throws ResourceException;
}
