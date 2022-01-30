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
	WorkFlowService workFlowService;
	@Autowired
	WorkFlowManager workFlowManager;
	@Autowired
	WorkFlowStatusService workFlowStatusService;
	@Autowired
	MetadataService metadataService;

	@Test
	public void testStatus() throws ResourceException {
		WorkFlowStatusQuery query = new WorkFlowStatusQuery();
		query.setPeriods(new int[] {1440, 2880, 14400});
		query.setIncludeRecentMinutes(1440);
		query.setIncludeLastDays(10);
		List<WorkFlowMethodCount> lines = workFlowStatusService.getWorkflowStatus(query);
	}

	@Test
	public void testSearchWithMetadata() throws ResourceException, IOException {
		// In memory DB is cleared between tests, so propertyType map is invalid.
		((MetadataServiceImpl)metadataService).initPropertyTypes();

		UserId userId = new UserId("test1");
		BranchId branchId = new BranchId(""+RandomUtils.nextInt(9999));

		byte[] blob = new byte[0];
		
		Method doA = WorkFlowUtil.getMethod(TestWorkFlow.class, "doA");
		Method doB = WorkFlowUtil.getMethod(TestWorkFlow.class, "doB");
		Method doC = WorkFlowUtil.getMethod(TestWorkFlow.class, "doC");

		long timestamp = System.currentTimeMillis();
		TestWorkFlow wf1 = new TestWorkFlow(5, blob);
		Metadata meta1 = createMetadata("A1"+timestamp, "B1"+timestamp, "C1"+timestamp, "D1"+timestamp);
		
		TestWorkFlow wf2 = new TestWorkFlow(2, blob);
		Metadata meta2 = createMetadata("A2"+timestamp, "B2"+timestamp, "C2"+timestamp, "D2"+timestamp);
		

		TestWorkFlow wf3 = new TestWorkFlow(8, blob);
		Metadata meta3 = createMetadata("A3"+timestamp, "B3"+timestamp, "C3"+timestamp, "D3"+timestamp);

		WorkFlowSearch search = new WorkFlowSearch();
		search.setBranchId(branchId);
		search.setMethod("doB");
		WorkFlowSearchResult before = workFlowService.searchWorkFlows(search, 0, 10);

		WorkFlow<?> w1 = workFlowService.insertWorkFlow(null, null, userId, "requestDomain", branchId, wf1, doA, null, meta1, null);
		WorkFlow<?> w2 = workFlowService.insertWorkFlow(null, null, userId, "requestDomain", branchId, wf2, doB, null, meta2, null);
		WorkFlow<?> w3 = workFlowService.insertWorkFlow(null, null, userId, "requestDomain", branchId, wf3, doC, null, meta3, null);

		WorkFlowSearch all = new WorkFlowSearch();
		all.setBranchId(branchId);
		WorkFlowSearchResult total = workFlowService.searchWorkFlows(all, 0, 10);
		System.out.println("Total workflows="+total.totalWorkflows);
		
		{
			WorkFlowSearchResult after = workFlowService.searchWorkFlows(search, 0, 10);
			assertEquals(1, after.totalWorkflows - before.totalWorkflows);
			assertEquals(w2.getId(), after.workflows.get(0).getId());
		}

		{
			search = new WorkFlowSearch();
			search.setBranchId(branchId);			
			search.setWorkFlowClass(TestWorkFlow.class);
			WorkFlowSearchResult result = workFlowService.searchWorkFlows(search, 0, 10);
			assertEquals(w3.getId(), result.workflows.get(0).getId());
		}

		{
			search = new WorkFlowSearch();
			search.setBranchId(branchId);
			search.putMetadataProperty("propB", "B1%");
			WorkFlowSearchResult result = workFlowService.searchWorkFlows(search, 0, 10);
			assertEquals(w1.getId(), result.workflows.get(0).getId());
		}

		{
			search = new WorkFlowSearch();
			search.setBranchId(branchId);
			search.putMetadataProperty("propB", "B3"+timestamp);
			WorkFlowSearchResult result = workFlowService.searchWorkFlows(search, 0, 10);
			assertEquals(w3.getId(), result.workflows.get(0).getId());
		}
		
	}

	@Test
	public void testSearchWithoutMetadata() throws ResourceException, IOException {
		UserId userId = new UserId("test1");
		BranchId branchId = new BranchId(""+RandomUtils.nextInt(9999));

		byte[] blob = new byte[0];
		
		Method doA = WorkFlowUtil.getMethod(TestWorkFlow.class, "doA");
		Method doB = WorkFlowUtil.getMethod(TestWorkFlow.class, "doB");
		Method doC = WorkFlowUtil.getMethod(TestWorkFlow.class, "doC");

		long timestamp = System.currentTimeMillis();
		TestWorkFlow wf1 = new TestWorkFlow(5, blob);
		
		WorkFlowSearch all = new WorkFlowSearch();
		all.setBranchId(branchId);
		WorkFlowSearchResult before = workFlowService.searchWorkFlows(all, 0, 10);

		WorkFlow<TestWorkFlow> w1 = workFlowService.insertWorkFlow(null, null, userId, "requestDomain", branchId, wf1, doA, null, null, null);

		WorkFlowSearchResult after = workFlowService.searchWorkFlows(all, 0, 10);
		System.out.println("Total workflows="+after.totalWorkflows);
		assertEquals(1, after.totalWorkflows - before.totalWorkflows);
		assertEquals(w1.getId(), after.workflows.get(0).getId());

	}	
	
	@Test
	public void testSearchWithMetadataAndMore() throws ResourceException, IOException {
		UserId userId = new UserId("test1");
		BranchId branchId = new BranchId("1234");

		byte[] blob = new byte[0];
		
		Method doA = WorkFlowUtil.getMethod(TestWorkFlow.class, "doA");
		Method doB = WorkFlowUtil.getMethod(TestWorkFlow.class, "doB");
		Method doC = WorkFlowUtil.getMethod(TestWorkFlow.class, "doC");

		TestWorkFlow wf1 = new TestWorkFlow(5, blob);
		long timestamp = System.currentTimeMillis();
		Metadata meta1 = createMetadata("A1"+timestamp, "B1"+timestamp, "C1"+timestamp, "D1"+timestamp);
		WorkFlow<?> w1 = workFlowService.insertWorkFlow(null, null, userId, "requestDomain", branchId, wf1, doA, null, meta1, null);

		WorkFlowSearch search = new WorkFlowSearch();
		search.putMetadataProperty("propB", "B1%");
		WorkFlowSearchResult result = workFlowService.searchWorkFlows(search, 0, 10);
		
		assertTrue(result.totalWorkflows>0);
		
		WorkFlow<?> wf = result.workflows.get(0);
		Metadata meta = workFlowService.loadWorkFlowMetadata(wf); //new MetadataServiceImpl().getMetadata(wf.getId());
		assertEquals(4, meta.entries().size());
		List<PropertyType> types = metadataService.getPropertyTypes();
		assertEquals(1, meta.getProperties("propA").size());
		assertEquals(1, meta.getProperties("propB").size());
		assertEquals(1, meta.getProperties("propC").size());
		assertEquals(1, meta.getProperties("propD").size());
		assertEquals(0, meta.getProperties("propE").size());
		
		search = new WorkFlowSearch();
		search.putMetadataProperty("propC", meta.getFirstProperty("propC"));
		search.putMetadataProperty("propA", meta.getFirstProperty("propA"));
		search.setBranchId(wf.getBranchId());
		search.setUserId(wf.getUserId());
		search.setMethod(wf.getMethodName());
		
		result = workFlowService.searchWorkFlows(search, 0, 10);
		
		assertEquals(1, result.totalWorkflows);
		assertEquals(wf.getId(), result.workflows.get(0).getId());
		
		// Verify that metadata is also loaded when getting workflow by id.
		WorkFlow<?> wf2 = workFlowService.getWorkFlow(wf.getId());
		assertEquals(workFlowService.loadWorkFlowMetadata(wf).getFirstProperty("propA"), workFlowService.loadWorkFlowMetadata(wf2).getFirstProperty("propA"));
	}
	
	
	//@Test(expected=IllegalArgumentException.class)
	public void testSearchWithWrongMetadata() throws ResourceException, IOException {
			WorkFlowSearch search = new WorkFlowSearch();
			search.putMetadataProperty("propE", "B3xxx");
			WorkFlowSearchResult result = workFlowService.searchWorkFlows(search, 0, 100);
			assertEquals(0, result.totalWorkflows);
	}

	@Test
	public void testMetadataOnWorkFlow() throws ResourceException, IOException {
		UserId userId = new UserId("test1");
		BranchId branchId = new BranchId(""+RandomUtils.nextInt(9999));

		byte[] blob = new byte[0];
		
		Method doA = WorkFlowUtil.getMethod(TestWorkFlow.class, "doA");
		Method doB = WorkFlowUtil.getMethod(TestWorkFlow.class, "doB");
		Method doC = WorkFlowUtil.getMethod(TestWorkFlow.class, "doC");

		long timestamp = System.currentTimeMillis();
		TestWorkFlow twf = new TestWorkFlow(5, blob);
		{		
			WorkFlow<TestWorkFlow> wNoMetadata = workFlowService.insertWorkFlow(null, null, userId, "requestDomain", branchId, twf, doA, null, null, null);
	
			WorkFlowSearch all = new WorkFlowSearch();
			WorkFlowSearchResult after = workFlowService.searchWorkFlows(all, 0, 10);
			assertEquals(wNoMetadata.getId(), after.workflows.get(0).getId());
			
			WorkFlow<?> w = after.workflows.get(0);
			assertEquals(0, workFlowService.loadWorkFlowMetadata(w).getNamedEntries().size());
		}
		{
			Metadata metadata = new Metadata(null);
			metadata.putProperty(metadataService.getOrCreatePropertyType("test1", null), "value1");
			UUID id = UUID.randomUUID();
			WorkFlow<TestWorkFlow> wOneMetadata = workFlowService.insertWorkFlow(id, null, userId, "requestDomain", branchId, twf, doA, null, metadata, null);
	
			WorkFlowSearch all = new WorkFlowSearch();
			WorkFlowSearchResult after = workFlowService.searchWorkFlows(all, 0, 100);
			// TODO 2 workflows inserted and returned in arbitrary order
//			assertEquals(wOneMetadata.getId(), after.workflows.get(0).getId());

			UUID uid = wOneMetadata.getId();
			assertTrue(uid.equals(after.workflows.get(0).getId()) || uid.equals(after.workflows.get(1).getId()));

			WorkFlow<?> w = null;
			System.out.println("After: " + after.workflows.size());
			System.out.println("Looking for: " + id);
			for (WorkFlow<?> wf : after.workflows) {
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
			WorkFlow<?> wOneMetadata = workFlowService.insertWorkFlow(null, null, userId, "requestDomain", branchId, twf, doA, null, metadata, null);
	
			WorkFlowSearch all = new WorkFlowSearch();
			WorkFlowSearchResult after = workFlowService.searchWorkFlows(all, 0, 10);
			assertEquals(wOneMetadata.getId(), after.workflows.get(0).getId());
			
			WorkFlow<?> w = after.workflows.get(0);
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
