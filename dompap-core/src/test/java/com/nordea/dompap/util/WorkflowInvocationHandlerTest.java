package com.nordea.dompap.util;

import com.nordea.dompap.workflow.util.WorkflowInvocationHandlerFactory;
import org.junit.Assert;
import org.junit.Test;


public class WorkflowInvocationHandlerTest {

    private class WorkflowA {
        public String doIt(){
            return "A";
        }
    }

    private class WorkflowB {
        public String doIt(){
            return "B";
        }
    }
    
    private interface DoIt {
        String doIt();
    }

    @Test
    public void doInvocation(){
        WorkflowA a = new WorkflowA();
        WorkflowB b = new WorkflowB();
        
        WorkflowInvocationHandlerFactory<DoIt> proxyFactory = new WorkflowInvocationHandlerFactory<>(DoIt.class);
        DoIt proxy = proxyFactory.newInstance(a);               
        
        Assert.assertEquals("Wrong return value", "A", proxy.doIt());
        
        proxy = proxyFactory.newInstance(b);
        Assert.assertEquals("Wrong return value", "B", proxy.doIt());              
    }
}
