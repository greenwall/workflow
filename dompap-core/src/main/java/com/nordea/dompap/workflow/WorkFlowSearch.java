package com.nordea.dompap.workflow;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.nordea.next.dompap.domain.BranchId;
import com.nordea.next.dompap.domain.ProfitCenter;
import com.nordea.next.dompap.domain.UserId;
import com.nordea.next.dompap.domain.util.TimePart;
import lombok.Data;
import org.joda.time.Interval;

import java.io.Serializable;
import java.util.Date;

/**
 * Search query.
 * Search either on branchId or profitCenter, not both.
 */
@Data
public class WorkFlowSearch implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private UserId userId;
	private String requestDomain;
	private BranchId branchId;
	private ProfitCenter profitCenter;
	private Interval creationTime;
	private String[] workFlowClasses;
	private String subType;
	private String workFlowId;
	private String[] methods;
	private String methodException;
	private String externalKey;
	private Multimap<String, String> matchingMetadata;
	private String labelId;
	private Interval lastUpdateTime;
	private String exceptionMessage;
	private TimePart creationTimeRangeLowerLimit;
	private TimePart creationTimeRangeUpperLimit;
	private boolean isLabelChecked;
    private boolean excludeLabels;  // introduced in order to not risk breaking the general workflowService, as we cannot always asume that 'isLabelChecked=false' means exclude labels :( 
	private Date methodStart;
	private boolean stalled;
	

	@SuppressWarnings("rawtypes")
	public void setWorkFlowClass(Class workFlowClass) {
		this.workFlowClasses = new String[] { workFlowClass.getName() };
	}
	public void setMethodException(Class<? extends Throwable> methodException) {
		this.methodException = methodException.getName();
	}
	public void setAnyMethodException(boolean anyException) {
		this.methodException = "";
	}
	public void putMetadataProperty(String name, String value) {
		if (matchingMetadata==null) {
			matchingMetadata = HashMultimap.create();
		}
		matchingMetadata.put(name, value);
	}


	public void setBranchId(BranchId branchId) {
		this.branchId = branchId;
		this.profitCenter = null;
	}

	public void setProfitCenter(ProfitCenter profitCenter) {
		this.profitCenter = profitCenter;
		this.branchId = null;
	}

	public void setWorkFlowClass(String workFlowClass) {
		if (workFlowClass==null) {
			this.workFlowClasses = null;
		} else {
			this.workFlowClasses = new String[] { workFlowClass };
		}
	}

	public void setMethod(String method) {
		if (method==null) {
			this.methods = null;
		} else {
			this.methods = new String[] { method };
		}
	}

	public Multimap<String, String> getMatchingMetadata() {
		return matchingMetadata;
	}
	public void setMatchingMetadata(Multimap<String, String> matchingMetadata) {
		this.matchingMetadata = matchingMetadata;
	}

    /**
     * @return the isLabel
     */
    public boolean getLabelChecked() {
        return isLabelChecked;
    }

    /**
     * @param isLabel
     * the isLabel to set
     */
    public void setLabelChecked(boolean isLabel) {
        this.isLabelChecked = isLabel;
    }
}
