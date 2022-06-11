package com.nordea.dompap.workflow;

import com.nordea.dompap.workflow.config.WorkflowConfig;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import javax.resource.ResourceException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@EqualsAndHashCode
public class DefaultWorkflowController implements WorkflowController {
    private static final long serialVersionUID = 1L;

    private final transient WorkflowConfig config;
    private final transient WorkflowManager workFlowManager;

    String currentRetryMethod;
    int retries;

    public DefaultWorkflowController(WorkflowConfig config, WorkflowManager workFlowManager) {
        this.config = config;
        this.workFlowManager = workFlowManager;
    }

    private int[] getRetryMinutes(Workflow<?> workflow) {
        // load retry for method level
        String s = getRetryValue(workflow);
        String[] mins = StringUtils.split(s, ",");

        int[] minutes = new int[mins.length];
        for (int n = 0; n < mins.length; n++) {
            minutes[n] = Integer.parseInt(mins[n].trim());
        }
        return minutes;
    }

    private String configFor(String key) {
        return config.getControllerConfig(key);
    }

    private String configFor(String key, String defaultValue) {
        String value = config.getControllerConfig(key);
        return value!=null ? value : defaultValue;
    }

    protected String getRetryValue(Workflow<?> workflow) {
        String propertyValue;
        // try to load property value for workflowName,subType and method
        String propertName = getRetryPropertyName(getClassSimpleName(workflow), workflow.getSubType(),
                workflow.getMethodName());
        propertyValue = configFor(propertName);
        // try to load property value for workflowName and method
        if (propertyValue == null) {
            propertName = getRetryPropertyName(getClassSimpleName(workflow), null, workflow.getMethodName());
            propertyValue = configFor(propertName);
        }
        // try to load property for workflowName and subType
        if (propertyValue == null) {
            propertName = getRetryPropertyName(getClassSimpleName(workflow), workflow.getSubType(), null);
            propertyValue = configFor(propertName);
        }
        // try to load property for workflowName
        if (propertyValue == null) {
            propertName = getRetryPropertyName(getClassSimpleName(workflow), null, null);
            propertyValue = configFor(propertName);
        }
        // if nothing found try to load at defaultWorkflowController level and
        // if it is also nor found use default value
        if (propertyValue == null) {
            propertName = getRetryPropertyName(null, null, null);
            propertyValue = configFor(propertName, "1, 5, 30, 180, 720");
        }
        log.debug(propertName + "=" + propertyValue);
        return propertyValue;
    }
    
