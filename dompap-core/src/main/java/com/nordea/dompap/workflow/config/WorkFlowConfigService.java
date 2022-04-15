package com.nordea.dompap.workflow.config;

import java.util.List;

import javax.resource.ResourceException;

/**
 * Read active workflow classes on server
 */
public interface WorkFlowConfigService {
	/**
	 * Read active workflow classes on server
	 * @param serverName returns classes for the given serverName or all if null
	 * @param enabled returns classes with the given value for enabled or all if null
	 */
	List<String> getWorkFlowClassesFor(String serverName, Boolean enabled) throws ResourceException;
	
	void saveWorkFlowClassFor(String serverName, String workFlowClass, boolean enabled) throws ResourceException;

	List<WorkFlowClassServer> getWorkFlowClassServers(String className, String serverName, Boolean enabled) throws ResourceException;
	
	
}
