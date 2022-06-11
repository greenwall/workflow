package com.nordea.dompap.workflow.event;

import lombok.Value;

import java.util.List;

@Value
public class WorkflowEventSearchResult {
	public int total;
	public List<WorkflowEvent> events;
}
