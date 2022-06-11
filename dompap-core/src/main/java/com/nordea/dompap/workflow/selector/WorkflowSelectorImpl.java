package com.nordea.dompap.workflow.selector;

import com.nordea.dompap.workflow.config.WorkflowConfig;

import javax.sql.DataSource;

/**
 * Default implementation for WorkFlowSelector when no ServiceFactory definition is given
 */
public class WorkflowSelectorImpl extends WorkflowSelectorSelectForUpdate {
    public WorkflowSelectorImpl(WorkflowConfig config, DataSource dataSource) {
        super(config, dataSource);
    }
}
