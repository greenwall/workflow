package com.nordea.dompap.workflow.event;

import com.nordea.dompap.workflow.TestWithMemoryDB;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.resource.ResourceException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = com.nordea.dompap.config.WorkFlowContextSpring.class)
@ActiveProfiles("memorydb")
public class WorkFlowEventServiceTest extends TestWithMemoryDB {

	@Autowired
	private WorkFlowEventService service;

	@Test
	public void testFirstEvent() throws ResourceException, InterruptedException {
		UUID workflowId = UUID.randomUUID();

		UUID id1 = UUID.randomUUID();
		byte[] content1 = "This is a test event".getBytes();
		String eventType = "test";
		String eventName = "please.delete";
		
		WorkFlowEvent event1 = service.createEvent(id1, content1, eventType, workflowId, eventName);
		System.out.println(event1.getCreationTime().getTime());
		
		WorkFlowEvent firstEvent = service.getFirstEventFor(workflowId);
		assertEquals(id1, firstEvent.getId());

		Thread.sleep(1);

		UUID id2 = UUID.randomUUID();
		byte[] content2 = "This is a test event2".getBytes();
		WorkFlowEvent event2 = service.createEvent(id2, content2, eventType, workflowId, eventName);
		System.out.println(event2.getCreationTime().getTime());

		Thread.sleep(1);

		firstEvent = service.getFirstEventFor(workflowId);
		assertEquals(id1, firstEvent.getId());

		UUID id3 = UUID.randomUUID();
		byte[] content3 = "This is a test event3".getBytes();
		WorkFlowEvent event3 = service.createEvent(id3, content3, eventType, workflowId, eventName);
		System.out.println(event3.getCreationTime().getTime());

		firstEvent = service.getFirstEventFor(workflowId);
		assertEquals(id1, firstEvent.getId());
		
		// Mark first as processed and expect second.
		service.processedEvent(event1);
		firstEvent = service.getFirstEventFor(workflowId);
		assertEquals(id2, firstEvent.getId());
		
		// Mark second as processed and expect third.
		service.processedEvent(event2);
		firstEvent = service.getFirstEventFor(workflowId);
		assertEquals(id3, firstEvent.getId());

		// Mark third as processed and expect null.
		service.processedEvent(event3);
		firstEvent = service.getFirstEventFor(workflowId);
		assertNull(firstEvent);
		
	}
	
}
