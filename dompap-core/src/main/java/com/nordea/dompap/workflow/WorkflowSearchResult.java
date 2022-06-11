package com.nordea.dompap.workflow;

import lombok.Value;

import java.util.List;

@Value
public class WorkflowSearchResult {
	public int totalWorkflows;
	@SuppressWarnings("rawtypes")
	public List<Workflow> workflows;

}
