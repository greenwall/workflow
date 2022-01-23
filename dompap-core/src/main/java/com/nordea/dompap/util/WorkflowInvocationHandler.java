package com.nordea.dompap.util;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Invocation handler for using dynamic proxy to wrap workflows
 */
@RequiredArgsConstructor
public class WorkflowInvocationHandler implements InvocationHandler {

    final Object target;

    public Object invoke(final Object proxy, final Method method, final Object[] arguments) throws Throwable {
        final Class<?> targetClass = target.getClass();
        try {
            final Method targetMethod = targetClass.getMethod(method.getName(), method.getParameterTypes());
            if (method.getReturnType().isAssignableFrom(targetMethod.getReturnType())) {
                return targetMethod.invoke(target, arguments);
            }
            throw new UnsupportedOperationException("Target type " + targetClass.getName()
                    + " method has incompatible return type.");
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException("Target type " + targetClass.getName()
                    + " does not have a method matching " + method + ".", e);
        }
    }

}
