package com.nordea.dompap.workflow;

import java.lang.reflect.Method;
import java.util.Date;

import org.joda.time.DateTime;

/**
 * Holds information about when to execute the next method on a workflow
 * @author G93283
 */
public class WhenMethod {
    public final Method method;

    /**
     * Earliest time to execute. Null means immediately.
     */
    public final Date when;
    
    /**
     * Delayed executions may flag that events will interrupt/abort the method execution.
     */
    public final boolean eventsInterrupt;

    public WhenMethod(Method method, Date when, boolean eventsInterrupt) {
        this.method = method;
        this.when = when;
        this.eventsInterrupt = eventsInterrupt;
    }

    public WhenMethod(Method method) {
        this(method, null, false);
    }

    public static WhenMethod next(Method method, Date when, boolean eventsInterrupt) {
        return new WhenMethod(method, when, eventsInterrupt);
    }

    public static WhenMethod next(Method method, Date when) {
        return new WhenMethod(method, when, false);
    }

    public static WhenMethod next(Method method) {
        return new WhenMethod(method);
    }

    public static WhenMethod next(Class<?> workflowClass, String methodName, Date when) {
        return next(WorkFlowUtil.getMethod(workflowClass, methodName), when);
    }

    public static WhenMethod next(Class<?> workflowClass, String methodName) {
        return next(WorkFlowUtil.getMethod(workflowClass, methodName));
    }

    public WhenMethod when(DateTime executionTime) {
    	return new WhenMethod(method, executionTime.toDate(), eventsInterrupt);
    }

    public WhenMethod when(Date executionTime) {
    	return new WhenMethod(method, executionTime, eventsInterrupt);
    }
    
    public WhenMethod orEvent() {
    	return new WhenMethod(method, when, true);
    }
}
