package com.nordea.dompap.config;

import com.nordea.dompap.workflow.*;
import com.nordea.dompap.workflow.event.WorkflowEventService;
import com.nordea.dompap.workflow.event.WorkflowEventServiceImpl;
import com.nordea.dompap.workflow.selector.WorkflowSelector;
import com.nordea.dompap.workflow.selector.WorkflowSelectorImpl;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Value
@ConfigurationPropertiesScan("com.nordea.dompap.config")
@Import({WorkFlowConfigSpring.class, WorkFlowDataSourceConfiguration.class})
public class WorkFlowContextSpring {

    private WorkFlowConfigSpring workFlowConfig;

    private WorkFlowDataSourceConfiguration dataSourceConfiguration;

    private WorkflowLabelService workFlowLabelService;
    private MetadataService metadataService;
    private WorkflowEventService workFlowEventService;
    private WorkflowSelector workFlowSelector;
    private WorkflowStatusService workFlowStatusService;
    private WorkflowService workFlowService;
    private WorkflowManager workFlowManager;

    public WorkFlowContextSpring(WorkFlowConfigSpring workFlowConfig, WorkFlowDataSourceConfiguration dataSourceConfiguration) {
        this.workFlowConfig = workFlowConfig;
        this.dataSourceConfiguration = dataSourceConfiguration;

        workFlowLabelService = new WorkflowLabelServiceImpl(getDataSource());
        metadataService = new MetadataServiceImpl(workFlowConfig, getDataSource());
        workFlowEventService = new WorkflowEventServiceImpl(getDataSource());
        workFlowSelector = new WorkflowSelectorImpl(workFlowConfig, getDataSource());
        workFlowStatusService = new WorkflowStatusServiceImpl(getDataSource());
        workFlowService = new WorkflowServiceImpl(workFlowConfig, getDataSource(), metadataService, workFlowSelector, workFlowStatusService);
        workFlowManager = new WorkflowManagerImpl(workFlowConfig, workFlowService);
    }

    public DataSource getDataSource() {
        return dataSourceConfiguration.getDataSource();
    }

    @Bean
    public WorkflowLabelService getWorkFlowLabelService() { return workFlowLabelService; }

    @Bean
    public MetadataService getMetadataService() { return metadataService; }

    @Bean
    public WorkflowEventService getWorkFlowEventService() { return workFlowEventService; }

    @Bean
    public WorkflowSelector getWorkFlowSelector() { return workFlowSelector; }

    @Bean
    public WorkflowStatusService getWorkFlowStatusService() { return workFlowStatusService; }

    @Bean
    public WorkflowService getWorkFlowService() { return workFlowService; }

    @Bean
    public WorkflowManager getWorkFlowManager() { return workFlowManager; }
    /*
    @Bean
    @ConfigurationProperties("workflow.datasource")
    public DataSource getDataSource() {
        return DataSourceBuilder.create().type(BasicDataSource.class).build();
//        return DataSourceBuilder.create().build();
    }
*/
/*
    private WorkFlowLabelService workFlowLabelService = new WorkFlowLabelServiceImpl(getDataSource());
    private MetadataService metadataService = new MetadataServiceImpl(workFlowConfig, getDataSource());
    private WorkFlowSelector workFlowSelector = new WorkFlowSelectorImpl(workFlowConfig, getDataSource());
    private WorkFlowStatusService workFlowStatusService = new WorkFlowStatusServiceImpl(getDataSource());
    private WorkFlowService workFlowService = new WorkFlowServiceImpl(workFlowConfig, getDataSource(), metadataService, workFlowSelector, workFlowStatusService);
    private WorkFlowManager workFlowManager = new WorkFlowManagerImpl(workFlowConfig, workFlowService);

    private WorkFlowLabelService workFlowLabelService = null;
    private MetadataService metadataService = null;
    private WorkFlowSelector workFlowSelector = null;
    private WorkFlowStatusService workFlowStatusService = null;
    private WorkFlowService workFlowService = null;
    private WorkFlowManager workFlowManager = null;
*/
}
