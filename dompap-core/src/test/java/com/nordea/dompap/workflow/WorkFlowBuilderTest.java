package com.nordea.dompap.workflow;

import com.nordea.dompap.config.WorkFlowContextSpring;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.resource.ResourceException;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = WorkFlowContextSpring.class)
@ActiveProfiles("workflowbuildertest")
public class WorkFlowBuilderTest extends TestWithMemoryDB {

	@Autowired
	WorkflowManager workFlowManager;

	@Test
	public void testString() throws IOException, ResourceException, ClassNotFoundException {

		UUID id = UUID.randomUUID();
		String content = getJsonRequest();      
		
        WorkflowBuilder<String> builder = new WorkflowBuilder<String>()
        		.id(id)
//        		.userId(new UserId("G97435"))
//        		.branchId(new BranchId("0000"))
        		.workflowClassName("com.acme.SomeUnknownClass")
        		.workflow(content)
        		.methodName("start");
		
		Workflow<String> wf = workFlowManager.create(builder);
		
		assertEquals(id, wf.getId(), "Workflow ID should not change");
		
		Workflow<Object> wf2 = workFlowManager.getWorkFlow(wf.getId());

		assertNotNull(wf2);
		assertEquals(wf.getId(), wf2.getId());
		
	}

	@Test
	public void duplicateExternalKeyDetection() throws IOException, ResourceException {

		String content = getJsonRequest();

		WorkflowBuilder<String> builder1 = new WorkflowBuilder<String>()
				.id(UUID.randomUUID())
				.externalKey("this-is-unique")
				.workflowClassName("com.acme.SomeUnknownClass")
				.workflow(content)
				.methodName("irrelevant");

		Workflow<String> wf1 = workFlowManager.create(builder1);

		WorkflowBuilder<String> builder2 = new WorkflowBuilder<String>()
				.id(UUID.randomUUID())
				.externalKey("this-is-unique")
				.workflowClassName("com.acme.SomeUnknownClass")
				.workflow(content)
				.methodName("irrelevant");

		assertThrows(ResourceException.class, () -> {
			Workflow<String> wf2 = workFlowManager.create(builder2);
		});

	}

	@Test
	public void nullExternalKeyAllowed() throws IOException, ResourceException {

		String content = getJsonRequest();

		WorkflowBuilder<String> builder1 = new WorkflowBuilder<String>()
				.id(UUID.randomUUID())
				.externalKey(null)
				.workflowClassName("com.acme.SomeUnknownClass")
				.workflow(content)
				.methodName("irrelevant");

		Workflow<String> wf1 = workFlowManager.create(builder1);

		WorkflowBuilder<String> builder2 = new WorkflowBuilder<String>()
				.id(UUID.randomUUID())
				.externalKey(null)
				.workflowClassName("com.acme.SomeUnknownClass")
				.workflow(content)
				.methodName("irrelevant");

		Workflow<String> wf2 = workFlowManager.create(builder2);

		assertNotEquals(wf1.getId(), wf2.getId());
	}

    private String getJsonRequest() throws IOException {
        return IOUtils.toString(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("com/nordea/dompap/workflow/workflow-A.json")));
    }

}
