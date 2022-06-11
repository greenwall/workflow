package com.nordea.dompap.workflow;

import com.nordea.dompap.workflow.event.WorkflowEvent;
import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.ProfitCenter;
import com.nordea.next.dompap.domain.UserId;
import lombok.Getter;
import lombok.Setter;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class Workflow<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Getter private final UUID id;
    @Getter @Setter String externalKey;
    @Getter private final UserId userId;
    @Getter private final BranchId branchId;
    @Getter private final ProfitCenter profitCenter;
    @Getter private final Date creationTime;
    @Getter private final Date lastUpdateTime;
    @Getter private final String workflowClassName;
    @Getter private final String subType;
    @Getter @Setter private String methodName;
    @Getter @Setter private Date startWhen;
    @Getter @Setter private Date methodStarted;
    @Getter @Setter private Date methodEnded;
    @Getter @Setter private String exceptionName;
    @Getter @Setter private String exceptionMessage;
    @Getter @Setter private String stacktrace;
    @Getter @Setter private String serverName;
    @Getter @Setter private String version;
    private int execSeqno;
    @Getter @Setter private transient T content;
    @Getter @Setter private Metadata metadata;
    @Getter @Setter private String requestDomain;
    @Getter @Setter private UUID labelId;
    @Getter @Setter private int eventsQueued; // Incremented by NDS3 callback service - decremented when processing
    @Getter @Setter private Date latestEvent; // Latest event added by NDS 3 callback service
    @Getter @Setter private boolean processEvents; // Old workflows or workflows busy executing steps and/or retrying may prevent event processing by setting to 0 or NULL
    @Getter @Setter private UUID currentEventId;
    private transient WorkflowEvent currentEvent;

    public Workflow() {
        this.id = UUID.randomUUID();
        this.externalKey = null;
        this.userId = null;
        this.branchId = null;
        this.profitCenter = null;
        this.creationTime = null;
        this.lastUpdateTime = null;
        this.workflowClassName = null;
        this.subType = null;
    }

    protected Workflow(UUID id, String externalKey, String workflowClassName, UserId userId, BranchId branchId,
                       Date creationTime, Date lastUpdateTime) {
        this.id = id;
        this.externalKey = externalKey;
        this.userId = userId;
        this.branchId = branchId;
        this.profitCenter = null;
        this.creationTime = creationTime;
        this.lastUpdateTime = lastUpdateTime;
        this.workflowClassName = workflowClassName;
        this.subType = null;
    }

    protected Workflow(UUID id, String externalKey, String workflowClassName, UserId userId, ProfitCenter profitCenter,
                       Date creationTime, Date lastUpdateTime) {
        this.id = id;
        this.externalKey = externalKey;
        this.userId = userId;
        this.branchId = null;
        this.profitCenter = profitCenter;
        this.creationTime = creationTime;
        this.lastUpdateTime = lastUpdateTime;
        this.workflowClassName = workflowClassName;
        this.subType = null;
    }

    protected Workflow(UUID id, String externalKey, String workflowClassName, String subType, UserId userId, BranchId branchId,
                       Date creationTime, Date lastUpdateTime) {
        this.id = id;
        this.externalKey = externalKey;
        this.userId = userId;
        this.branchId = branchId;
        this.profitCenter = null;
        this.creationTime = creationTime;
        this.lastUpdateTime = lastUpdateTime;
        this.workflowClassName = workflowClassName;
        this.subType = subType;
    }

    protected Workflow(UUID id, String externalKey, String workflowClassName, String subType, UserId userId, ProfitCenter profitCenter,
                       Date creationTime, Date lastUpdateTime) {
        this.id = id;
        this.externalKey = externalKey;
        this.userId = userId;
        this.branchId = null;
        this.profitCenter = profitCenter;
        this.creationTime = creationTime;
        this.lastUpdateTime = lastUpdateTime;
        this.workflowClassName = workflowClassName;
        this.subType = subType;
    }

    /**
     * The number of times this workflow was picked for execution.
     * Note that several methods may be executed in one pickup.
     */
    public int getExecSeqno() {
		return execSeqno;
	}

    public void setExecSeqno(int execSeqno) {
        this.execSeqno = execSeqno;
    }

    /**
     * @deprecated Use WorkFlowService.loadWorkFlowMetadata(workflow)
     */
    @Deprecated
	public Metadata loadMetadata() throws ResourceException {
        throw new NotSupportedException("Use WorkFlowService.loadWorkFlowMetadata(workflow)");
/*
        if (metadata == null) {
            metadata = ServiceFactory.getService(WorkFlowService.class).loadWorkFlowMetadata(this);
        }
        return metadata;
 */
    }

	/**
	 * Loads the current event (currentEventId) and keeps as transient, so it will be loaded once. 
	 * Named loadCurrentEvent not getCurrentEvent to prevent logging in ServiceFactory to call
     * @deprecated Use WorkFlowEventService.getEvent(workflow.getCurrentEventId()
	 */
	@Deprecated
	public WorkflowEvent loadCurrentEvent() throws ResourceException {
        throw new NotSupportedException("Use WorkFlowEventService.getEvent(workflow.getCurrentEventId())");
/*
        if (currentEvent == null && currentEventId != null) {
            currentEvent = ServiceFactory.getService(WorkFlowEventService.class).getEvent(currentEventId);
        }
        return currentEvent;
 */
    }

    /**
     * @deprecated Use WorkFlowEventService.getLastEventFor
     */
    @Deprecated
	public WorkflowEvent loadLastProcessedEvent() throws ResourceException {
        throw new NotSupportedException("Use WorkFlowEventService.getLastEventFor");
//		return ServiceFactory.getService(WorkFlowEventService.class).getLastEventFor(id);
    }
}
