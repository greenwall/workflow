package com.nordea.dompap.workflow.boot;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Starts quartz jobs (NextScheduler) that executes enabled workflows
 * (WorkFlowConfigService) for the current server name. Following configuration
 * in dompap.properties controls which workflow classes are executed and how
 * often quartz executes:
 * 
 * <pre>
 * workflow.jobs=com.nordea.branchchannel.dpap.workflow.TestWorkFlow
 * workflow.triggers=1
 * workflow.schedule=0/15/30/45\ *\ *\ ?\ *\ *
 * workflow.maxthreads=40
 * </pre>
 * 
 * Compared to GenericWorkFlowExecutor, this one offloads processing of each
 * workflow to an executor which can handle up to workflow.maxthreads jobs at a time.
 * The FastWorkFlowExecutor can thus run many concurrent jobs with no extra burden
 * on the database when idle since only a single (or a few) threads will be selecting
 * new workflows for the database.
 * 
 * You should only have this in a single or in few cases two quartz threads.
 * 
 * Change the workflow.maxthreads to decide how many workflows you will execute concurrently.
 * 
 */
@SuppressWarnings("deprecation")
@Slf4j
public class FastWorkflowExecutor {

    private static boolean isWorkflowClassesLogged = false;
    public static final String WORKFLOW_MODULE = "workflow.module";

    private static ThreadPoolExecutor executor;

