package com.nordea.dompap.workflow.mock;

import com.nordea.dompap.workflow.*;
import com.nordea.dompap.workflow.content.WorkflowContentSerializer;
import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.UserId;
import org.joda.time.Interval;

import javax.resource.ResourceException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MockWorkflowService implements WorkflowService {

    @Override
    public <T> Workflow<T> getWorkFlow(UUID uuid, Class<T> interfaceClass) throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("unchecked")
	@Override
    public Workflow<?> getWorkFlow(UUID uuid) throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Workflow<T> findWorkFlowByExternalKey(String externalKey) throws ResourceException {
        return null;
    }

    @Override
    public <T> T loadWorkFlowContent(Workflow<T> workflow)
            throws ResourceException, ClassNotFoundException, IOException {
        return workflow.getContent();
    }

    @Override
    public <T> WorkflowController loadWorkFlowController(Workflow<T> workflow) throws ResourceException, ClassNotFoundException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> void saveWorkFlowController(Workflow<T> workflow, WorkflowController controller) throws ResourceException {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> Workflow<T> insertWorkFlow(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId,
                                          T workflowInstance, Method method, Date startWhen, Metadata metadata, WorkflowController controller)
            throws ResourceException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public <T> Workflow<T> insertWorkFlow(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId,
                                          String workflowClassName, String subType, WorkflowContentSerializer contentSerializer, T workflowInstance,
                                          String methodName, Date startWhen, Metadata metadata, WorkflowController controller)
			throws ResourceException, IOException {
		// TODO Auto-generated method stub
		return null;
	}
    
	@Override
	public <T> Workflow<T> insertWorkFlow(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId,
                                          String workflowClassName, WorkflowContentSerializer contentSerializer, T workflowInstance, String methodName, Date startWhen, Metadata metadata,
                                          WorkflowController controller) throws ResourceException, IOException {
		// TODO Auto-generated method stub
		return null;
	}    
        
    @Override
    public <T> void saveWorkFlowContent(Workflow<T> workflow) throws ResourceException {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> Metadata loadWorkFlowMetadata(Workflow<T> workflow) throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> void saveWorkFlowMetadata(Workflow<T> workflow, Metadata metadata) throws ResourceException {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void updateWorkFlow(Workflow<T> workflow, String methodName, Date startWhen, Date methodStarted,
                                   Date methodEnded, Throwable methodException, Metadata metadata) throws ResourceException {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void updateWorkFlow(Workflow<T> workflow, Method method, Date startWhen, Date methodStarted,
                                   Date methodEnded, Throwable methodException, Metadata metadata) throws ResourceException {
//        ServiceFactory.getService(WorkFlowService.class).updateWorkFlow(workflow, step, null, startTime, null, null, null);
    		workflow.setMethodName(method.getName());
    		workflow.setMethodStarted(methodStarted);
    		workflow.setMethodEnded(methodEnded);
    }

    @Override
    public <T> Workflow<T> pickReadyWorkFlow(String workflowClassName, String subType) throws ResourceException {
        return null;
    }

    @Override
    public <T> List<Workflow<T>> pickReadyWorkFlows(String workflowClassName, String subType, int maxCount) throws ResourceException {
    	return Collections.emptyList();
    }

    @SuppressWarnings("rawtypes")
	@Override
    public List<Workflow> getWorkFlows(UserId userId, BranchId branchId, Interval creationTime, Integer startRow,
                                       Integer maxRows) throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorkflowSearchResult searchWorkFlows(WorkflowSearch search, Integer startRow, Integer maxRows)
            throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorkflowSearchResult searchWorkFlowsAggregated(WorkflowSearch search, Integer startRow, Integer maxRows, Integer totalRows)
            throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateWorkFlow(UUID workflowId, String externalKey) throws ResourceException {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> List<WorkflowStepResult> getWorkFlowHistory(Workflow<T> workflow) throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<WorkflowMethodCount> getWorkflowStatus(int[] periodsInMinutes) throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<WorkflowMethodCount> getWorkflowStatus(int[] periods, String workflowClass) throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public List<WorkflowMethodCount> getWorkflowStatus(WorkflowStatusQuery query) throws ResourceException {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public <T> void archiveWorkFlow(Workflow<T> workflow) throws ResourceException {
        // TODO Auto-generated method stub

    }

	@Override
	public int updateWorkFlowLabel(WorkflowSearch search, WorkflowLabel label) throws ResourceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> T loadWorkFlowContent(Workflow<T> workflow, WorkflowContentSerializer contentSerializer)
			throws ResourceException, ClassNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void saveWorkFlowContent(Workflow<T> workflow, WorkflowContentSerializer contentSerializer)
			throws ResourceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void queueEvent(Workflow<T> workflow, UUID eventId) throws ResourceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void updateWorkFlow(Workflow<T> workflow, String methodName, Date startWhen, Date methodStarted,
                                   Date methodEnded, Throwable methodException, Metadata metadata, boolean processEvents)
			throws ResourceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void updateWorkFlow(Workflow<T> workflow, Method method, Date startWhen, Date methodStarted,
                                   Date methodEnded, Throwable methodException, Metadata metadata, boolean processEvents)
			throws ResourceException {
		// TODO Auto-generated method stub
		
	}

    @Override
    public <T> void stepExecutionCompleted(Workflow<T> workflow, Method method, Date startWhen, Date methodStarted, Date methodEnded, Throwable methodException, Metadata metadata, boolean processEvents) throws ResourceException {
        // Ignore
    }

    @Override
	public <T> Workflow<T> insertWorkFlow(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId,
                                          T workflowInstance, String subType, Method method, Date startWhen, Metadata metadata,
                                          WorkflowController controller) throws ResourceException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Workflow<T> insertWorkFlow(WorkflowBuilder<T> workflowBuilder) throws ResourceException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public WorkflowSearchResult searchWorkFlows(WorkflowSearch search, Integer startRow, Integer maxRows,
                                                boolean supportBackwardCompatibility) throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public int resumeWorkFlows(WorkflowSearch search, List<String> workFlowIdList, String methodName)
			throws ResourceException {
		// TODO Auto-generated method stub
		return 0;
	}

}
