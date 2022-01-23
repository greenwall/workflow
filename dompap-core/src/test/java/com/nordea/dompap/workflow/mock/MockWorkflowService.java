package com.nordea.dompap.workflow.mock;

import com.nordea.dompap.workflow.*;
import com.nordea.dompap.workflow.content.WorkFlowContentSerializer;
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

public class MockWorkflowService implements WorkFlowService {

    @Override
    public <T> WorkFlow<T> getWorkFlow(UUID uuid, Class<T> interfaceClass) throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("unchecked")
	@Override
    public WorkFlow<?> getWorkFlow(UUID uuid) throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> WorkFlow<T> findWorkFlowByExternalKey(String externalKey) throws ResourceException {
        return null;
    }

    @Override
    public <T> T loadWorkFlowContent(WorkFlow<T> workflow)
            throws ResourceException, ClassNotFoundException, IOException {
        return workflow.getContent();
    }

    @Override
    public <T> WorkFlowController loadWorkFlowController(WorkFlow<T> workflow) throws ResourceException, ClassNotFoundException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> void saveWorkFlowController(WorkFlow<T> workflow, WorkFlowController controller) throws ResourceException {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> WorkFlow<T> insertWorkFlow(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId,
                                          T workflowInstance, Method method, Date startWhen, Metadata metadata, WorkFlowController controller)
            throws ResourceException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public <T> WorkFlow<T> insertWorkFlow(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId,
                                          String workflowClassName, String subType, WorkFlowContentSerializer contentSerializer, T workflowInstance,
                                          String methodName, Date startWhen, Metadata metadata, WorkFlowController controller)
			throws ResourceException, IOException {
		// TODO Auto-generated method stub
		return null;
	}
    
	@Override
	public <T> WorkFlow<T> insertWorkFlow(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId,
			String workflowClassName, WorkFlowContentSerializer contentSerializer, T workflowInstance, String methodName, Date startWhen, Metadata metadata,
			WorkFlowController controller) throws ResourceException, IOException {
		// TODO Auto-generated method stub
		return null;
	}    
        
    @Override
    public <T> void saveWorkFlowContent(WorkFlow<T> workflow) throws ResourceException {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> Metadata loadWorkFlowMetadata(WorkFlow<T> workflow) throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> void saveWorkFlowMetadata(WorkFlow<T> workflow, Metadata metadata) throws ResourceException {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void updateWorkFlow(WorkFlow<T> workflow, String methodName, Date startWhen, Date methodStarted,
            Date methodEnded, Throwable methodException, Metadata metadata) throws ResourceException {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void updateWorkFlow(WorkFlow<T> workflow, Method method, Date startWhen, Date methodStarted,
            Date methodEnded, Throwable methodException, Metadata metadata) throws ResourceException {
//        ServiceFactory.getService(WorkFlowService.class).updateWorkFlow(workflow, step, null, startTime, null, null, null);
    		workflow.setMethodName(method.getName());
    		workflow.setMethodStarted(methodStarted);
    		workflow.setMethodEnded(methodEnded);
    }

    @Override
    public <T> WorkFlow<T> pickReadyWorkFlow(String workflowClassName, String subType) throws ResourceException {
        return null;
    }

    @Override
    public <T> List<WorkFlow<T>> pickReadyWorkFlows(String workflowClassName, String subType, int maxCount) throws ResourceException {
    	return Collections.emptyList();
    }

    @SuppressWarnings("rawtypes")
	@Override
    public List<WorkFlow> getWorkFlows(UserId userId, BranchId branchId, Interval creationTime, Integer startRow,
            Integer maxRows) throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorkFlowSearchResult searchWorkFlows(WorkFlowSearch search, Integer startRow, Integer maxRows)
            throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorkFlowSearchResult searchWorkFlowsAggregated(WorkFlowSearch search, Integer startRow, Integer maxRows, Integer totalRows)
            throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateWorkFlow(UUID workflowId, String externalKey) throws ResourceException {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> List<WorkFlowStepResult> getWorkFlowHistory(WorkFlow<T> workflow) throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<WorkFlowMethodCount> getWorkflowStatus(int[] periodsInMinutes) throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<WorkFlowMethodCount> getWorkflowStatus(int[] periods, String workflowClass) throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public List<WorkFlowMethodCount> getWorkflowStatus(WorkFlowStatusQuery query) throws ResourceException {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public <T> void archiveWorkFlow(WorkFlow<T> workflow) throws ResourceException {
        // TODO Auto-generated method stub

    }

	@Override
	public int updateWorkFlowLabel(WorkFlowSearch search, WorkFlowLabel label) throws ResourceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> T loadWorkFlowContent(WorkFlow<T> workflow, WorkFlowContentSerializer contentSerializer)
			throws ResourceException, ClassNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void saveWorkFlowContent(WorkFlow<T> workflow, WorkFlowContentSerializer contentSerializer)
			throws ResourceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void queueEvent(WorkFlow<T> workflow, UUID eventId) throws ResourceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void updateWorkFlow(WorkFlow<T> workflow, String methodName, Date startWhen, Date methodStarted,
			Date methodEnded, Throwable methodException, Metadata metadata, boolean processEvents)
			throws ResourceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void updateWorkFlow(WorkFlow<T> workflow, Method method, Date startWhen, Date methodStarted,
			Date methodEnded, Throwable methodException, Metadata metadata, boolean processEvents)
			throws ResourceException {
		// TODO Auto-generated method stub
		
	}

    @Override
    public <T> void stepExecutionCompleted(WorkFlow<T> workflow, Method method, Date startWhen, Date methodStarted, Date methodEnded, Throwable methodException, Metadata metadata, boolean processEvents) throws ResourceException {
        // Ignore
    }

    @Override
	public <T> WorkFlow<T> insertWorkFlow(UUID id, String externalKey, UserId userId, String requestDomain, BranchId branchId,
			T workflowInstance, String subType, Method method, Date startWhen, Metadata metadata,
			WorkFlowController controller) throws ResourceException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> WorkFlow<T> insertWorkFlow(WorkFlowBuilder<T> workflowBuilder) throws ResourceException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public WorkFlowSearchResult searchWorkFlows(WorkFlowSearch search, Integer startRow, Integer maxRows,
            boolean supportBackwardCompatibility) throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public int resumeWorkFlows(WorkFlowSearch search, List<String> workFlowIdList, String methodName)
			throws ResourceException {
		// TODO Auto-generated method stub
		return 0;
	}

}
