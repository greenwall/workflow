package com.nordea.dompap.workflow;

import com.nordea.next.dompap.workflow.Activity;
import com.nordea.next.dompap.workflow.annotation.SerializerClass;
import org.junit.jupiter.api.Test;

import static org.springframework.test.util.AssertionErrors.*;

public class WorkflowSerializerAnnotationTest {
	public class MyActivityWorkflow extends Activity {
		private static final long serialVersionUID = 2349431731201273942L;
	}
	public class MyWorkFlow extends Object {
	}
	
	@Test
	public void AnnotationFound() {
		Class<? super MyActivityWorkflow> superclass = MyActivityWorkflow.class.getSuperclass();
		assertNotNull("No super class extended", superclass);
		assertEquals("Not extended Activity class", Activity.class, superclass);
		assertTrue("Annotation not found", superclass.isAnnotationPresent(SerializerClass.class));
	}
	
	@Test
	public void AnnotationNotFound() {
		Class<? super MyWorkFlow> superclass = MyWorkFlow.class.getSuperclass();
		assertNotEquals("Extended Activity class", Activity.class, superclass);
		if (superclass != null) {
			assertTrue("Annotation found", !superclass.isAnnotationPresent(SerializerClass.class));
		}
	}
	
}
