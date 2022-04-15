package com.nordea.dompap.workflow.content;

import org.joda.time.DateTime;

import javax.resource.ResourceException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Serializer requiring workflow content to be exposed as properties, by implementing ContentProperties.
 * Further workflow content class must have an empty constructor.
 * Loading and storing of corresponding properties is from table WFLW_WORKFLOW_CONTENT (ID, CONTENT).  
 */
public class PropertiesWorkFlowContentSerializer extends DefaultWorkFlowContentSerializer {

	/** 
	 * WorkflowContent implements to allow loading and storing as properties
	 */
	public interface ContentProperties {
		/**
		 * Called by serializer to retrieve properties when workflow is stored. 
		 */
		Properties getProperties();

		/**
		 * Called by serializer when properties have been loaded. 
		 */
		void setProperties(Properties props);
	}
	
    public <T> byte[] serialize(T workflowContent) throws IOException {
    	if (workflowContent instanceof ContentProperties) {
    		Properties props = ((ContentProperties)workflowContent).getProperties();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            String comments = "Stored on "+DateTime.now();
    		props.store(bout, comments);
            return bout.toByteArray();
    	} else {
    		throw new IllegalArgumentException(
				"Workflow of type "+workflowContent.getClass().getSimpleName()
				+" needs to implement ContentProperties in order for "+PropertiesWorkFlowContentSerializer.class.getSimpleName()
				+" to be able to serialize it.");
    	}
    }

    public <T> T deserialize(byte[] bytes, Class<T> contentType) throws ClassNotFoundException, IOException, ResourceException {    	
        try {
        	Properties props = new Properties();
        	props.load(new ByteArrayInputStream(bytes));
        
        	T content = contentType.newInstance();    	
        	if (content instanceof ContentProperties) {
        		((ContentProperties) content).setProperties(props);
                return content;        		
        	} else {
        		throw new IllegalArgumentException(
        				"Workflow of type "+content.getClass().getSimpleName()
        				+" needs to implement ContentProperties in order for "+PropertiesWorkFlowContentSerializer.class.getSimpleName()
        				+" to be able to deserialize it.");
        	}
        } catch (InstantiationException | IllegalAccessException e) {
        	throw new ResourceException(e);
		}
	}
}
