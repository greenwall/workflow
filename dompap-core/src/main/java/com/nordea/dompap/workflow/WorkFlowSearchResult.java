package com.nordea.dompap.workflow;

import lombok.Value;

import java.util.List;

@Value
public class WorkFlowSearchResult {
	public int totalWorkflows;
	@SuppressWarnings("rawtypes")
	public List<WorkFlow> workflows;

}
