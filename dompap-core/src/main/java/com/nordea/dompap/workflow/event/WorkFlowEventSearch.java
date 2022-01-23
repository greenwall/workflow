package com.nordea.dompap.workflow.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.Interval;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkFlowEventSearch implements Serializable {
	private static final long serialVersionUID = 1L;

	private Interval creationTime;
	private String workFlowId;
	private String eventId;
	private String eventType;
	private String eventName;
}
