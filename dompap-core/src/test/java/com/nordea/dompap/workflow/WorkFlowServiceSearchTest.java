package com.nordea.dompap.workflow;

import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.UserId;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.resource.ResourceException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = com.nordea.dompap.config.WorkFlowContextSpring.class)
@ActiveProfiles("springtest")
public class WorkFlowServiceSearchTest extends TestWithMemoryDB {

	@Autowired
	WorkflowService workFlowService;
	@Autowired
	WorkflowManager workFlowManager;
	@Autowired
	WorkflowStatusService workFlowStatusService;
	@Autowired
	MetadataService metadataService;

	@Test
	public void testStatus() throws ResourceException {
		WorkflowStatusQuery query = new WorkflowStatusQuery();
		query.setPeriods(new int[] {1440, 2880, 14400});
		query.setIncludeRecentMinutes(1440);
		query.setIncludeLastDays(10);
		List<WorkflowMethodCount> lines = workFlowStatusService.getWorkflowStatus(query);
	}

	@Test
	public void testSearchWithMetadata() throws ResourceException, IOException {
		// In memory DB is cleared between tests, so propertyType map is invalid.
		((MetadataServiceImpl)metadataService).initPropertyTypes();

		UserId userId = new UserId("test1");
		BranchId branchId = new BranchId(""+RandomUtils.nextInt(9999));

		byte[] blob = new byte[0];
		
		Method doA = WorkflowUtil.getMethod(TestWorkFlow.class, "doA");
		Method doB = WorkflowUtil.getMethod(TestWorkFlow.class, "doB");
		Method doC = WorkflowUtil.getMethod(TestWorkFlow.class, "doC");

		long timestamp = System.currentTimeMillis();
		TestWorkFlow wf1 = new TestWorkFlow(5, blob);
		Metadata meta1 = createMetadata("A1"+timestamp, "B1"+timestamp, "C1"+timestamp, "D1"+timestamp);
		
		TestWorkFlow wf2 = new TestWorkFlow(2, blob);
		Metadata meta2 = createMetadata("A2"+timestamp, "B2"+timestamp, "C2"+timestamp, "D2"+timestamp);
		

		TestWorkFlow wf3 = new TestWorkFlow(8, blob);
		Metadata meta3 = createMetadata("A3"+timestamp, "B3"+timestamp, "C3"+timestamp, "D3"+timestamp);

		WorkflowSearch search = new WorkflowSearch();
		search.setBranchId(branchId);
		search.setMethod("doB");
		WorkflowSearchResult before = workFlowService.searchWorkFlows(search, 0, 10);

		Workflow<?> w1 = workFlowService.insertWorkFlow(null, null, userId, "requestDomain", branchId, wf1, doA, null, meta1, null);
		Workflow<?> w2 = workFlowService.insertWorkFlow(null, null, userId, "requestDomain", branchId, wf2, doB, null, meta2, null);
		Workflow<?> w3 = workFlowService.insertWorkFlow(null, null, userId, "requestDomain", branchId, wf3, doC, null, meta3, null);

		WorkflowSearch all = new WorkflowSearch();
		all.setBranchId(branchId);
		WorkflowSearchResult total = workFlowService.searchWorkFlows(all, 0, 10);
		System.out.println("Total workflows="+total.totalWorkflows);
		
		{
			WorkflowSearchResult after = workFlowService.searchWorkFlows(search, 0, 10);
			assertEquals(1, after.totalWorkflows - before.totalWorkflows);
			assertEquals(w2.getId(), after.workflows.get(0).getId());
		}

		{
			search = new WorkflowSearch();
			search.setBranchId(branchId);			
			search.setWorkFlowClass(TestWorkFlow.class);
			WorkflowSearchResult result = workFlowService.searchWorkFlows(search, 0, 10);
			assertEquals(3, result.totalWorkflows);
			assertTrue(result.workflows.stream().anyMatch(w->w.getId().equals(w3.getId())));
		}

		{
			search = new WorkflowSearch();
			search.setBranchId(branchId);
			search.putMetadataProperty("propB", "B1%");
			WorkflowSearchResult result = workFlowService.searchWorkFlows(search, 0, 10);
			assertEquals(w1.getId(), result.workflows.get(0).getId());
		}

		{
			search = new WorkflowSearch();
			search.setBranchId(branchId);
			search.putMetadataProperty("propB", "B3"+timestamp);
			WorkflowSearchResult result = workFlowService.searchWorkFlows(search, 0, 10);
			assertEquals(w3.getId(), result.workflows.get(0).getId());
		}
		
	}

