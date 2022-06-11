package com.nordea.dompap.workflow.config;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.io.Serializable;

@Value
@RequiredArgsConstructor
public class WorkflowClassServer implements Serializable {
	private static final long serialVersionUID = 1L;

	public String className;
	public String serverName;
	public boolean enabled;
}
