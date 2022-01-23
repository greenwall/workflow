package com.nordea.dompap.workflow;

import javax.resource.ResourceException;

/**
 * The interface for getting the workflow content in a formatted way
 * @author g20446
 */
public interface WorkflowContentAsStringProvider {
    /**
     * Returns the content as a formatted string for showing in a text area.
     */
    String getContent() throws ResourceException;
}
