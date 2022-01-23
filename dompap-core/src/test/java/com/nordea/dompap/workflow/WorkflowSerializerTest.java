package com.nordea.dompap.workflow;

import com.nordea.dompap.workflow.content.GsonWorkFlowContentSerializer;
import com.nordea.next.dompap.workflow.Activity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


public class WorkflowSerializerTest {

	public class MyActivityWorkflow extends Activity {
		private static final long serialVersionUID = 2349431731201273942L;
		@SuppressWarnings("unused")
		private String value = "Some value";
	}

	public class MySerializer extends GsonWorkFlowContentSerializer {
		@Override
		public <T> byte[] serialize(T workflowContent) throws IOException {
			return super.serialize(workflowContent);
		}
		
		@Override
		public <T> T deserialize(byte[] bytes, Class<T> contentType) throws IOException, ClassNotFoundException {
			return super.deserialize(bytes, contentType);
		}
	}
	
	@Test
	public void GsonSerialize() {
		MyActivityWorkflow awf = new MyActivityWorkflow();
		awf.setSignableDocument("docURI", "docTitle", "docFileType");
		MySerializer ser = new MySerializer();
		try {
			byte[] bytes = ser.serialize(awf);
			System.out.println(new String(bytes));
			MyActivityWorkflow nawf = ser.deserialize(bytes, MyActivityWorkflow.class);
			assertEquals(awf, nawf, "Er ikke ens");
		} catch (IOException | ClassNotFoundException e) {
			fail(e.getMessage());
		}
	}
}
