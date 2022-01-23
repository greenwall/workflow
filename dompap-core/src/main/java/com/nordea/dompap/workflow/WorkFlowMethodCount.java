package com.nordea.dompap.workflow;

import lombok.Getter;

import java.io.Serializable;
import java.util.Arrays;

public class WorkFlowMethodCount implements Serializable {
	private static final long serialVersionUID = 1L;

	@Getter private final String workflowClassName;
    @Getter private final String subType;
    @Getter private final String methodName;

    @Getter protected int failed;
    @Getter protected int retry;
    @Getter protected int stalled;
    @Getter protected int running;
    @Getter protected int ready;
    @Getter protected int later;
    @Getter protected int finalized;
    @Getter protected int archivable;
    @Getter protected int[] periodsInMinutes;
    @Getter public int countGreaterThanFirstPeriod;
    @Getter protected int[] countLessThanPeriod;

    public WorkFlowMethodCount(WorkFlowMethodCount copy) {
        workflowClassName = copy.workflowClassName;
		subType = copy.getSubType();
        methodName = copy.methodName;
        failed = copy.failed;
        stalled = copy.stalled;
        running = copy.running;
        ready = copy.ready;
        finalized = copy.finalized;
        archivable = copy.archivable;
        periodsInMinutes = copy.periodsInMinutes;
        countGreaterThanFirstPeriod = copy.countGreaterThanFirstPeriod;
        countLessThanPeriod = copy.countLessThanPeriod;
    }

    public WorkFlowMethodCount(String workflowClassName, String methodName) {
    	this(workflowClassName, "", methodName);
    }
    
    public WorkFlowMethodCount(String workflowClassName, String subType, String methodName) {
        this.workflowClassName = workflowClassName;
		this.subType = subType;
        this.methodName = methodName;
    }

    public WorkFlowMethodCount(String workflowClassName, String methodName, int[] periodsInMinutes, int countGreaterThanFirstPeriod, int[] countLessThanPeriod) {
    	this(workflowClassName, "", methodName, periodsInMinutes, countGreaterThanFirstPeriod, countLessThanPeriod);
    }
    	
    public WorkFlowMethodCount(String workflowClassName, String subType, String methodName, int[] periodsInMinutes, int countGreaterThanFirstPeriod, int[] countLessThanPeriod) {
        this.workflowClassName = workflowClassName;
		this.subType = subType;
        this.methodName = methodName;
        this.periodsInMinutes = periodsInMinutes;
        this.countGreaterThanFirstPeriod = countGreaterThanFirstPeriod;
        this.countLessThanPeriod = countLessThanPeriod;
    }

    public boolean isZero() {
        for (int count : countLessThanPeriod) {
            if (count != 0) {
                return false;
            }
        }
        return failed == 0 && stalled == 0 && running == 0 && ready == 0 && countGreaterThanFirstPeriod == 0;
    }

    public int[] getCountForPeriods() {
        return countLessThanPeriod;
    }

    public WorkFlowMethodCount subtract(WorkFlowMethodCount b) {
        if (!Arrays.equals(getPeriodsInMinutes(), b.getPeriodsInMinutes())) {
            throw new IllegalArgumentException("Count periods does not match.");
        }
        WorkFlowMethodCount result = new WorkFlowMethodCount(b.getWorkflowClassName(), b.getSubType(), b.getMethodName());

        result.periodsInMinutes = b.periodsInMinutes;

        result.failed = failed - b.failed;
        result.retry = retry - b.retry;
        result.stalled = stalled - b.stalled;
        result.running = running - b.running;
        result.ready = ready - b.ready;
        result.later = later - b.later;

        result.countGreaterThanFirstPeriod = countGreaterThanFirstPeriod - b.countGreaterThanFirstPeriod;
        result.countLessThanPeriod = new int[b.countLessThanPeriod.length];
        for (int n = 0; n < b.countLessThanPeriod.length; n++) {
            result.countLessThanPeriod[n] = countLessThanPeriod[n] - b.countLessThanPeriod[n];
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
        result = prime * result + ((workflowClassName == null) ? 0 : workflowClassName.hashCode());
        result = prime * result + ((subType == null) ? 0 : subType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WorkFlowMethodCount other = (WorkFlowMethodCount) obj;
        if (methodName == null) {
            if (other.methodName != null)
                return false;
        } else if (!methodName.equals(other.methodName))
            return false;
        if (workflowClassName == null) {
            if (other.workflowClassName != null)
                return false;
        } else if (!workflowClassName.equals(other.workflowClassName))
            return false;
        if (subType == null) {
            if (other.subType != null)
                return false;
        } else if (!subType.equals(other.subType))
            return false;
        return true;
    }

    public int sumOfAllPeriods() {
        int sum = 0;
        for (int count : countLessThanPeriod) {
            sum += count;
        }
        // countGreaterThanFirstPeriod
        sum += countGreaterThanFirstPeriod;
        return sum;
    }

}
