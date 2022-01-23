package com.nordea.dompap.workflow;

import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.UserId;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.resource.ResourceException;
import java.io.IOException;
import java.util.UUID;

/**
 * Test case intended to see runtime performance of adding metadata to workflow.
 * @author G93283
 */
@SpringBootTest(classes = com.nordea.dompap.config.WorkFlowContextSpring.class)
@ActiveProfiles("springtest")
public class WorkFlowServiceMetadataTest extends TestWithMemoryDB {

	private long createTimer = 0;

	private long getWorkflowOnlyTimer;

	private long saveWorkFlowMetadataTimer;

	@Autowired
	WorkFlowService workFlowService;

	@Autowired
	WorkFlowManager workFlowManager;

	@Autowired
	MetadataService metadataService;

	@Test
	public void metadataTest() throws ResourceException, IOException {
		int workflowCount = 10;
		int metadataCount = 10;
		for (int n=0; n<10; n++) {
			insertWorkflowMetadata(metadataCount);
		}
		
		System.out.println("#workflows="+workflowCount);
		System.out.println("createTimer="+createTimer);
		System.out.println("getWorkflowOnlyTimer="+getWorkflowOnlyTimer);
		System.out.println("saveWorkFlowMetadataTimer="+saveWorkFlowMetadataTimer);
	}
	
	public void insertWorkflowMetadata(int metadataCount) throws ResourceException, IOException {
		UUID id = UUID.randomUUID();

		{
			String proc = "";
			
			UserId userId = new UserId("G93283");
			BranchId branchId = new BranchId("0000");
			
			long start = System.currentTimeMillis();
			workFlowManager.create(id, null, userId, "requestDomain", branchId, proc.getClass().getName(), null, proc, "stop", null, null);
			createTimer  += System.currentTimeMillis()-start; 
		}
		
		String millis = ""+System.currentTimeMillis();
		Metadata metadata = new Metadata(null);
		for (int m=0; m<metadataCount; m++) {
			addIfNotEmpty(metadata, "x"+m, "a"+m+"-"+millis);
		}

		
		long start = System.currentTimeMillis();
		WorkFlow<?> wf = workFlowManager.getWorkFlowOnly(id);
		getWorkflowOnlyTimer  += System.currentTimeMillis()-start; 
		
		start = System.currentTimeMillis();
		workFlowService.saveWorkFlowMetadata(wf, metadata);
		saveWorkFlowMetadataTimer += System.currentTimeMillis()-start;
	}
	
	private void addIfNotEmpty(Metadata metadata, String metadataName, String value) throws ResourceException {
		if (StringUtils.isNotBlank(value)) {
			String currentValue = metadata.getFirstProperty(metadataName);
			PropertyType propertyType = metadataService.getOrCreatePropertyType(metadataName, "no description");
			if (currentValue==null) {
				metadata.putProperty(propertyType, value);
			} else {
				if (!StringUtils.equals(currentValue, value)) {
					// Different metadata!
					metadata.removeAll(propertyType);
					metadata.putProperty(propertyType, value);
				}
			}
		}		
	}
	

}
