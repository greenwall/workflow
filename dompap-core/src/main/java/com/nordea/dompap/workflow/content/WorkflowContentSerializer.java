package com.nordea.dompap.workflow.content;

import com.nordea.dompap.workflow.Workflow;

import javax.resource.ResourceException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Interface for saving and loading workflow content.
 * If workflow content is to be stored/loaded from other tables implement this and configure for that workflow.
 */
public interface WorkflowContentSerializer {

	/**
	 * Loads the workflow content from the given connection.
	 */
    <T> T loadWorkflowContent(Connection connection, Workflow<T> workflow) throws ResourceException, ClassNotFoundException, IOException;

    /**
     * Save the workflow content using the given connection, making it part of the same transaction.
     */
    <T> void saveWorkflowContent(Connection connection, Workflow<T> workflow) throws ResourceException;
	
    /**
     * Archives the workflow content using the given connection, making it part of the same transaction.
     */
	<T> void archiveWorkflowContent(Connection con, Workflow<T> workflow) throws ResourceException;

	/**
	 * Inserts content of workflow
	 */
	<T> void insertContent(Connection con, UUID id, T workflowInstance) throws SQLException, IOException;
}