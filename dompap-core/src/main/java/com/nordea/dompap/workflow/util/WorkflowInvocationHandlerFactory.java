package com.nordea.dompap.workflow.util;

import java.lang.reflect.Proxy;

/**
 * Factory for creating dynamic proxies
 * 
 * factory = new WorkflowInvocationHandlerFactory<NdsSignableWorkflow>(NdsSignableWorkflow.class);
 * NdsSignableWorkflow c = factory.newInstance(myWorkflowContent);
 * c.foo();
 *
 * @param <T> Interface to expose as dynamic proxy
 */
public class WorkflowInvocationHandlerFactory<T> {

    private final Class<?> interfacee;

    // May write as lambda expression to avoid this argument
    public WorkflowInvocationHandlerFactory(Class<?> interfacee) {
        super();
        this.interfacee = interfacee;
    }

    @SuppressWarnings("unchecked")
    public T newInstance(Object target){        
        WorkflowInvocationHandler handler = new WorkflowInvocationHandler(target);
        return (T) Proxy.newProxyInstance(target.getClass().getClassLoader(), new Class[]{ interfacee }, handler);
    }
}
