package com.nordea.dompap.workflow;

import com.nordea.dompap.config.WorkFlowContextSpring;
import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.UserId;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.resource.ResourceException;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = WorkFlowContextSpring.class)
@ActiveProfiles("workflowbuildertest")
public class WorkFlowBuilderTest extends TestWithMemoryDB {

	@Autowired
	WorkFlowManager workFlowManager;

	@Test
	public void testString() throws IOException, ResourceException, ClassNotFoundException {

		UUID id = UUID.randomUUID();
		String content = getJsonRequest();      
		
        WorkFlowBuilder<String> builder = new WorkFlowBuilder<String>()
        		.id(id)
        		.userId(new UserId("G97435"))
        		.branchId(new BranchId("0000"))
        		.workflowClassName("com.acme.SomeUnknownClass")
        		.workflow(content)
        		.methodName("start");
		
		WorkFlow<String> wf = workFlowManager.create(builder);
		
		Assertions.assertEquals(id, wf.getId(), "Workflow ID should not change");
		
		WorkFlow<Object> wf2 = workFlowManager.getWorkFlow(wf.getId());

		assertNotNull(wf2);
		
	}

    private String getJsonRequest() throws IOException {
        return IOUtils.toString(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("com/nordea/dompap/workflow/workflow-A.json")));
    }

}
