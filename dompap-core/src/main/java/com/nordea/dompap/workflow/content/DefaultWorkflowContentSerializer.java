package com.nordea.dompap.workflow.content;

import com.nordea.dompap.jdbc.JdbcUtil;
import com.nordea.dompap.workflow.Workflow;

import javax.resource.ResourceException;
import java.io.*;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Default serializer uses Java built-in serialization (requiring entire content to be Serializable).
 * Loading and storing of serialized byte array is from table WFLW_WORKFLOW_CONTENT (ID, CONTENT).  
 */
public class DefaultWorkflowContentSerializer implements WorkflowContentSerializer {
	
	@Override
    public <T> T loadWorkflowContent(Connection con, Workflow<T> workflow) throws ResourceException, ClassNotFoundException, IOException {
        String sql = "select ID, CONTENT from WFLW_WORKFLOW_CONTENT where ID=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, workflow.getId().toString());

            byte[] content = JdbcUtil.exactQuery(ps, (rs, rowNum) -> {
                Blob b = rs.getBlob("CONTENT");
                return b.getBytes(1, (int) b.length());
            });
            @SuppressWarnings("unchecked")
			Class<T> contentType = (Class<T>) Class.forName(workflow.getWorkflowClassName());
            T t = deserialize(content, contentType);
            workflow.setContent(t);
            return t;
        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }

    }

	@Override
    public <T> void saveWorkflowContent(Connection con, Workflow<T> workflow) throws ResourceException {
        if (workflow == null) {
            throw new NullPointerException("No workflow instance.");
        }

        byte[] content;
        try {
            content = serialize(workflow.getContent());
        } catch (IOException e1) {
            throw new ResourceException("Workflow instance cannot be serialized", e1);
        }

        String sql = "update WFLW_WORKFLOW_CONTENT set CONTENT = ? where ID = ? ";
        ByteArrayInputStream bais = new ByteArrayInputStream(content);
        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBinaryStream(1, bais, content.length);
            ps.setString(2, workflow.getId().toString());
            ps.execute();

        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }

    }

	public <T> void archiveWorkflowContent(Connection con, Workflow<T> workflow) throws ResourceException {
        String sql = null;
		try {
			sql = "INSERT INTO WFLW_ARCHIVED_WORKFLOW_CONTENT ( ID, CONTENT ) SELECT ID, CONTENT FROM WFLW_WORKFLOW_CONTENT where ID=?";
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, workflow.getId().toString());
				ps.execute();
			}
			sql = "DELETE FROM WFLW_WORKFLOW_CONTENT where ID=?";
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, workflow.getId().toString());
				ps.execute();
			}
	    } catch (SQLException e) {
	        throw new ResourceException(e + ":" + sql, e);
	    }
	}

    public <T> byte[] serialize(T workflowContent) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(workflowContent);
        return bout.toByteArray();
    }

    @SuppressWarnings("unchecked")
	public <T> T deserialize(byte[] bytes, Class<T> contentType) throws IOException, ClassNotFoundException, ResourceException {
        ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(bytes));
        return (T)oin.readObject();
    }

	/**
	 * Inserts the content in the database
	 */
	public <T> void insertContent(Connection con, UUID id, T workflowInstance) throws SQLException, IOException {
        byte[] content = serialize(workflowInstance);
		String sql = "insert into WFLW_WORKFLOW_CONTENT (ID, CONTENT) values (?,?)";
		ByteArrayInputStream bais = new ByteArrayInputStream(content);
		try (PreparedStatement ps = con.prepareStatement(sql)) {
		    ps.setString(1, id.toString());
		    ps.setBinaryStream(2, bais, content.length);
		    ps.execute();
		}
	}
}
