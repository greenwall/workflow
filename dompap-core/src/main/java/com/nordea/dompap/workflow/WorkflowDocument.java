package com.nordea.dompap.workflow;

import javax.resource.ResourceException;

/**
 * The interface for generic checking document is present and getting the document.
 * @deprecated
 */
@Deprecated
public interface WorkflowDocument {
	/**
	 * Returns true if document is ready for viewing
	 */
	boolean isDocumentReady();

	/**
	 * Returns the pdf document as bytes
	 */
	byte[] loadPdf() throws ResourceException;

	/**
	 * Returns the document uris
	 * TODO Describe what parameter signable means! Only return signable documents if true and return all if false, or what ???? 
	 * @param signable
	 */
//	DocumentInfo[] getDocumentInfo(boolean signable);

	/**
	 * Returns the sign order id
	 */
//	SignOrderId getSignOrderId();
}
