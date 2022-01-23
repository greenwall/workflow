package com.nordea.dompap.workflow.event;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class WorkFlowEventBuilder implements Serializable {
	
	private static final long serialVersionUID = 1L;

	UUID id;
	Date creationTime;
	byte[] content;
	UUID workflowId;
	String eventType;
	String eventName;
	Date processedTime;
	
	String userId;
	String applicationId;
	String technicalUserId;
	String requestId;
	String requestDomain;
	String sessionId;

	public WorkFlowEventBuilder id(UUID id) {
		this.id = id;
		return this;
	}
	
	public WorkFlowEventBuilder creationTime(Date creationTime) {
		this.creationTime = creationTime;
		return this;
	}
    
	public WorkFlowEventBuilder workflowId(UUID workflowId) {
		this.workflowId = workflowId;
		return this;
	}
	
	public WorkFlowEventBuilder eventType(String eventType) {
		this.eventType = eventType;
		return this;
	}
	
	public WorkFlowEventBuilder eventName(String eventName) {
		this.eventName = eventName;
		return this;
	}
	
	public WorkFlowEventBuilder processedTime(Date processedTime) {
		this.processedTime = processedTime;
		return this;
	}
	
	public WorkFlowEventBuilder userId(String userId) {
		this.userId = userId;
		return this;
	}
	
	public WorkFlowEventBuilder applicationId(String applicationId) {
		this.applicationId = applicationId;
		return this;
	}
	
	public WorkFlowEventBuilder technicalUserId(String technicalUserId) {
		this.technicalUserId = technicalUserId;
		return this;
	}
	
	public WorkFlowEventBuilder requestId(String requestId) {
		this.requestId = requestId;
		return this;
	}
	
	public WorkFlowEventBuilder requestDomain(String requestDomain) {
		this.requestDomain = requestDomain;
		return this;
	}
	
	public WorkFlowEventBuilder sessionId(String sessionId) {
		this.sessionId = sessionId;
		return this;
	}
	
	public WorkFlowEventBuilder content(byte[] content) {
		this.content = content;
		return this;
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
