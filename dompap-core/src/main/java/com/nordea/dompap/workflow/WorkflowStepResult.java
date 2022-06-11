package com.nordea.dompap.workflow;

import lombok.Value;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Value
public class WorkflowStepResult implements Serializable {
	UUID id;
	String methodName;
	Date methodStarted;
	Date methodEnded;
	String exceptionName;
	String exceptionMessage;
	String stacktrace;
	String serverName;
	String version;
}