	@Test
	public void testSearchWithoutMetadata() throws ResourceException, IOException {
		UserId userId = new UserId("test1");
		BranchId branchId = new BranchId(""+RandomUtils.nextInt(9999));

		byte[] blob = new byte[0];
		
		Method doA = WorkflowUtil.getMethod(TestWorkFlow.class, "doA");
		Method doB = WorkflowUtil.getMethod(TestWorkFlow.class, "doB");
		Method doC = WorkflowUtil.getMethod(TestWorkFlow.class, "doC");

		long timestamp = System.currentTimeMillis();
		TestWorkFlow wf1 = new TestWorkFlow(5, blob);
		
		WorkflowSearch all = new WorkflowSearch();
		all.setBranchId(branchId);
		WorkflowSearchResult before = workFlowService.searchWorkFlows(all, 0, 10);

		Workflow<TestWorkFlow> w1 = workFlowService.insertWorkFlow(null, null, userId, "requestDomain", branchId, wf1, doA, null, null, null);

		WorkflowSearchResult after = workFlowService.searchWorkFlows(all, 0, 10);
		System.out.println("Total workflows="+after.totalWorkflows);
		assertEquals(1, after.totalWorkflows - before.totalWorkflows);
		assertEquals(w1.getId(), after.workflows.get(0).getId());

	}	
	
	@Test
	public void testSearchWithMetadataAndMore() throws ResourceException, IOException {
		UserId userId = new UserId("test1");
		BranchId branchId = new BranchId("1234");

		byte[] blob = new byte[0];
		
		Method doA = WorkflowUtil.getMethod(TestWorkFlow.class, "doA");
		Method doB = WorkflowUtil.getMethod(TestWorkFlow.class, "doB");
		Method doC = WorkflowUtil.getMethod(TestWorkFlow.class, "doC");

		TestWorkFlow wf1 = new TestWorkFlow(5, blob);
		long timestamp = System.currentTimeMillis();
		Metadata meta1 = createMetadata("A1"+timestamp, "B1"+timestamp, "C1"+timestamp, "D1"+timestamp);
		Workflow<?> w1 = workFlowService.insertWorkFlow(null, null, userId, "requestDomain", branchId, wf1, doA, null, meta1, null);

		WorkflowSearch search = new WorkflowSearch();
		search.putMetadataProperty("propB", "B1%");
		WorkflowSearchResult result = workFlowService.searchWorkFlows(search, 0, 10);
		
		assertTrue(result.totalWorkflows>0);
		
		Workflow<?> wf = result.workflows.get(0);
		Metadata meta = workFlowService.loadWorkFlowMetadata(wf); //new MetadataServiceImpl().getMetadata(wf.getId());
		assertEquals(4, meta.entries().size());
		List<PropertyType> types = metadataService.getPropertyTypes();
		assertEquals(1, meta.getProperties("propA").size());
		assertEquals(1, meta.getProperties("propB").size());
		assertEquals(1, meta.getProperties("propC").size());
		assertEquals(1, meta.getProperties("propD").size());
		assertEquals(0, meta.getProperties("propE").size());
		
		search = new WorkflowSearch();
		search.putMetadataProperty("propC", meta.getFirstProperty("propC"));
		search.putMetadataProperty("propA", meta.getFirstProperty("propA"));
		search.setBranchId(wf.getBranchId());
		search.setUserId(wf.getUserId());
		search.setMethod(wf.getMethodName());
		
		result = workFlowService.searchWorkFlows(search, 0, 10);
		
		assertEquals(1, result.totalWorkflows);
		assertEquals(wf.getId(), result.workflows.get(0).getId());
		
		// Verify that metadata is also loaded when getting workflow by id.
		Workflow<?> wf2 = workFlowService.getWorkFlow(wf.getId());
		assertEquals(workFlowService.loadWorkFlowMetadata(wf).getFirstProperty("propA"), workFlowService.loadWorkFlowMetadata(wf2).getFirstProperty("propA"));
	}
	
	
	//@Test(expected=IllegalArgumentException.class)
	public void testSearchWithWrongMetadata() throws ResourceException, IOException {
			WorkflowSearch search = new WorkflowSearch();
			search.putMetadataProperty("propE", "B3xxx");
			WorkflowSearchResult result = workFlowService.searchWorkFlows(search, 0, 100);
			assertEquals(0, result.totalWorkflows);
	}

