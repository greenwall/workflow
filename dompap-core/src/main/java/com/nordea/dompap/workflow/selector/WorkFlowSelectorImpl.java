package com.nordea.dompap.workflow.selector;

import com.nordea.dompap.workflow.config.WorkFlowConfig;

import javax.sql.DataSource;

/**
 * Default implementation for WorkFlowSelector when no ServiceFactory definition is given
 */
public class WorkFlowSelectorImpl extends WorkFlowSelectorSelectForUpdate {
    public WorkFlowSelectorImpl(WorkFlowConfig config, DataSource dataSource) {
        super(config, dataSource);
    }
}
