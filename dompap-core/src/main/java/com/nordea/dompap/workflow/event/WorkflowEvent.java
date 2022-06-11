package com.nordea.dompap.workflow.event;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import lombok.Getter;
import org.apache.commons.lang.builder.ToStringBuilder;

public class WorkflowEvent implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Getter final UUID id;
	@Getter final Date creationTime;
	@Getter byte[] content;
	@Getter final UUID workflowId;
	@Getter final String eventType;
	@Getter final String eventName;
	@Getter final Date processedTime;

	@Getter String userId;
	@Getter String applicationId;
	@Getter String technicalUserId;
	@Getter String requestId;
	@Getter String requestDomain;
	@Getter String sessionId;

	public WorkflowEvent(UUID id, Date eventReceivedTime, byte[] content, UUID workflowId, String eventType, String eventName, Date processedTime) {
		this.id = id;
		this.creationTime = eventReceivedTime;
		this.content = content;
		this.workflowId = workflowId;
		this.eventType = eventType;
		this.eventName = eventName;
		this.processedTime = processedTime;
	}
        
	public String toString() {
    	return new ToStringBuilder(this)
    	.append("id",id)
    	.append("creationTime", creationTime)
    	.append("workflowId",workflowId)
    	.append("eventType", eventType)    	
    	.append("eventName", eventName)    	
        .append("processedTime", processedTime)
        .append("userId", userId)
        .append("applicationId", applicationId)
        .append("technicalUserId", technicalUserId)
        .append("requestId", requestId)
        .append("requestDomain", requestDomain)
        .append("sessionId", sessionId)
        .toString();
    }
}