    private static String getRetryPropertyName(String workflowClassName, String subType, String method) {
        return Stream
                .of(DefaultWorkflowController.class.getSimpleName(), getWorkflowSuffixWithSubType(workflowClassName, subType), method, "retryMinutes")
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("."));
    }

    private static String getWorkflowSuffixWithSubType(String workflowClassName, String subType) {
        return workflowClassName!=null?workflowClassName.concat((subType != null ? ":".concat(subType) : "")):null;
    }

    /**
     * Returns configured resumeAt method Configuration follows this format:
     * 
     * DefaultWorkFlowController.onFail.CLASS.METHOD.[EXCEPTION.]resumeAt=[
     * MESSAGE_CONTAINS:]RESUME_METHOD[;[MESSAGE_CONTAINS:]RESUME_METHOD]
     * 
     * examples: DefaultWorkFlowController.onFail.DocumentSigningProcessV2Impl.
     * processNdsEvent(IllegalArgumentException).resumeAt=error.timeout:
     * sendToNds
     * 
     * DefaultWorkFlowController.onFail.TestWorkFlow.doA.resumeAt=doB - Any
     * exception from TestWorkFlow.doA resumes at doB
     * 
     * DefaultWorkFlowController.onFail.TestWorkFlow.doA(NullPointerException).
     * resumeAt=doB - NullPointerExceptions from TestWorkFlow.doA resumes at doB
     * 
     * DefaultWorkFlowController.onFail.TestWorkFlow.doA(NullPointerException).
     * resumeAt=n\=3:doB - NullPointerExceptions with 'n=3' in the exception
     * getMessage() from TestWorkFlow.doA resumes at doB
     * 
     * DefaultWorkFlowController.onFail.TestWorkFlow.doA(NullPointerException).
     * resumeAt=n\=3:doB;n\=7:doC - NullPointerExceptions in TestWorkFlow.doA 
     * resumes in doB if 'n=3' is found in the exception getMessage() 
     * or it resumes in doC if 'n=7' is found in the exception getMessage()
     * 
     * DefaultWorkFlowController.onFail.TestWorkFlow.doA(NullPointerException).
     * resumeAt=n\=3:doB;doC - NullPointerExceptions in TestWorkFlow.doA 
     * resumes in doB if 'n=3' is found in the exception getMessage() 
     * or it resumes in doC if 'n=3' is NOT found in the exception getMessage()
     * 
     * DefaultWorkFlowController.onFail.TestWorkFlow.doA(NullPointerException).
     * resumeAt=n\=3: - NullPointerExceptions with 'n=3' in the exception
     * getMessage() from TestWorkFlow.doA are left as failed, i.e. retry
     * disabled.
     * 
     * DefaultWorkFlowController.onFail.TestWorkFlow.doA.resumeAt=n\=3: - Any
     * exception with 'n=3' in the exception getMessage() from TestWorkFlow.doA
     * are left as failed, i.e. retry disabled.
     * 
     * DefaultWorkFlowController.onFail.TestWorkFlow.doA.resumeAt= - Any
     * exception from TestWorkFlow.doA are left as failed, i.e. retry disabled.
     */
    Method getOnFail(Workflow<?> workflow, Method failingMethod, Throwable exception, Method defaultResumeMethod) {

        String workflowClass = getClassSimpleName(workflow);
        String methodName = failingMethod.getName();
        String exceptionClass = exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();

        final String onFailMethod = DefaultWorkflowController.class.getSimpleName() + ".onFail." + workflowClass + "."
                + methodName;
        String resumeAt = configFor(onFailMethod + "(" + exceptionClass + ").resumeAt");
        log.info(onFailMethod + "(" + exceptionClass + ").resumeAt=" + resumeAt);

        // if not found - try without workflowclass 
        final String onFailMethodForAnyWorkflow = DefaultWorkflowController.class.getSimpleName() + ".onFail." + methodName;
        if (resumeAt == null) {
            resumeAt = configFor(onFailMethodForAnyWorkflow + "(" + exceptionClass + ").resumeAt");
            log.info(onFailMethodForAnyWorkflow + "(" + exceptionClass + ").resumeAt=" + resumeAt);
        }
        
        // if no onFail - try for given workflow class for any exception class.
        if (resumeAt == null) {
            resumeAt = configFor(onFailMethod + ".resumeAt");
            log.info(onFailMethod + ".resumeAt=" + resumeAt);
        }
        
        // if no onFail - try for any workflow class for any exception class.
        if (resumeAt == null) {
            resumeAt = configFor(onFailMethodForAnyWorkflow + ".resumeAt");
            log.info(onFailMethodForAnyWorkflow + ".resumeAt=" + resumeAt);
        }
        if (resumeAt != null) {
            if (!StringUtils.contains(resumeAt, ";") && !StringUtils.contains(resumeAt, ":")) {
                // resumeAt contains a specific method
                return getMethodOrNull(workflow, resumeAt);
            }
            // Split into resume elements
            String[] resumeAtArray = StringUtils.split(resumeAt, ";");
            // Check first resume elements that contains a string to be matched with  exception message 
            for (String resumeAtElement : resumeAtArray) {
                if (StringUtils.contains(resumeAtElement, ":")) {
                    String[] s = StringUtils.split(resumeAtElement, ":");
                    String matchContent = s[0];
                    resumeAtElement = s.length > 1 ? s[1] : null;
                    if (StringUtils.contains(exceptionMessage, matchContent) || StringUtils.contains(exceptionClass,matchContent)) {
                        return getMethodOrNull(workflow, resumeAtElement);
                    }
                }
            }
            // If no exception match was found then check resume elements for a general resume method 
            for (String resumeAtElement : resumeAtArray) {
                if (!StringUtils.contains(resumeAtElement, ":")) {
                    return getMethodOrNull(workflow, resumeAtElement);
                }
            }
        }
        // If no match or general resume method was found then return default resume method
        return defaultResumeMethod;
    }

    private static Method getMethodOrNull(Workflow<?> workflow, String methodName) {
        if (methodName == null) {
            return null;
        }
        try {
            return WorkflowUtil.getMethodChecked(workflow.getContent(), methodName);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static String getClassSimpleName(Workflow<?> workflow) {
        return StringUtils.substringAfterLast(workflow.getWorkflowClassName(), ".");
    }

    @Override
    public <T> WorkflowController onStart(Workflow<T> workflow, Method methodToBeExecuted) {
        return null;
    }

    @Override
    public <T> WorkflowController onComplete(Workflow<T> workflow, Method methodExecuted, Method nextMethod,
                                             Date startWhen) {
        return null;
    }

    @Override
    public <T> WorkflowController onFail(Workflow<T> workflow, Method methodExecuted, Throwable exception)
            throws ResourceException {
        log.debug("onFail for " + workflow.getId() + ", failing method:" + methodExecuted.getName() + " with "
                + exception.getMessage());
        String classIdMethod = getClassSimpleName(workflow) + "(" + workflow.getId() + "):" + methodExecuted.getName();

        if (!methodExecuted.getName().equals(currentRetryMethod)) {
            currentRetryMethod = methodExecuted.getName();
            retries = 0;
        }

        int[] retryMinutes = getRetryMinutes(workflow);

        if (retries >= retryMinutes.length) {
            // Fail - give up retrying
            if (workflow.getContent() != null) {
                Method retryMethod = WorkflowUtil.getMethod(workflow.getContent(), currentRetryMethod);
                log.info("onFail for " + classIdMethod + ", giving up after " + retries + " retried executions of "
                        + retryMethod.getName());
//                getAlertManager().fail(workflow, retryMethod,
//                        "Retried method " + currentRetryMethod + " " + retryMinutes.length + " times - giving up.");
                // call the permanent failure method if defined for specific
                // workflow
                workflowCallbackOnPermanentFailure(workflow);

            }

        } else {
            // Retry after configured minutes.
            int waitMinutes = retryMinutes[retries];
            DateTime startWhen = new DateTime().plusMinutes(waitMinutes);
            retries++;

            // Get method to retry - default is current failed method
            Method retryMethod = getOnFail(workflow, methodExecuted, exception, methodExecuted);
            if (retryMethod != null) {
                log.info("onFail for " + classIdMethod + ", retrying (" + retries + ") execution of "
                        + retryMethod.getName() + " at " + startWhen);
//                getAlertManager().retry(workflow, retryMethod, "Retrying method " + currentRetryMethod + " (attempt #"
//                        + retries + ") in " + waitMinutes + " minutes at " + startWhen);
                workFlowManager.retryAt(workflow, retryMethod, startWhen.toDate(), exception);
            } else {
                // Ignore error
                log.info("onFail for " + classIdMethod + ", retry disabled");
//                getAlertManager().retry(workflow, null, "Failure in " + classIdMethod + ", retry disabled");
            }
        }

        return this;
    }

    /**
     * Controller will lookup Permanent failure method name on following criteria
 	 * retry_method+Failed, eg. retry_method=archiveDocuments
     * so permanent failure method name would be archiveDocumentsFailed
     */
    private <T> void workflowCallbackOnPermanentFailure(Workflow<T> workflow) throws ResourceException {
        Method permanentFailureMethod = null;
        if (workflow.getContent() != null) {
            permanentFailureMethod = WorkflowUtil.getMethodOrNull(workflow.getContent().getClass(), currentRetryMethod + "Failed");
        }
        // if no method found it means for current workflow there is no permanent failure action defined
        if (permanentFailureMethod != null) {
            workFlowManager.resumeAt(workflow, permanentFailureMethod, null);
        }
    }

}
