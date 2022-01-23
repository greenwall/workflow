package com.nordea.dompap.workflow.config;

import com.nordea.dompap.workflow.*;
import com.nordea.dompap.workflow.selector.WorkFlowSelector;
import lombok.Data;

import javax.sql.DataSource;
import java.util.Map;

public interface WorkFlowContext {
    DataSource getDataSource();
    WorkFlowService getWorkFlowService();
    WorkFlowManager getWorkFlowManager();
    WorkFlowConfig getWorkFlowConfig();
    MetadataService getMetadataService();
    WorkFlowSelector getWorkFlowSelector();
    WorkFlowStatusService getWorkFlowStatusService();
    WorkFlowLabelService getWorkFlowLabelService();
}
