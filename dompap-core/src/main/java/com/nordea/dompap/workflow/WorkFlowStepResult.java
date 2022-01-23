package com.nordea.dompap.workflow;

import lombok.Value;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Value
public class WorkFlowStepResult implements Serializable {
	private static final long serialVersionUID = -7063393164516034871L;

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
