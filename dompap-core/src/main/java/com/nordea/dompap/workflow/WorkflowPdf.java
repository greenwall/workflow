package com.nordea.dompap.workflow;

import javax.resource.ResourceException;

/**
 * @deprecated Use WorkflowDocument
 * The interface for generic checking document is present and getting the document.
 * @author g20446
 */
@Deprecated
public interface WorkflowPdf {
	/**
	 * Returns true if document is ready for viewing
	 */
	boolean isDocumentReady();

	/**
	 * Returns the pdf document as bytes or loads it from an archive using stored document key/uri 
	 */
	byte[] loadPdf() throws ResourceException;
}