    public static void startWorkFlowEngine() {
/*
        // Pass current module to workflow job - this could be set differently
        // for different workflows!
        ModuleId currentModuleId = ModuleHolder.get().getCurrentModule();
        String schedule = WorkFlowConfig.getSchedule();
        try {
        	executor = new ThreadPoolExecutor(WorkFlowConfig.getWorkflowMaxThreads(), WorkFlowConfig.getWorkflowMaxThreads(), 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        	executor.allowCoreThreadTimeOut(true);
        	
        	// React to any changes in threadpool size
        	NextConfigurationV2.getCurrent().addConfigurationListener(mid -> {
        		ModuleHolder.get().pushCurrentModule(currentModuleId);
        		try {
	        		executor.setMaximumPoolSize(WorkFlowConfig.getWorkflowMaxThreads());
	        		executor.setCorePoolSize(WorkFlowConfig.getWorkflowMaxThreads());
        		} catch (NoActiveModuleException e) {
					log.error("This cannot happen - module ID is set", e);
				} finally {
        			ModuleHolder.get().popCurrentModule();
        		}
        	});
        	
            log.info("Schedule for workflow processing:" + schedule);

            if (StringUtils.isBlank(schedule)) {
                log.error("Missing schedule for workflow.schedule");
            } else {
            	for (int triggers=0; triggers<WorkFlowConfig.getWorkflowJobTriggers(); triggers++) {
	                JobDetail jobDetail = JobBuilder.newJob(WorkFlowJob.class).build();
	                jobDetail.getJobDataMap().put(WORKFLOW_MODULE, currentModuleId.getId());
	                NextScheduler.getInstance().schedule(jobDetail, schedule);
            	}
            }
        } catch (SchedulerException e) {
            log.error("Unable to schedule workflows", e);
        }

 */
    }
/*
    public static class WorkFlowJob implements Job {
        public void execute(JobExecutionContext context) throws JobExecutionException {
            long startTime = System.currentTimeMillis();
            try {
                JobDataMap data = context.getJobDetail().getJobDataMap();
                String wfModule = data.getString(WORKFLOW_MODULE);
                ModuleId moduleId = ModuleId.of(wfModule);
                // TODO Introduce hook to add callback on startup
                //WatchDogProcess.iAmAlive(wfModule);
                executeEnabledWorkflows(moduleId);
            } catch (ResourceException | ServiceException e) {
                throw new JobExecutionException(e);
            } finally {
            	ErrorDiagnostics.clear();
            }
            log.info("WorkFlowJob:execute time={}", System.currentTimeMillis()-startTime);
        }

        private boolean isPoolFull() {
        	return executor.getActiveCount() >= executor.getMaximumPoolSize();
        }

        /**
         * Iterate configured workflow classes and execute as long as a workflow
         * is ready. A maximum of workflows to execute are configurable
         * (workflow.maxExecutions=XXX). Disabled by setting to 0. A maximum
         * total execution time (for all instances) are configurable
         * (workflow.maxSeconds=XXX). Disabled by setting to 0.
         */
/*
        private void executeEnabledWorkflows(ModuleId moduleId) throws ResourceException, ServiceException {
            long logStartTime = System.currentTimeMillis();
            try {
                ModuleHolder.get().pushCurrentModule(moduleId);
             // workflow will update its own status that means workflow is alive
                // so that information will be used by watchdog
                
                String[] workflowClasses = WorkFlowConfig.getJobs();
                String serverName = getCurrentServerName();
                // Only log the workflow classes once.
                if (!isWorkflowClassesLogged){
                    log.info("Workflows for " + serverName + "=" + StringUtils.join(workflowClasses, ";"));
                    isWorkflowClassesLogged = true;
                }
                if (workflowClasses != null) {
                	int workflowsExecuted = 0;
                    for (String workflowClassNameSubType : workflowClasses) {
                    	String subType = StringUtils.substringAfter(workflowClassNameSubType, ":");

 */
    /*
                    	String workflowClassName = StringUtils.substringBefore(workflowClassNameSubType, ":");
                    	                    	
                        int maxWorkflows = workFlowConfig
                                .getMaxWorkflowsPerFire(getWorkflowClassNameWithoutPackage(workflowClassName));
                        int maxSeconds = workFlowConfig
                                .getMaxSecondsPerFire(getWorkflowClassNameWithoutPackage(workflowClassName));
                        long startTime = System.currentTimeMillis();
                        long durationMillis = 0;

                        List<WorkFlow<Object>> workflows;
                        do {
        					if (isPoolFull()) {
        						// All threads are in use already, wait a bit and give
        						// up if nothing is available trusting we will be called later.
        						int count = 0;
        						while (count < 500 && isPoolFull()) {
        							try {
        								Thread.sleep(10);
        							} catch (InterruptedException ignored) {
        							}
        							++count;
        						}
        						if (isPoolFull())
        							return;
        					}
        					Monitor.timeUsed("Concurrent workflows", executor.getActiveCount());

        					int maxCount = executor.getMaximumPoolSize() - executor.getActiveCount();
        					if (maxCount < 1)
        						maxCount = 1;
        					
        					workflows = ServiceFactory.getService(WorkFlowManager.class).pick(workflowClassName, subType, maxCount);
        	                if (workflows != null) {
        	                	for(WorkFlow<Object> workflow : workflows) {
	        	                	executor.execute(() -> executeSingleWorkflow(moduleId, workflow));
	        	                	workflowsExecuted++;
	        	                	durationMillis = System.currentTimeMillis() - startTime;
        	                	}
        	                }
                        } while (workflows != null
                        	&& !workflows.isEmpty()
                            && (workflowsExecuted < maxWorkflows || maxWorkflows <= 0)
                            && (durationMillis / 1000 < maxSeconds || maxSeconds <= 0));
                        
                    }
                }
            } finally {
                ModuleHolder.get().cleanup();
                ErrorDiagnostics.clear();
            }
            log.info("WorkFlowJob:executeEnabledWorkflows time={}", System.currentTimeMillis()-logStartTime);
        }

        /**
         * Executes a single workflow. Pushes given module and clears all
         * modules after.
         */
/*
        private void executeSingleWorkflow(ModuleId wfModule, WorkFlow<Object> workflow) {
            long startTime = System.currentTimeMillis();
            try {
                ModuleHolder.get().pushCurrentModule(wfModule);

                ServiceFactory.getService(WorkFlowManager.class).execute(workflow);
            } catch (ResourceException | ServiceException e) {
    			log.warn("Problem executing workflow: " + workflow.getWorkflowClassName() + ", " + workflow.getId(), e);
			} finally {
                // Ensure that all Monitor.stop() were called.
                MonitorObject mo = Monitor.stopAll();

                // Slow execution - may include several steps in the workflow
                if ((mo != null) && (mo.endTime - mo.startTime > 120000)) {
                    LoggerFactory.getLogger(getClass())
                            .warn("WorkflowJob detected slow execution, details:\n" + mo.toString());
                }
                ErrorDiagnostics.clear();
                LogMDCInjector.clearLogValues();

                ModuleHolder.get().popCurrentModule();
                log.info("WorkFlowJob:executeSingleWorkflow time={}", System.currentTimeMillis()-startTime);
                
                ErrorDiagnostics.clear();
            }
        }
    }
    
    /**
     * This method will return class name when parameter value pass as
     * packageName+className.
     */
    private static String getWorkflowClassNameWithoutPackage(String workflowClassName) {
        String className = null;
        if (workflowClassName != null) {
            className = workflowClassName.substring(workflowClassName.lastIndexOf(".") + 1);
        }
        return className;
    }

    private static String getCurrentServerName() {
        // TODO Current server name
        return "server-unknown";
/*
        String thisAppServerName = EnvironmentController.getServerName();
        if (thisAppServerName == null) {
            thisAppServerName = EnvironmentController.getCurrentEnvironment().name();
        }
        return thisAppServerName.toLowerCase();

 */
    }

   
}