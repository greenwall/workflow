package com.nordea.dompap.workflow;

import com.nordea.dompap.workflow.content.WorkFlowContentSerializer;
import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.ProfitCenter;
import com.nordea.next.dompap.domain.UserId;

import java.util.Date;
import java.util.UUID;

/**
 * Helper class for setting parameters for starting a workflow. 
 * @author G93283
 * @param <T>
 */
public class WorkFlowBuilder<T> {
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
	WorkFlowController controller;
	WorkFlowContentSerializer contentSerializer;
	
	public WorkFlowBuilder<T> id(UUID id) {
		this.id = id;
		return this;
	}
	public WorkFlowBuilder<T> userId(UserId userId) {
		this.userId = userId;
		return this;
	}
	public WorkFlowBuilder<T> requestDomain(String requestDomain) {
		this.requestDomain = requestDomain;
		return this;
	}	
	public WorkFlowBuilder<T> branchId(BranchId branchId) {
		this.branchId = branchId;
		return this;
	}
	public WorkFlowBuilder<T> profitCenter(ProfitCenter profitCenter) {
		this.profitCenter = profitCenter;
		return this;
	}
	public WorkFlowBuilder<T> subType(String subType) {
		this.subType = subType;
		return this;
	}
	public WorkFlowBuilder<T> externalKey(String externalKey) {
		this.externalKey = externalKey;
		return this;
	}
	public WorkFlowBuilder<T> workflow(T workflow) {
		this.workflow = workflow;
//		this.workflowClass = (Class<T>) workflow.getClass();
		if (workflowClassName==null) {
//			this.workflowClassName = workflowClass.getName();
			workflowClassName = workflow.getClass().getName();
		}
		return this;
	}	
	public WorkFlowBuilder<T> workflowClassName(String workflowClassName) {
		this.workflowClassName = workflowClassName;
		return this;
	}	
	public WorkFlowBuilder<T> methodName(String methodName) {
		this.methodName = methodName;
//		this.method = WorkFlowUtil.getMethod(workflow, methodName);
		return this;
	}
	public WorkFlowBuilder<T> startWhen(Date startWhen) {
		this.startWhen = startWhen;
		return this;
	}
	public WorkFlowBuilder<T> metadata(Metadata metadata) {
		this.metadata = metadata;
		return this;
	}
	public WorkFlowBuilder<T> controller(WorkFlowController controller) {
		this.controller = controller;
		return this;
	}
	public WorkFlowBuilder<T> contentSerializer(WorkFlowContentSerializer contentSerializer) {
		this.contentSerializer = contentSerializer;
		return this;
	}
}
