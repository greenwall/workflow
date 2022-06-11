package com.nordea.dompap.workflow;

import com.nordea.dompap.workflow.boot.WorkflowContentInitializer;
import com.nordea.dompap.workflow.util.WorkflowProcessingStatus;
import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.UserId;
import lombok.extern.slf4j.Slf4j;

import javax.resource.ResourceException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * In memory manager for test purposes.
 * Not thread-safe because the workflow instance is stored in member during execution.
 */
@Slf4j
public class MemoryWorkflowManager implements WorkflowManager {
    private static final String IGNORE_LOOPING_MONITORING_WF="loadWorkflowSnapshot";

    private Workflow<?> currentWorkFlow;
    
	@Override
    public <T> Workflow<T> startImmediate(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId, T process, Method entry) {
		//WorkFlow<T> workflow = ServiceFactory.getService(WorkFlowService.class).insertWorkFlow(id, externalKey, userId, branchId, content, entry);				
		Date creationTime = new Date();
		Date lastUpdateTime = new Date();		
		Workflow<T> workflow = new Workflow<T>(id, externalKey, process.getClass().getName(), userId, branchId, creationTime, lastUpdateTime) {
			private static final long serialVersionUID = -8888741881798788103L;

			@Override
			public Metadata loadMetadata() {
				return new Metadata(null) {
					private static final long serialVersionUID = -5974644242071162619L;
					@Override
					public void putProperty(PropertyType type, String value) {
					}
					@Override
					public void putProperty(String type, String value) {
					}
				};
			}
		};
		
		workflow.setContent(process);		

		log.info("startImmediate: id="+workflow.getId()+" userId="+workflow.getUserId()+" step:"+workflow.getMethodName());
		return execute(workflow, entry);
	}

	@Override
	public <T> Workflow<T> startImmediate(WorkflowBuilder<T> builder) {
		return startImmediate(
				builder.id,
				builder.externalKey,
				builder.userId,
				builder.requestDomain, 
				builder.branchId, 
				builder.workflow, 
				WorkflowUtil.getMethod(builder.workflow, builder.methodName));
	}

	@Override
	public <T> Workflow<T> start(WorkflowBuilder<T> builder) throws ResourceException, IOException {
		return start(
				builder.id, 
				builder.externalKey,
				builder.userId,
				builder.requestDomain, 
				builder.branchId,
				builder.workflow,
				builder.subType,
				WorkflowUtil.getMethod(builder.workflow, builder.methodName),
				builder.startWhen, 
				builder.metadata);
	}

	@Override
	public <T> Workflow<T> create(WorkflowBuilder<T> builder) throws ResourceException, IOException {
		return create(
				builder.id,
				builder.externalKey,
				builder.userId,
				builder.requestDomain,
				builder.branchId,
				builder.workflowClassName,
				builder.subType,
				builder.workflow,
				builder.methodName,
				builder.startWhen, 
				builder.metadata);
	}
	
    @Override
	public <T> Workflow<T> start(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId, T process, String subType, Method entry, Date startWhen, Metadata metadata) throws ResourceException, IOException {
		return startImmediate(id, externalKey, userId, requestDomain, branchId, process, entry);
	}	

	@Override
	public <T> Workflow<T> create(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId,
								  String workflowClassName, String subType, T content, String startMethod, Date startWhen, Metadata metadata)
			throws ResourceException, IOException {
		try {
			Class<?> c = Class.forName(workflowClassName);
			Method entry = WorkflowUtil.getMethod(c, startMethod);
			return startImmediate(id, externalKey, userId, requestDomain, branchId, content, entry);			
		} catch (ClassNotFoundException e) {
			throw new ResourceException(e);
		}
	}
    
	private <T> Workflow<T> execute(Workflow<T> workflow, Method step) {
		currentWorkFlow = workflow;
		WhenMethod nextMethod;
		do {
			try {
				if (methodHasWorkFlowParameter(step)) {
					nextMethod = mapToWhenMethod(step.invoke(workflow.getContent(), workflow));
				} else {
					nextMethod = mapToWhenMethod(step.invoke(workflow.getContent()));
				}
				if(nextMethod!=null){
				    if(nextMethod.when != null && nextMethod.method.getName().equals(IGNORE_LOOPING_MONITORING_WF)){
				        nextMethod=null;
				    }else{
				        step = nextMethod.method;   
				    }
				   
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);				
			} catch (InvocationTargetException e) {
				log.info("Exception during step: "+step.getName(), e.getTargetException());
				throw new RuntimeException(e);				
			}
		} while (nextMethod!=null);
		return workflow;
	}

	private static boolean methodHasWorkFlowParameter(Method step) {
		Class[] parameterTypes = step.getParameterTypes();
		return parameterTypes.length==1 && parameterTypes[0].equals(Workflow.class);
	}

	@Override
	public WorkflowProcessingStatus pickAndExecute(String workflowClassName, String subType, WorkflowContentInitializer workFlowInitializer) {
		return WorkflowProcessingStatus.idle;
	}

	@Override
	public <T> Workflow<T> pick(String workflowClassName, String subType) {
		return null;
	}
	
	@Override
	public <T> List<Workflow<T>> pick(String workflowClassName, String subType, int maxCount) {
		return Collections.emptyList();
	}
	
	@Override
	public <T> WorkflowProcessingStatus execute(Workflow<T> workflow, WorkflowContentInitializer workFlowContentInitializer) throws ResourceException {
		return WorkflowProcessingStatus.idle;
	}
	
	@Override
	public <T> Workflow<T> resumeAt(Workflow<T> workflow, Method method, Date startWhen) throws ResourceException {
		return execute(workflow, method);
	}

	@Override
	public <T> Workflow<T> resumeAt(Workflow<T> workflow, String method, Date startWhen) throws ResourceException {
		Class<?> clazz;
		try {
			clazz = Class.forName(workflow.getWorkflowClassName());
		} catch (ClassNotFoundException e) {
			throw new ResourceException(e);
		}
		Method step = WorkflowUtil.getMethod(clazz, method);
		return execute(workflow, step);
	}

	@Override
	public <T> Workflow<T> moveAfter(Workflow<T> workflow, String method) {
		return workflow;
	}
	
	@Override
	public <T> Workflow<T> retryAt(Workflow<T> workflow, Method method, Date startWhen, Throwable exception) {
		return execute(workflow, method);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Workflow<?> getWorkFlow(UUID uuid) throws ResourceException, ClassNotFoundException, IOException {
		return currentWorkFlow;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Workflow<?> getWorkFlowOnly(UUID uuid) {
		return currentWorkFlow;
	}
	
	@Override
	public void updateExternalKey(UUID workflowId, String externalKey) {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public Workflow<?> findWorkFlowByExternalKey(String externalKey) {
		// TODO Auto-generated method stub
		return null;
	}
	 private static WhenMethod mapToWhenMethod(Object returnValue) {
	        if (returnValue instanceof Method) {
	            return new WhenMethod((Method) returnValue);
	        } else {
	            if (returnValue instanceof WhenMethod) {
	                return (WhenMethod) returnValue;
	            } else {
	                // Unknonw return type.
	                log.info("Method returned value=" + returnValue + ". Assume no further methods to execute.");
	                return null;
	            }
	        }
	    }

	@Override
	public <T> void queueEvent(Workflow<T> workflow, UUID eventId) throws ResourceException {
		// TODO Auto-generated method stub
		
	}
}
