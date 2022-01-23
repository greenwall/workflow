package com.nordea.dompap.config;

import com.nordea.dompap.workflow.*;
import com.nordea.dompap.workflow.config.WorkFlowConfig;
import com.nordea.dompap.workflow.config.WorkFlowContext;
import com.nordea.dompap.workflow.event.WorkFlowEventService;
import com.nordea.dompap.workflow.event.WorkFlowEventServiceImpl;
import com.nordea.dompap.workflow.selector.WorkFlowSelector;
import com.nordea.dompap.workflow.selector.WorkFlowSelectorImpl;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Value
@ConfigurationPropertiesScan("com.nordea.dompap.config")
@Import({WorkFlowConfigSpring.class, WorkFlowDataSourceConfiguration.class})
public class WorkFlowContextSpring implements WorkFlowContext {

    private WorkFlowConfigSpring workFlowConfig;

    private WorkFlowDataSourceConfiguration dataSourceConfiguration;

    private WorkFlowLabelService workFlowLabelService;
    private MetadataService metadataService;
    private WorkFlowEventService workFlowEventService;
    private WorkFlowSelector workFlowSelector;
    private WorkFlowStatusService workFlowStatusService;
    private WorkFlowService workFlowService;
    private WorkFlowManager workFlowManager;

    public WorkFlowContextSpring(WorkFlowConfigSpring workFlowConfig, WorkFlowDataSourceConfiguration dataSourceConfiguration) {
        this.workFlowConfig = workFlowConfig;
        this.dataSourceConfiguration = dataSourceConfiguration;

        workFlowLabelService = new WorkFlowLabelServiceImpl(getDataSource());
        metadataService = new MetadataServiceImpl(workFlowConfig, getDataSource());
        workFlowEventService = new WorkFlowEventServiceImpl(getDataSource());
        workFlowSelector = new WorkFlowSelectorImpl(workFlowConfig, getDataSource());
        workFlowStatusService = new WorkFlowStatusServiceImpl(getDataSource());
        workFlowService = new WorkFlowServiceImpl(workFlowConfig, getDataSource(), metadataService, workFlowSelector, workFlowStatusService);
        workFlowManager = new WorkFlowManagerImpl(workFlowConfig, workFlowService);
    }

    public DataSource getDataSource() {
        return dataSourceConfiguration.getDataSource();
    }

    @Bean
    public WorkFlowLabelService getWorkFlowLabelService() { return workFlowLabelService; }

    @Bean
    public MetadataService getMetadataService() { return metadataService; }

    @Bean
    public WorkFlowEventService getWorkFlowEventService() { return workFlowEventService; }

    @Bean
    public WorkFlowSelector getWorkFlowSelector() { return workFlowSelector; }

    @Bean
    public WorkFlowStatusService getWorkFlowStatusService() { return workFlowStatusService; }

    @Bean
    public WorkFlowService getWorkFlowService() { return workFlowService; }

    @Bean
    public WorkFlowManager getWorkFlowManager() { return workFlowManager; }
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