	@Test
	public void testMetadataOnWorkFlow() throws ResourceException, IOException {
		UserId userId = new UserId("test1");
		BranchId branchId = new BranchId(""+RandomUtils.nextInt(9999));

		byte[] blob = new byte[0];
		
		Method doA = WorkflowUtil.getMethod(TestWorkFlow.class, "doA");
		Method doB = WorkflowUtil.getMethod(TestWorkFlow.class, "doB");
		Method doC = WorkflowUtil.getMethod(TestWorkFlow.class, "doC");

		long timestamp = System.currentTimeMillis();
		TestWorkFlow twf = new TestWorkFlow(5, blob);
		{		
			Workflow<TestWorkFlow> wNoMetadata = workFlowService.insertWorkFlow(null, null, userId, "requestDomain", branchId, twf, doA, null, null, null);
	
			WorkflowSearch all = new WorkflowSearch();
			WorkflowSearchResult after = workFlowService.searchWorkFlows(all, 0, 10);
			assertEquals(wNoMetadata.getId(), after.workflows.get(0).getId());
			
			Workflow<?> w = after.workflows.get(0);
			assertEquals(0, workFlowService.loadWorkFlowMetadata(w).getNamedEntries().size());
		}
		{
			Metadata metadata = new Metadata(null);
			metadata.putProperty(metadataService.getOrCreatePropertyType("test1", null), "value1");
			UUID id = UUID.randomUUID();
			Workflow<TestWorkFlow> wOneMetadata = workFlowService.insertWorkFlow(id, null, userId, "requestDomain", branchId, twf, doA, null, metadata, null);
	
			WorkflowSearch all = new WorkflowSearch();
			WorkflowSearchResult after = workFlowService.searchWorkFlows(all, 0, 100);
			// TODO 2 workflows inserted and returned in arbitrary order
//			assertEquals(wOneMetadata.getId(), after.workflows.get(0).getId());

			UUID uid = wOneMetadata.getId();
			assertTrue(uid.equals(after.workflows.get(0).getId()) || uid.equals(after.workflows.get(1).getId()));

			Workflow<?> w = null;
			System.out.println("After: " + after.workflows.size());
			System.out.println("Looking for: " + id);
			for (Workflow<?> wf : after.workflows) {
				System.out.println("UUID: " + wf.getId());
				if (wf.getId().equals(id)) {
					w = wf;
					break;
				}
			}
			assertNotNull(w, "No workflow found for id: " + id);
			Metadata wmd = workFlowService.loadWorkFlowMetadata(w);
			assertEquals(1, wmd.getNamedEntries().size());
		}
		{
			Metadata metadata = new Metadata(null);
			metadata.putProperty(metadataService.getOrCreatePropertyType("test1", null), "value1");
			metadata.putProperty(metadataService.getOrCreatePropertyType("test1", null), "value2");
			metadata.putProperty(metadataService.getOrCreatePropertyType("test2", null), "value1");

			Workflow<?> wOneMetadata = workFlowService.insertWorkFlow(null, null, userId, "requestDomain", branchId, twf, doA, null, metadata, null);

			WorkflowSearch all = new WorkflowSearch();
			WorkflowSearchResult after = workFlowService.searchWorkFlows(all, 0, 10);
			assertTrue(after.workflows.stream().anyMatch(w->w.getId().equals(wOneMetadata.getId())));

			Workflow<?> w = after.workflows.get(0);
			List<Entry<String,String>> props = workFlowService.loadWorkFlowMetadata(w).getNamedEntries();
			assertEquals(3, props.size());
		}
		
	}
	
	private Metadata createMetadata(String propA, String propB, String propC, String propD) throws ResourceException {
		Metadata meta1 = new Metadata(null);
		meta1.putProperty(metadataService.getOrCreatePropertyType("propA", null), propA);
		meta1.putProperty(metadataService.getOrCreatePropertyType("propB", null), propB);
		meta1.putProperty(metadataService.getOrCreatePropertyType("propC", null), propC);
		meta1.putProperty(metadataService.getOrCreatePropertyType("propD", null), propD);
		return meta1;
	}
}
