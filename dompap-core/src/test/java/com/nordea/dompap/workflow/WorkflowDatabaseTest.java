package com.nordea.dompap.workflow;

import com.nordea.dompap.workflow.config.WorkFlowContext;
import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.resource.ResourceException;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = com.nordea.dompap.config.WorkFlowContextSpring.class)
@ActiveProfiles("springtest")
public class WorkflowDatabaseTest extends TestWithMemoryDB {
	private static final String DO_STUFF2 = "doStuff2";
	private static final String NEW_EXTERNAL_KEY = "TEST2";

	@Autowired
	private WorkFlowContext context;

	@Test
	public void insertUpdateWorkflow() throws ResourceException, IOException, ClassNotFoundException {
		MyWorkflow wf = new MyWorkflow();
		WorkFlowService service = context.getWorkFlowService();
		UUID id = UUID.randomUUID();
		Metadata wfMetadata = new Metadata(null);
		wfMetadata.putProperty(new PropertyType("PROPERTY", null), "VALUE");
		DefaultWorkFlowController wfController = new DefaultWorkFlowController(context.getWorkFlowConfig(), context.getWorkFlowManager());
		WorkFlowBuilder<MyWorkflow> builder = new WorkFlowBuilder<>();
		builder.id(id)
			.externalKey("TEST1")
			.userId(new UserId("X00000"))
			.requestDomain("FI")
			.branchId(new BranchId("1111"))
			.workflow(wf)
			.methodName(WorkFlowUtil.getMethod(MyWorkflow.class, "doStuff").getName())
			.startWhen(new Date())
			.metadata(wfMetadata)
			.controller(wfController);
		service.insertWorkFlow(builder);
//		service.insertWorkFlow(id, "TEST1", new UserId("X00000"), "FI", new BranchId("1111"), wf, WorkFlowUtil.getMethod(MyWorkflow.class, "doStuff"), new Date(), wfMetadata , wfController);
		WorkFlow<?> workflow = service.getWorkFlow(id);
		Object content = service.loadWorkFlowContent(workflow);
		assertEquals(wf, content, "Workflow content not equal");
		WorkFlowController controller = service.loadWorkFlowController(workflow);
		assertEquals(wfController, controller, "Workflow controller not equal");
		Metadata metadata = service.loadWorkFlowMetadata(workflow);
		System.out.println(wfMetadata);
		System.out.println(metadata.toString());
		assertEquals(wfMetadata, metadata, "Metadata not equal");
		
		service.updateWorkFlow(id, NEW_EXTERNAL_KEY);
		workflow = service.getWorkFlow(id);
		assertEquals(NEW_EXTERNAL_KEY, workflow.getExternalKey(), "External key not updated");
		
		service.updateWorkFlow(workflow, WorkFlowUtil.getMethod(MyWorkflow.class, DO_STUFF2), new Date(), new Date(), new Date(), null, metadata);
		workflow = service.getWorkFlow(id);
		assertEquals(DO_STUFF2, workflow.getMethodName(), "Method name not updated");
	}
}
