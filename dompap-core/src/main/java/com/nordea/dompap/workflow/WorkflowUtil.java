package com.nordea.dompap.workflow;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.reflect.MethodUtils;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

@Slf4j
public class WorkflowUtil {

    public static Method getMethod(Object workflow, String methodName) {
        return getMethod(workflow.getClass(), methodName);
    }

    public static Method getMethod(Class<?> workflowClass, String methodName) {
        Method method = getMethodOrNull(workflowClass, methodName);
        if (method == null) {
            throw new NullPointerException("No method named:" + methodName + " (without parameters or with WorkFlow parameter) found in class: "
                    + workflowClass.getName());
        } else {
            return method;
        }
    }

    public static Method getMethodChecked(Object workflow, String methodName) throws NoSuchMethodException {
        return getMethodChecked(workflow.getClass(), methodName);
    }

    public static Method getMethodOrNull(Class<?> workflowClass, String methodName) {
        // TODO Prefer the method with WorkFlow parameter
        Method method = MethodUtils.getAccessibleMethod(workflowClass, methodName, new Class[] {});
        if (method == null) {
            method = MethodUtils.getAccessibleMethod(workflowClass, methodName, new Class[] { Workflow.class});
        }
        return method;
    }
    
    /**
     * Same as getMethod, but throws checked exception NoSuchMethodException if
     * method not found.
     */
    public static Method getMethodChecked(Class<?> workflowClass, String methodName) throws NoSuchMethodException {
        Method method = getMethodOrNull(workflowClass, methodName);
        if (method == null) {
            throw new NoSuchMethodException("No method named:" + methodName + " (without parameters or with WorkFlow parameter) found in class: "
                    + workflowClass.getName());
        } else {
            return method;
        }
    }

    /**
     * Returns a WhenMethod indicating the time to schedule the method.
     */
    public static WhenMethod getWhenMethod(Class<?> workflowClass, String methodName, Date timestamp) {
        return WhenMethod.next(getMethod(workflowClass, methodName), timestamp);
    }

    public static WhenMethod getWhenMethod(Object workflow, String methodName, Date timestamp) {
        return WhenMethod.next(getMethod(workflow, methodName), timestamp);
    }

    /**
     * The workflow being executed by the current thread. 
     */
    public static Workflow<?> getCurrentWorkFlow() {
		return new ThreadLocal<Workflow<?>>().get();
    }
    
    /**
     * If the workflow contains a version number in the manifest file then use that.
     * Else return the version number of current class.
     * @return Version number
     */
    public static String getVersionFromWorkflow(Class<?> currentClass, Workflow<?> workflow) {
        String workflowSpecificVersion;
        
        if (workflow.getContent()!=null){
            workflowSpecificVersion = workflow.getContent().getClass().getPackage().getImplementationVersion();
            if (workflowSpecificVersion!=null){
                return workflowSpecificVersion;
            }
        }

        return getVersionFromWorkflow(currentClass, workflow.getWorkflowClassName());
    }

    /**
     * If the workflow contains a version number in the manifest file then use that.
     * Else return the version number of current class.
     */
    public static String getVersionFromWorkflow(Class<?> currentClass, String workflowClassName) {
        String workflowSpecificVersion;

        if (workflowClassName!=null){
            log.debug("Reading version from workflow class without actual content: " + workflowClassName);
            try {
                workflowSpecificVersion = currentClass.getClassLoader().loadClass(workflowClassName).getPackage().getImplementationVersion();
                if (workflowSpecificVersion!=null){
                    return workflowSpecificVersion;
                }
            } catch (ClassNotFoundException e) {
                log.warn("Warning loading workflow class: " + workflowClassName);
            }
        }
        return currentClass.getPackage().getImplementationVersion();
    }


    /**
     * Get the server name
     */
    public static String getServerName() {
        // TODO How to get server name?
/*
        String serverName = EnvironmentController.getServerName();
        if (serverName == null) {
            serverName = EnvironmentController.getCurrentEnvironment().name();
            try {
                serverName = serverName + "@" + InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                log.debug("Error getting servername", e);
            }
        }
        serverName = serverName.toLowerCase();
        return serverName;

 */
        try {
            return "@" + InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.debug("Error getting servername", e);
            return "unknown-server";
        }
    }

    
}
