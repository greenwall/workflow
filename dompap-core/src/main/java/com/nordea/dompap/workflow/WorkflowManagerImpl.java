package com.nordea.dompap.workflow;

import com.google.common.base.MoreObjects;
import com.nordea.dompap.workflow.boot.WorkflowContentInitializer;
import com.nordea.dompap.workflow.config.WorkflowConfig;
import com.nordea.dompap.workflow.content.StringWorkflowContentSerializer;
import com.nordea.dompap.workflow.content.WorkflowContentSerializer;
import com.nordea.dompap.workflow.util.WorkflowProcessingStatus;
import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import javax.resource.ResourceException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class WorkflowManagerImpl implements WorkflowManager {

    private final WorkflowConfig config;
    private final WorkflowService workFlowService;

    /**
     * Stores a new workflow instance and executes this in the current thread
     * before returning.
     * @deprecated Use startImmediate(WorkFlowBuilder)
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    @Override
    public <T> Workflow<T> startImmediate(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId, T content,
                                          Method entry) throws ResourceException, IOException {
        WorkflowController controller = createController(content.getClass());
        Workflow<T> workflow = workFlowService.insertWorkFlow(id, externalKey, userId, requestDomain, branchId, content, entry, null, null, controller);
        logWorkflowInfo("startImmediate", workflow);
        return execute(workflow, entry, controller);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Workflow<T> startImmediate(WorkflowBuilder<T> builder) throws ResourceException, IOException {
    	if (builder.controller==null) {
    		builder = builder.controller(createController(builder.workflow.getClass()));
    	}
        Workflow<T> workflow = workFlowService.insertWorkFlow(builder);
        logWorkflowInfo("startImmediate", workflow);                
        Method entry = WorkflowUtil.getMethod(builder.workflow, builder.methodName);
        return execute(workflow, entry, builder.controller);
    }
    
    @Override
	public <T> Workflow<T> start(WorkflowBuilder<T> builder) throws ResourceException, IOException {
        WorkflowController controller = createController(builder.workflow.getClass());
        builder = builder.controller(controller);
        Workflow<T> workflow = workFlowService.insertWorkFlow(builder);
        logWorkflowInfo("start", workflow);        
        return workflow;
    }

	/**
     * Stores a new workflow instance marking it ready and returns without
     * executing it.
     * Threads/background jobs will call pickAndExecute to obtain and execute
     * any ready workflow instance (one at a time).
     * @deprecated Use start(WorkFlowBuilder)
     * @param entry method to execute
     * @param startWhen when to execute method entry
     */
	@Deprecated
    @Override
	public <T> Workflow<T> start(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId, T content,
                                 String subType, Method entry, Date startWhen, Metadata metadata) throws ResourceException, IOException {
        WorkflowController controller = createController(content.getClass());
        Workflow<T> workflow = workFlowService.insertWorkFlow(id, externalKey, userId, requestDomain, branchId, content, subType, entry, startWhen, metadata, controller);
        logWorkflowInfo("start", workflow);
        return workflow;
    }

    /**
     * @deprecated Use create(WorkFlowBuilder)
     */
    @Deprecated
    @Override
	public <T> Workflow<T> create(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId, String workflowClassName, String subType,
                                  T content, String startMethod, Date startWhen, Metadata metadata) throws ResourceException, IOException {
        WorkflowController controller = createController(content.getClass());
        WorkflowContentSerializer contentSerializer = new StringWorkflowContentSerializer();
        Workflow<T> workflow = workFlowService.insertWorkFlow(id, externalKey, userId, requestDomain, branchId, workflowClassName, subType, contentSerializer, content, startMethod, startWhen, metadata, controller);
        logWorkflowInfo("create", workflow);
        return workflow;
    }

    @Override
	public <T> Workflow<T> create(WorkflowBuilder<T> builder) throws ResourceException, IOException {
		if (builder.controller == null) {
			WorkflowController controller;
			try {
				controller = createController(Class.forName(builder.workflowClassName));
				builder = builder.controller(controller);
			} catch (ClassNotFoundException e) {
				builder = builder.controller(new DefaultWorkflowController(config, this));
			}
		}
		if (builder.contentSerializer == null) {
			WorkflowContentSerializer contentSerializer = new StringWorkflowContentSerializer();
			builder = builder.contentSerializer(contentSerializer);
		}
		Workflow<T> workflow = workFlowService.insertWorkFlow(builder);
		logWorkflowInfo("create", workflow);

		return workflow;
	}
    
    @SuppressWarnings("rawtypes")
	private WorkflowController createController(Class workflowClass) {
        String ctrlName = null;
        try {
            ctrlName = config.getControllerFor(workflowClass.getName());
            if (StringUtils.isBlank(ctrlName)) {
                ctrlName = config.getControllerFor(workflowClass.getSimpleName());
            }
            if (!StringUtils.isBlank(ctrlName)) {
                Class ctrlClass = Class.forName(ctrlName);
                Object ctrl = ctrlClass.getDeclaredConstructor().newInstance();
                if (!(ctrl instanceof WorkflowController)) {
                    log.error("Controller for workflow:{}={} does not implement WorkFlowController", workflowClass, ctrlName);
                    return null;
                }
                return (WorkflowController) ctrl;
            }
        } catch (ClassNotFoundException e) {
            log.error("Controller for workflow:{}={} not found", workflowClass, ctrlName, e);
            return null;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            log.error("Controller for workflow:{}={} can't be instantiated (missing default constructor?)",
                    workflowClass, ctrlName, e);
            return null;
        }
        if (ctrlName == null) {
            // If controller config is left out, use default controller.
            return new DefaultWorkflowController(config, this);
        } else {
            // If controller config is set to blank, use no controller.
            return null;
        }
    }

    /**
     * Picks a ready workflow instance (starttime==null) and executes it until a
     * method returns null (or throws exception).
     * This instance will then be updated with end time and any method
     * exception.
     * So instances with endtime != null and exception ==null have run to
     * completion or is waiting for external resume.
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public WorkflowProcessingStatus pickAndExecute(String workflowClassName, String subType, WorkflowContentInitializer workFlowInitializer) throws ResourceException {
        Workflow workflow = pick(workflowClassName, subType);
        if (workflow != null) {
            return execute(workflow, workFlowInitializer);
        } else {
            log.trace("pickAndExecute: idling (No ready workflows of class:{} and subtype:{}) ", workflowClassName, subType);
            return WorkflowProcessingStatus.idle;
        }
    }

    @Override
    public <T> List<Workflow<T>> pick(String workflowClassName, String subType, int maxCount) throws ResourceException {
        WorkflowTimer pickReadyTimer = new WorkflowTimer(workflowClassName);
        List<Workflow<T>> workflows = workFlowService.pickReadyWorkFlows(workflowClassName, subType, maxCount);
        for(Workflow<T> workflow : workflows) {
        	pickReadyTimer.logTime("pickReady", workflow);
        }
        return workflows;
    }
    @Override
    public <T> Workflow<T> pick(String workflowClassName, String subType) throws ResourceException {
        WorkflowTimer pickReadyTimer = new WorkflowTimer(workflowClassName);
        Workflow<T> workflow = workFlowService.pickReadyWorkFlow(workflowClassName, subType);
        pickReadyTimer.logTime("pickReady", workflow);
        return workflow;
    }
    
    @Override
    @SuppressWarnings({"deprecation"})
    public <T> WorkflowProcessingStatus execute(Workflow<T> workflow, WorkflowContentInitializer workFlowInitializer) throws ResourceException {
        log.info("execute: id={} userId={} step:{} class:{} subtype:{}", workflow.getUserId(), workflow.getId(), workflow.getMethodName(), workflow.getWorkflowClassName(), workflow.getSubType());
        T content;
        WorkflowController controller;
        Method method = null;
        try {
            if (isArchiveWorkFlow(workflow)) {                    
                logWorkflowInfo("pickAndExecute: Archiving", workflow);
                WorkflowTimer archiveTimer = new WorkflowTimer(workflow);
                workFlowService.archiveWorkFlow(workflow);
                archiveTimer.logTime("archive");
                return WorkflowProcessingStatus.hasMore;
            } else {
            	// No need to load content and controller when archiving.
            	WorkflowTimer loadTimer = new WorkflowTimer(workflow);
                content = workFlowService.loadWorkFlowContent(workflow);
                if (workFlowInitializer!=null) {
                    workFlowInitializer.initialize(content);
                }
                controller = workFlowService.loadWorkFlowController(workflow);
                workflow.setContent(content);
                method = WorkflowUtil.getMethodChecked(workflow.getContent(), workflow.getMethodName());
                loadTimer.logTime("load");
            }
        } catch (ResourceException | ClassNotFoundException | NoClassDefFoundError | IOException | NoSuchMethodException | RuntimeException e) {
        	// Catch exceptions on loading workflow, content and controller to report them.
            log.error("Workflow:[{}] failed in step:{}", workflow.getId(), method != null ? method.getName() : "-", e);
            workFlowService.updateWorkFlow(workflow, workflow.getMethodName(), null, new Date(), new Date(), e, null);
            return WorkflowProcessingStatus.done;
        }
        if (content != null) {
            execute(workflow, method, controller);
            return WorkflowProcessingStatus.hasMore;
        } else {
            workFlowService.updateWorkFlow(workflow, workflow.getMethodName(), null, new Date(), new Date(), new NoSuchMethodException(workflow.getMethodName()), null);
            return WorkflowProcessingStatus.done;
        }
    }

    @SuppressWarnings("rawtypes")
	private boolean isArchiveWorkFlow(Workflow workflow) {
        return workflow.getMethodName().startsWith("!");
    }

    @Override
    public <T> Workflow<T> resumeAt(Workflow<T> workflow, String method, Date startWhen) throws ResourceException {
    	workFlowService.updateWorkFlow(workflow, method, startWhen, null, null, null, null);
    	workflow.setMethodName(method);
    	workflow.setMethodStarted(null);
    	workflow.setMethodEnded(null);
    	return workflow;
    }    
    
    @Override
    public <T> Workflow<T> resumeAt(Workflow<T> workflow, Method method, Date startWhen) throws ResourceException {
    	// TODO Consider why this resumeAt saves content and the other does not. On retry content should not be saved!?!?!?!?
        if (workflow.getContent() != null) {
            workFlowService.saveWorkFlowContent(workflow);
        }
        // Passing null as startWhen means resume immediately.
        if (startWhen==null) {
            startWhen = new Date();
        }
        workFlowService.updateWorkFlow(workflow, method, startWhen, null, null, null, null);
        workflow.setMethodName(method.getName());
        workflow.setMethodStarted(null);
        workflow.setMethodEnded(null);
        return workflow;
    }
    
	/**
	 * Queue an event for the workflow, when it's ready to process it. 
	 */
    @Override
	public <T> void queueEvent(Workflow<T> workflow, UUID eventId) throws ResourceException {
        workFlowService.queueEvent(workflow, eventId);
    }
    

    @Override
	public <T> Workflow<T> moveAfter(Workflow<T> workflow, String method) throws ResourceException {
    	Date now = new Date();
    	workFlowService.updateWorkFlow(workflow, method, null, now, now, null, null);
    	workflow.setMethodName(method);
    	workflow.setMethodStarted(now);
    	workflow.setMethodEnded(now);
    	return workflow;
	}
    
    /**
     * Used when method failed and controller decides to retry.
     * In contrast to resumeAt the exception from the failing method is kept, to allow distinguishing:
     * - workflows postponed for LATER from 
     * - workflows waiting for RETRY 
     * @see WorkflowService#getWorkflowStatus
     * TODO Consider to leave the exception unchanged instead of writing the same exception again to database.
     */
    @Override
    public <T> Workflow<T> retryAt(Workflow<T> workflow, Method method, Date startWhen, Throwable methodException) throws ResourceException {
        workFlowService.updateWorkFlow(workflow, method, startWhen, null, null, methodException, null);
        workflow.setMethodName(method.getName());
        workflow.setMethodStarted(null);
        workflow.setMethodEnded(null);
        return workflow;
    }
    
    /**
     * Get Workflow without the content.
     */
    @Override
    public <T> Workflow<T> getWorkFlowOnly(UUID uuid) throws ResourceException {
        return workFlowService.getWorkFlow(uuid);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Workflow getWorkFlow(UUID uuid) throws ResourceException, ClassNotFoundException,
            IOException {
        Workflow workflow = workFlowService.getWorkFlow(uuid);
        if (workflow != null) {
            Object content = workFlowService.loadWorkFlowContent(workflow);
            workflow.setContent(content);
        }
        return workflow;
    }

    @Override
    public void updateExternalKey(UUID workflowId, String externalKey) throws ResourceException {
        workFlowService.updateWorkFlow(workflowId, externalKey);
    }

    /**
     * This workflow is reserved for the current execution,
     * and will not be picked up by other workflow threads until methodStarted
     * is set to null (and method is non-null).
     * The WorkFlowController is invoked prior to execution, after completion
     * and on failure.
     * WorkFlowController may call resumeAt to execute another method, which
     * will cause this execute method to return,
     * leaving the workflow to be picked for execution by the next execution.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    Workflow execute(Workflow workflow, Method step, WorkflowController controller) throws ResourceException {
    	// Package scoped method to allow testing
        Date startTime;
        WhenMethod nextMethod;
        do {
        	WorkflowTimer stepTimer = new WorkflowTimer(workflow);
            startTime = new Date();
            WorkflowTimer finalizeTimer = null;
            WorkflowTimer executeTimer = null;
            try {
                WorkflowTimer prepareTimer = new WorkflowTimer(workflow, step);
                logWorkflowInfo("execute", workflow, step.getName());
                workFlowService.updateWorkFlow(workflow, step, null, startTime, null, null, null);

                controller = controllerOn(new ControllerOnStart(workflow, step, controller));
                prepareTimer.logTime("prepare");

                // If controller called resumeAt - current workflow will have
                // startTime different from the startTime given above.
                if (!workflow.getMethodStarted().equals(startTime)) {
                    // Abort execution, since controller called resumeAt.
                    return workflow;
                }

                // Execute method and get next method.
                executeTimer = new WorkflowTimer(workflow, step);
                nextMethod = executeStep(workflow, step);
            	executeTimer.logTime("execute");
            	
                // Store workflow with endTime and content.
                finalizeTimer = new WorkflowTimer(workflow, step);
                Date endTime = new Date();
                // When nextMethod==null the workflow is ready to receive events and not final step
            	final boolean processEvents = (nextMethod==null) && !step.isAnnotationPresent(FinalState.class);

                // TODO Update workflow status and persisting state should happen in one transaction, to avoid possibility of failure/shutdown in between
                //workFlowService.updateWorkFlow(workflow, step, null, startTime, endTime, null, null, processEvents);
                //workFlowService.saveWorkFlowContent(workflow);

                // TODO Updating method completed separatedly from updating next method starting leaves a risk of "losing" the start execution on hard errors.
                workFlowService.stepExecutionCompleted(workflow, step, null, startTime, endTime, null, null, processEvents);

                // Controller may call resumeAt aborting execution of the returned nextMethod.
                controller = controllerOn(new ControllerOnComplete(workflow, step, nextMethod!=null?nextMethod.method:null, null, controller));

                // If controller called resumeAt - current workflow will have
                // startTime different from the startTime given above.
                if (!workflow.getMethodStarted().equals(startTime)) {
                    // Abort execution, since controller called resumeAt.
                	finalizeTimer.logTime("finalize");
                    return workflow;
                }

                if (nextMethod != null) {
                    // Next method given
                    if (nextMethod.when == null) {
                        // Execute next method immediately.
                        step = nextMethod.method;
                        log.debug("Execute next method immediately: {}", step);
                    } else {
                        // Postpone next method execution until given time, and return.
                    	// Delayed method exception may be conditional meaning if no events occur, so set processEvents if interruptable method.
                        log.info("Postpone next method={} execution until={} eventsInterrupt={}", nextMethod.method, nextMethod.when, nextMethod.eventsInterrupt);
                        workFlowService.updateWorkFlow(workflow, nextMethod.method, nextMethod.when, null, null, null, null, nextMethod.eventsInterrupt);
                        finalizeTimer.logTime("finalize");
                        return workflow;
                    }
                } else {
                    // Is step FinalState of workflow then archive
                    if (step.isAnnotationPresent(FinalState.class)) {
                        prepareArchivingWorkFlow(workflow, step, endTime);
                    }
                }
                finalizeTimer.logTime("finalize");
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // Exception during deserialization, looking up method or trying to invoke method.
                log.error("failure from: id={}, step:{}", workflow.getId(), step, e);
                Date endTime = new Date();
                workFlowService.updateWorkFlow(workflow, step, null, startTime, endTime, e, null, false);

                // Controller is not invoked since it was not the workflow failing but the execution logic.
                WorkflowTimer.logTimeFirstNonNull("error", finalizeTimer, executeTimer);
                return workflow;
            } catch (InvocationTargetException e) {
            	// Exception from execution of workflow
                log.error("exception from: id={} , step:{}", workflow.getId(), step, e.getTargetException());
                // Actual exception from Task method.
                Date endTime = new Date();
                workFlowService.updateWorkFlow(workflow, step, null, startTime, endTime, e.getTargetException(), null, false);

                // Controller may call resumeAt marking the workflow ready for executing another step. 
                // This will be picked up later.
                controllerOn(new ControllerOnFail(workflow, step, e.getTargetException(), controller));
                executeTimer.logTime("execute-fail");
                return workflow;
            } finally {
            	// Total time to prepare, execute and finalize step
            	stepTimer.logTime("step-total");
            }
        } while (nextMethod != null);

        return workflow;
    }

    private WhenMethod executeStep(Workflow workflow, Method step) throws InvocationTargetException, IllegalAccessException {

        if (methodHasWorkFlowParameter(step)) {
            return mapToWhenMethod(step.invoke(workflow.getContent(), workflow));
        } else {
            return mapToWhenMethod(step.invoke(workflow.getContent()));
        }
    }

    private static boolean methodHasWorkFlowParameter(Method step) {
        Class<?>[] parameterTypes = step.getParameterTypes();
        return parameterTypes.length==1 && parameterTypes[0].equals(Workflow.class);
    }

    private static WhenMethod mapToWhenMethod(Object returnValue) {
        if (returnValue instanceof Method) {
        	// Return immediate WhenMethod
            return new WhenMethod((Method) returnValue);
        } else {
            if (returnValue instanceof WhenMethod) {
                return (WhenMethod) returnValue;
            } else {
                // Unknown return type.
                log.info("Method returned value={}. No next method to execute.", returnValue);
                return null;
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void prepareArchivingWorkFlow(Workflow workflow, Method step, Date endTime) throws ResourceException {
        int archiveAfterDays = step.getAnnotation(FinalState.class).archiveAfterDays();
        Integer i = config.getArchiveAfterDays(step);
        if (i != null) {
            archiveAfterDays = i;
        }
        DateTime archiveWhen = new DateTime().plusDays(archiveAfterDays);
        String archiveStep = "!" + step.getName();

        //Monitor.timeUsed("Workflow finished", 1);
        
        log.info("Preparing to archive :{}, after {} days, at {}", workflow.getId(), archiveAfterDays, archiveWhen);

        // Workflow is finalized, so no events should be processed.
    	final boolean processEvents = false;        
        workFlowService.updateWorkFlow(workflow, archiveStep, archiveWhen.toDate(), null, endTime, null, null, processEvents);
    }

    /**
     * Executes the controller onEvent (onStart, onComplete, onFail).
     * If onEvent (onStart, onComplete or onFail) returns a controller, this
     * controller is stored and returned.
     * Otherwise the controller is not stored and returned as is, i.e. it's
     * state is considered unchanged.
     * If controller onEvent method fails the controller returned and the
     * failure is ignored.
     */
    private WorkflowController controllerOn(ControllerOnEvent controllerOnEvent) {
        log.debug("{}:{}", controllerOnEvent.getClass().getSimpleName(), controllerOnEvent);
        Workflow<?> workflow = controllerOnEvent.getWorkFlow();
        try {
            WorkflowController updatedController = controllerOnEvent.onEvent();
            if (updatedController != null) {
                // Store the updated controller state.
                workFlowService.saveWorkFlowController(workflow, updatedController);
                return updatedController;
            }
        } catch (Exception e) {
            log.error(controllerOnEvent + " failed for id=" + workflow.getId().toString(), e);
            // Ignore failing controller.
        }
        return controllerOnEvent.getController();
    }

    /**
     * Abstract the actual onXXXX call away a.k.a. "Function Pointer" Class
     * @author G93283
     */
    @SuppressWarnings("rawtypes")
    private abstract static class ControllerOnEvent {
        protected final Workflow workflow;
        protected final Method step;
        protected final WorkflowController controller;

        public ControllerOnEvent(Workflow workflow, Method step, WorkflowController controller) {
            this.workflow = workflow;
            this.step = step;
            this.controller = controller;
        }

        public Workflow getWorkFlow() {
            return workflow;
        }

        public WorkflowController getController() {
            return controller;
        }

        public abstract WorkflowController onEvent() throws ResourceException;

        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("step", step != null ? step.getName() : null)
                    .toString();
        }
    }

    @SuppressWarnings("rawtypes")
    private static final class ControllerOnStart extends ControllerOnEvent {
        public ControllerOnStart(Workflow workflow, Method step, WorkflowController controller) {
            super(workflow, step, controller);
        }

        @SuppressWarnings("unchecked")
		public WorkflowController onEvent() throws ResourceException {
            return controller != null ? controller.onStart(workflow, step) : null;
        }
    }

    private static final class ControllerOnComplete extends ControllerOnEvent {
        private final Method nextMethod;
        private final Date startWhen;

        @SuppressWarnings("rawtypes")
		public ControllerOnComplete(Workflow workflow, Method methodExecuted, Method nextMethod, Date startWhen,
                                    WorkflowController controller) {
            super(workflow, methodExecuted, controller);
            this.nextMethod = nextMethod;
            this.startWhen = startWhen;
        }

        @SuppressWarnings({ "unchecked" })
		public WorkflowController onEvent() throws ResourceException {
            return controller != null ? controller.onComplete(workflow, step, nextMethod, startWhen) : null;
        }
    }

    private static final class ControllerOnFail extends ControllerOnEvent {
        private final Throwable exception;

		public ControllerOnFail(Workflow<?> workflow, Method methodExecuted, Throwable exception,
                                WorkflowController controller) {
            super(workflow, methodExecuted, controller);
            this.exception = exception;
        }

        @SuppressWarnings("unchecked")
		public WorkflowController onEvent() throws ResourceException {
            return controller != null ? controller.onFail(workflow, step, exception) : null;
        }
    }

    @Override
    public <T> Workflow<T> findWorkFlowByExternalKey(String externalKey) throws ResourceException {
        return workFlowService.findWorkFlowByExternalKey(externalKey);
    }

    private static void logWorkflowInfo(String onMethod, Workflow<?> workflow ) {
        logWorkflowInfo(onMethod, workflow, workflow.getMethodName());
    }

    private static void logWorkflowInfo(String onMethod, Workflow<?> workflow, String methodName){
        UserId id = workflow.getUserId();
        String userId = id == null || id.getId() == null ? null : id.getId();
        log.info("{}:id={}, userId={}, step:{}", onMethod, workflow.getId(), userId, methodName);
    }

}
