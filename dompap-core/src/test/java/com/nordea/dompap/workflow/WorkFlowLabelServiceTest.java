package com.nordea.dompap.workflow;

import com.nordea.next.dompap.domain.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.resource.ResourceException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = com.nordea.dompap.config.WorkFlowContextSpring.class)
@ActiveProfiles("springtest")
public class WorkFlowLabelServiceTest extends TestWithMemoryDB {

	@Autowired
	WorkFlowLabelService workFlowLabelService;
	@Autowired
	WorkFlowService workFlowService;

	@Test
	public void testCreateDelete() throws ResourceException {
		UserId createdBy = new UserId("test");
		Date expireTime = null;
		String name = "test";
		int ignoreWorkflows = 0;
		String description = "This is a test";

		List<WorkFlowLabel> result;
		result = workFlowLabelService.searchLabels(createdBy, null, null, name);
		assertEquals(0, result.size());
		
		WorkFlowLabel label = workFlowLabelService.create(createdBy, expireTime, name, ignoreWorkflows, description);

		result = workFlowLabelService.searchLabels(createdBy, null, null, name);
		assertEquals(1, result.size());
		
		workFlowLabelService.deleteLabel(label);

		result = workFlowLabelService.searchLabels(createdBy, null, null, name);
		assertEquals(0, result.size());
		
	}
	
	@Test
	public void testAddLabel() throws ResourceException, IOException {
		UserId createdBy = new UserId("test");
		Date expireTime = null;
		String name = "testLabel";
		int ignoreWorkflows = 0;
		String description = "This is a test label for testing addition to workflows";

		// Search for existing label and remove if found
		{
			List<WorkFlowLabel> labels = workFlowLabelService.searchLabels(null, null,  null, name);
			if (labels.size()==1) {
				workFlowLabelService.deleteLabel(labels.get(0));
			}
		}
		
		// Create label
		WorkFlowLabel label = workFlowLabelService.create(createdBy, expireTime, name, ignoreWorkflows, description);

		// Assert label is not added to any workflows.
		{
			WorkFlowSearch searchWithLabel = new WorkFlowSearch();
			searchWithLabel.setLabelId(label.getId().toString());
			WorkFlowSearchResult resultWithLabel = workFlowService.searchWorkFlows(searchWithLabel, 0, 10, false);
			assertEquals(0, resultWithLabel.totalWorkflows);
		}

		WorkFlowBuilder<String> builder = new WorkFlowBuilder<>();
		builder.workflow("test").methodName("no");
		for (int n=0; n<10; n++) {
			workFlowService.insertWorkFlow(builder);
		}

		// Fetch 10 workflows
		WorkFlowSearch search = new WorkFlowSearch();
		WorkFlowSearchResult result = workFlowService.searchWorkFlows(search, 0, 10, false);
		assertTrue(result.totalWorkflows>=10);
		
		// Add label to 10 workflows
		List<UUID> workflowIds = result.workflows.stream().map(WorkFlow::getId).collect(Collectors.toList());
		workFlowLabelService.addLabelToWorkFlows(label, workflowIds);
		
		// Assert label is added to 10 workflows.
		{
			WorkFlowSearch searchWithLabel = new WorkFlowSearch();
			searchWithLabel.setLabelId(label.getId().toString());
			WorkFlowSearchResult resultWithLabel = workFlowService.searchWorkFlows(searchWithLabel, 0, 10, false);
			assertEquals(10, resultWithLabel.totalWorkflows);
		}		
		
		// Delete label - assert no workflows has label after deletion.
		{
			UUID labelId = label.getId();
			
			workFlowLabelService.deleteLabel(label);

			WorkFlowSearch searchWithLabel = new WorkFlowSearch();
			searchWithLabel.setLabelId(labelId.toString());
			WorkFlowSearchResult resultWithLabel = workFlowService.searchWorkFlows(searchWithLabel, 0, 10, false);
			assertEquals(0, resultWithLabel.totalWorkflows);
		}
	
	}	
	
	
}
