package com.nordea.dompap.workflow.boot;

import com.nordea.dompap.workflow.WorkFlow;
import com.nordea.dompap.workflow.WorkFlowManager;
import com.nordea.dompap.workflow.config.WorkFlowConfig;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.service.ServiceException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import javax.resource.ResourceException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * Compared to GenericWorkFlowExecutor, this one offloads processing of each
 * workflow to an executor which can handle up to workflow.maxthreads jobs at a time.
 * The ThreadPoolWorkFlowExecutor can thus run many concurrent jobs with no extra burden
 * on the database when idle since only a single (or a few) threads will be selecting
 * new workflows for the database.
 *
 * <pre>
 * workflow.jobs=com.nordea.branchchannel.dpap.workflow.TestWorkFlow
 * workflow.maxthreads=40
 * </pre>
 *
 * Change the workflow.maxthreads to decide how many workflows you will execute concurrently.
 */
@Slf4j
public class ThreadPoolWorkFlowExecutor {

    private static boolean isWorkflowClassesLogged = false;

    private static ThreadPoolExecutor executor;
    private final WorkFlowConfig config;
    private final WorkFlowManager workFlowManager;
    private final WorkFlowExecutorObserver observer;

    private final SchedulerFactory schedulerFactory = new StdSchedulerFactory();
    private final Scheduler scheduler;


    public ThreadPoolWorkFlowExecutor(WorkFlowManager manager, WorkFlowConfig config) {
        this(manager, config, null);
    }

    public ThreadPoolWorkFlowExecutor(WorkFlowManager manager, WorkFlowConfig config, WorkFlowExecutorObserver observer) {
        this.config = config;
        this.workFlowManager = manager;
        this.observer = observer;
        initThreadPool();

        try {
            scheduler = schedulerFactory.getScheduler();
            scheduler.setJobFactory((triggerFiredBundle, scheduler) -> new WorkFlowJob(this));
        } catch (SchedulerException e) {
            throw new NestableRuntimeException(e);
        }

    }

    public void initThreadPool() {
        executor = new ThreadPoolExecutor(config.getWorkflowMaxThreads(), config.getWorkflowMaxThreads(), 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        executor.allowCoreThreadTimeOut(true);

        executor.setMaximumPoolSize(config.getWorkflowMaxThreads());
        executor.setCorePoolSize(config.getWorkflowMaxThreads());
    }

    public void startWorkFlowEngine() {
        String schedule = config.getSchedule();
        log.info("Schedule for workflow processing:" + schedule);
        if (StringUtils.isBlank(schedule)) {
            log.error("Missing schedule for workflow.schedule");
        } else {
            for (int triggers=0; triggers<config.getWorkflowJobTriggers(); triggers++) {


                JobDetail jobDetail = JobBuilder.newJob(WorkFlowJob.class)
                        .withIdentity("job1", "group1")
                        .build();
//	                jobDetail.getJobDataMap().put(WORKFLOW_MODULE, currentModuleId.getId());

                CronTrigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity("trigger1", "group1")
                        .withSchedule(CronScheduleBuilder.cronSchedule(schedule))
                        .forJob(jobDetail)
                        .build();

                try {
                    scheduler.scheduleJob(jobDetail, trigger);
                    scheduler.start();
                } catch (SchedulerException e) {
                    throw new NestableRuntimeException(e);
                }
            }
        }
    }

    public static class WorkFlowJob implements Job {

        private final ThreadPoolWorkFlowExecutor executor;

        public WorkFlowJob(ThreadPoolWorkFlowExecutor executor) {
            this.executor = executor;
        }

        public void execute(JobExecutionContext context) throws JobExecutionException {
            log.debug("WorkFlowJob:execute");
            long startTime = System.currentTimeMillis();
            try {
                JobDataMap data = context.getJobDetail().getJobDataMap();
//                String wfModule = data.getString(WORKFLOW_MODULE);
                // TODO Introduce hook to add callback on startup
                //WatchDogProcess.iAmAlive(wfModule);
//                executeEnabledWorkflows(moduleId);

                // TODO
                executor.executeEnabledWorkflows();
            } catch (ServiceException | ResourceException e) {
                throw new JobExecutionException(e);
            }
            log.info("WorkFlowJob:execute time={}", System.currentTimeMillis() - startTime);
        }
    }

    private boolean isPoolFull() {
        return executor.getActiveCount() >= executor.getMaximumPoolSize();
    }

    /**
     * Iterate configured workflow classes and execute as long as a workflow is ready.
     * A maximum of workflows to execute are configurable (workflow.maxExecutions=XXX).
     * Disabled by setting to 0.
     * A maximum total execution time (for all instances) are configurable (workflow.maxSeconds=XXX).
     * Disabled by setting to 0.
     */
    private void executeEnabledWorkflows() throws ResourceException {
        long logStartTime = System.currentTimeMillis();
        // workflow will update its own status that means workflow is alive
        // so that information will be used by watchdog

        String[] workflowClasses = config.getJobs();
        String serverName = getCurrentServerName();
        // Only log the workflow classes once.
        if (!isWorkflowClassesLogged) {
            log.info("Workflows for " + serverName + "=" + StringUtils.join(workflowClasses, ";"));
            isWorkflowClassesLogged = true;
        }
        if (workflowClasses != null) {
            int workflowsExecuted = 0;
            for (String workflowClassNameSubType : workflowClasses) {
                String subType = StringUtils.substringAfter(workflowClassNameSubType, ":");
                String workflowClassName = StringUtils.substringBefore(workflowClassNameSubType, ":");

                int maxWorkflows = config
                        .getMaxWorkflowsPerFire(getWorkflowClassNameWithoutPackage(workflowClassName));
                int maxSeconds = config
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
                        if (isPoolFull()) {
                            return;
                        }
                    }
                    //Monitor.timeUsed("Concurrent workflows", executor.getActiveCount());

                    int maxCount = executor.getMaximumPoolSize() - executor.getActiveCount();
                    if (maxCount < 1) {
                        maxCount = 1;
                    }

                    workflows = workFlowManager.pick(workflowClassName, subType, maxCount);
                    if (workflows != null) {
                        for (WorkFlow<Object> workflow : workflows) {
                            executor.execute(() -> executeSingleWorkflow(workflow));
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
        log.info("WorkFlowJob:executeEnabledWorkflows time={}", System.currentTimeMillis() - logStartTime);
    }

    /**
     * Executes a single workflow.
     */
    private void executeSingleWorkflow(WorkFlow<Object> workflow) {
        long startTime = System.currentTimeMillis();
        try {
            if (observer!=null) {
                observer.preExecute(workflow);
            }

            workFlowManager.execute(workflow);

        } catch (ResourceException e) {
            log.warn("Problem executing workflow: " + workflow.getWorkflowClassName() + ", " + workflow.getId(), e);
        } finally {
            if (observer!=null) {
                observer.postExecute(workflow);
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("executeSingleWorkflow time={}", duration);
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
        // TODO
        return "unknown-server";
/*
        String thisAppServerName = EnvironmentController.getServerName();
        if (thisAppServerName == null) {
            thisAppServerName = EnvironmentController.getCurrentEnvironment().name();
        }
        return thisAppServerName.toLowerCase();

 */
    }

   
}