package com.nordea.dompap.workflow.config;

import java.lang.reflect.Method;

public interface WorkflowConfig {

    boolean isSelectorSkipLocked();

    /**
     * Quartz cron based schedule for workflow pickup
     */
    String getSchedule();

    /**
     * Class name of controller for given workflow class
     */
    String getControllerFor(String workFlowClass);

    String getControllerConfig(String controllerName);

    /**
     * After how many days should a workflow (declaring class of method) that finalized the given method be archived.
     */
	Integer getArchiveAfterDays(Method method);

    /**
     * Workflow classes (fully qualified) to be picked up and executed.
     */
    String[] getJobs();

    String getSchedule(String jobName);

    /**
     * App servers that should executed workflows.
     * Configuration is presumably the same across multiple instances on different app servers.
     */
    String[] getAppServers();

	String[] getExcludedAppServers();

    int getWorkflowJobTriggers();
    
    int getWorkflowMaxThreads();
    
    /**
     * Maximum number of workflow instances to execute per quartz job fire.
     */
    int getMaxWorkflowsPerFire(String workflowClassName);

    /**
     * Maximum number of seconds to execute workflow instances per quartz job fire.
     */
    int getMaxSecondsPerFire(String workflowClassName);

    /**
     * Maximum number of retries when selecting ready workflow.
     */
    int getMaxRetriesPerSelect();

    /**
     * Check value of workflow metadata.
     */
	boolean getCheckMetadata();

    /**
     * Only samples between 0-100 is allowed - everything else returns 0 = no sampling.
     */
	int getWorkflowSample();

    /**
     * Serializer class name for the given workflow
     *
     * Looks for configuration of serializer for workflowClassName, by looking up:
     * 1. specific serializer for workflowClassName (workflow.serializer.a.b.c.SomeWorkFlow),
     * 2. any package serializer up to root (workflow.serializer. + a.b.c or a.b or a),
     * 3. and finally just any workflow.serializer configuration.
     */
	String getSerializer(String workflowClassName);
}
