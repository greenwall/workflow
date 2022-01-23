package com.nordea.dompap.workflow.event;

import lombok.Value;

import java.util.List;

@Value
public class WorkFlowEventSearchResult {
	public int total;
	public List<WorkFlowEvent> events;
}
