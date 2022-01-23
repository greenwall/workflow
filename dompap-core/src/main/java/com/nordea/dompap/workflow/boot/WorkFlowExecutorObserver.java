package com.nordea.dompap.workflow.boot;

import com.nordea.dompap.workflow.WorkFlow;

public interface WorkFlowExecutorObserver {

    void preExecute(WorkFlow<Object> workFlow);
    void postExecute(WorkFlow<Object> workFlow);

}
