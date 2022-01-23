package com.nordea.dompap.config;

import com.nordea.dompap.workflow.config.WorkFlowConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "workflow")
@Slf4j
public class WorkFlowConfigSpring implements WorkFlowConfig {

    private String schedule;
    private Map<String, String> controllerFor = new HashMap<>();

    // Controller config
    private Map<String, String> controller = new HashMap<>();
    private Map<String, String> archive = new HashMap<>();
    private String jobs;
    private Map<String, String> job = new HashMap<>();
    private String appservers;
    private ExcludedAppservers excluded = new ExcludedAppservers();
    private String triggers;
    private String maxthreads;
    private String maxSeconds;
    private String maxExecutions;

    private SelectMaxRetries select; // = new SelectMaxRetries();
    private MetadataCheck metadata = new MetadataCheck();
    private String sample;
    private Map<String, String> serializer = new HashMap<>();
    private Selector selector;

    @Data
    public static class MetadataCheck {
        private String check;
    }

    @Data
    public static class ExcludedAppservers {
        private String appservers;        
    }

    @Data
    public static class SelectMaxRetries {
        private String maxRetries;
    }

    @Data
    public static class Selector {
        private Boolean skipLocked;
    }

    @Override
    public boolean isSelectorSkipLocked() {
        return selector!=null && selector.skipLocked!=null ? selector.skipLocked : true;
    }

    @Override
    public String getSchedule() {
        return schedule;
    }

    @Override
    public String getControllerFor(String workFlowClass) {
        return controllerFor.get(workFlowClass);
    }

    @Override
    public String getControllerConfig(String controllerName) {
        return controller.get(controllerName);
    }

    @Override
	public Integer getArchiveAfterDays(Method method) {
		Class<?> workflowClass = method.getDeclaringClass();

		String s1 = archive.get( workflowClass.getSimpleName() + "." + method.getName());
		if (s1 == null) {
			s1 = archive.get(workflowClass.getSimpleName());
		}

		if (s1 != null) {
			try {
				return Integer.parseInt(s1);
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
    public String[] getJobs() {
        return StringUtils.split(jobs, ";");        
    }

    @Override
    public String getSchedule(String jobName) {
        return job.get(jobName);
    }    

    @Override
    public String[] getAppServers() {
        log.info("workflow.appservers="+appservers);
        if (appservers!=null) {
            String[] includedServers = StringUtils.split(appservers, ";");
            for (int n=0; n<includedServers.length; n++) {
                includedServers[n] = includedServers[n].toLowerCase();
            }       
            return includedServers;
        } 
        return null;
    }

    @Override
	public String[] getExcludedAppServers() {
		String exclAppServers = excluded.appservers;
		log.info("workflow.excluded.appservers=" + exclAppServers);
		if (exclAppServers != null) {
			String[] excludedServers = StringUtils.split(exclAppServers, ";");
			for (int n = 0; n < excludedServers.length; n++) {
				excludedServers[n] = excludedServers[n].toLowerCase();
			}
			return excludedServers;
		}
		return null;
	}

	@Override
    public int getWorkflowJobTriggers() {
        return intValue(triggers, 1);
    }

    @Override
    public int getWorkflowMaxThreads() {
        return intValue(maxthreads, 40);
    }
    
    
    /**
     * Maximum number of workflow instances to execute per quartz job fire.
     */
    @Override
    public int getMaxWorkflowsPerFire(String workflowClassName) {
        // TODO Fix specific settings
        return intValue(maxExecutions, 30);
    }

    /**
     * Maximum number of seconds to execute workflow instances per quartz job fire.
     */
    @Override
    public int getMaxSecondsPerFire(String workflowClassName) {
        // TODO Fix specific maxSeconds
//        String maxSeconds = configFor("workflow.maxSeconds." + workflowClassName);
//        if (StringUtils.isBlank(maxSeconds)) {
//            maxSeconds = configFor("workflow.maxSeconds");
//        }
        return intValue(maxSeconds, 15);
    }

    /**
     * Maximum number of retries when selecting ready workflow.
     */
    @Override
    public int getMaxRetriesPerSelect() {
        return intValue(select.maxRetries, 3);
    }

    /**
     * Check value of workflow metadata. Defaults to true.
     */
    @Override
	public boolean getCheckMetadata() {
		return booleanValue(metadata.check, true);
	}

    @Override
    public int getWorkflowSample() {
        return intValue(sample, 0);
    }

    @Override
    public String getSerializer(String workflowClassName) {
        String specific = serializer.get(workflowClassName);
        if (specific!=null) {
            return specific;
        }

        // If specific serializer for class not found, try package hierarchy.
        int packages = StringUtils.countMatches(workflowClassName, ".");
        String packageName = workflowClassName;
        for (int n=0; n<packages; n++) {
            packageName = StringUtils.substringBeforeLast(packageName, ".");
            String packageSerializer = serializer.get(packageName);
            if (packageSerializer!=null) {
                return packageSerializer;
            }
        }
        // TODO Does Spring map this to the map?
        return serializer.get("");
    }

    private static int intValue(String stringValue, int defaultValue) {
        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static boolean booleanValue(String stringValue, boolean defaultValue) {
        if (StringUtils.isBlank(stringValue)) {
            return Boolean.parseBoolean(stringValue);
        } else {
            return defaultValue;
        }
    }


}
