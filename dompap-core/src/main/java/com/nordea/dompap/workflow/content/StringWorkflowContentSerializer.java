package com.nordea.dompap.workflow.content;

import com.nordea.dompap.jdbc.JdbcUtil;
import com.nordea.dompap.workflow.Workflow;

import javax.resource.ResourceException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Serializer loading workflow content as a String, by implementing ContentProperties.
 * Loading and storing to/from table WFLW_WORKFLOW_CONTENT (ID, CONTENT).  
 */
public class StringWorkflowContentSerializer extends DefaultWorkflowContentSerializer {

	// Override loadWorkFlowContent to avoid loading class for workflow.
	@Override
    public <T> T loadWorkflowContent(Connection con, Workflow<T> workflow) throws ResourceException {
        String sql = "select ID, CONTENT from WFLW_WORKFLOW_CONTENT where ID=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, workflow.getId().toString());

            byte[] content = JdbcUtil.exactQuery(ps, (rs, rowNum) -> {
                Blob b = rs.getBlob("CONTENT");
                return b.getBytes(1, (int) b.length());
            });
            String t = deserialize(content, String.class);
            workflow.setContent((T) t);
            return (T)t;
        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }

    }

    public String getWorkFlowContent(Connection con, UUID uuid) throws ResourceException {
        String sql = "select ID, CONTENT from WFLW_WORKFLOW_CONTENT where ID=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());

            byte[] content = JdbcUtil.exactQuery(ps, (rs, rowNum) -> {
                Blob b = rs.getBlob("CONTENT");
                return b.getBytes(1, (int) b.length());
            });
            return deserialize(content, String.class);
        } catch (SQLException e) {
            throw new ResourceException(e + ":" + sql, e);
        }
    }
	
	@Override
    public <T> byte[] serialize(T workflowContent) throws IOException {
		// TODO move parameter type to interface to allow String as parameter
		if (workflowContent instanceof String) { 
			return ((String) workflowContent).getBytes(StandardCharsets.UTF_8);
		} else {
			throw new IllegalArgumentException("StringWorkFlowContentSerializer can not serialize "+workflowContent.getClass());
		}
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> contentType) {
		// TODO move parameter type to interface to allow String as parameter
		if (String.class.equals(contentType)) { 
			return (T) new String(bytes, StandardCharsets.UTF_8);
		} else {
			throw new IllegalArgumentException("StringWorkFlowContentSerializer can not deserialize "+contentType);
		}
    }    
}
