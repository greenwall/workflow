package com.nordea.dompap.workflow;

import com.nordea.dompap.workflow.content.WorkflowContentSerializer;
import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.ProfitCenter;
import com.nordea.next.dompap.domain.UserId;

import java.util.Date;
import java.util.UUID;

/**
 * Helper class for setting parameters for starting a workflow. 
 * @param <T>
 */
public class WorkflowBuilder<T> {
	UUID id;
	String externalKey;
	UserId userId;
	String requestDomain = null;
	BranchId branchId;
	ProfitCenter profitCenter;
	String subType = null;				
	Metadata metadata = null;
	String methodName;
//	Method method;
	Date startWhen=new Date();
	T workflow;
//	Class<T> workflowClass;
	String workflowClassName;
	WorkflowController controller;
	WorkflowContentSerializer contentSerializer;
	
	public WorkflowBuilder<T> id(UUID id) {
		this.id = id;
		return this;
	}
	public WorkflowBuilder<T> userId(UserId userId) {
		this.userId = userId;
		return this;
	}
	public WorkflowBuilder<T> requestDomain(String requestDomain) {
		this.requestDomain = requestDomain;
		return this;
	}	
	public WorkflowBuilder<T> branchId(BranchId branchId) {
		this.branchId = branchId;
		return this;
	}
	public WorkflowBuilder<T> profitCenter(ProfitCenter profitCenter) {
		this.profitCenter = profitCenter;
		return this;
	}
	public WorkflowBuilder<T> subType(String subType) {
		this.subType = subType;
		return this;
	}
	public WorkflowBuilder<T> externalKey(String externalKey) {
		this.externalKey = externalKey;
		return this;
	}
	public WorkflowBuilder<T> workflow(T workflow) {
		this.workflow = workflow;
//		this.workflowClass = (Class<T>) workflow.getClass();
		if (workflowClassName==null) {
//			this.workflowClassName = workflowClass.getName();
			workflowClassName = workflow.getClass().getName();
		}
		return this;
	}	
	public WorkflowBuilder<T> workflowClassName(String workflowClassName) {
		this.workflowClassName = workflowClassName;
		return this;
	}	
	public WorkflowBuilder<T> methodName(String methodName) {
		this.methodName = methodName;
//		this.method = WorkFlowUtil.getMethod(workflow, methodName);
		return this;
	}
	public WorkflowBuilder<T> startWhen(Date startWhen) {
		this.startWhen = startWhen;
		return this;
	}
	public WorkflowBuilder<T> metadata(Metadata metadata) {
		this.metadata = metadata;
		return this;
	}
	public WorkflowBuilder<T> controller(WorkflowController controller) {
		this.controller = controller;
		return this;
	}
	public WorkflowBuilder<T> contentSerializer(WorkflowContentSerializer contentSerializer) {
		this.contentSerializer = contentSerializer;
		return this;
	}
}
