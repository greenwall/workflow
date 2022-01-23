package com.nordea.dompap.workflow;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

import javax.resource.ResourceException;

import junit.framework.Assert;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.Test;

import com.nordea.dompap.dpap.Dompap;
import com.nordea.next.core.config.NextConfiguration;
import com.nordea.next.core.config.NoActiveModuleException;
import com.nordea.next.core.module.ModuleHolder;
import com.nordea.next.domain.BranchId;
import com.nordea.next.domain.UserId;

import dk.asseco.servicefactory.ServiceException;
import dk.asseco.servicefactory.ServiceFactory;

public class TestWorkFlowTest {

	@Before
	public void init() throws ServiceException, NoActiveModuleException {			
        ModuleHolder.get().pushCurrentModule(Dompap.MODULE);  
        NextConfiguration.reinitialize(Dompap.MODULE_ID);

//        ServiceFactory.setService(WorkFlowService.class, new WorkFlowServiceImpl());			
//        ServiceFactory.setService(WorkFlowManager.class, new WorkFlowManagerImpl());			
	}

	
	@Test
	public void testStartTestFlow() throws ResourceException, ServiceException, IOException, ClassNotFoundException {
		UserId userId = new UserId("g93283");
		BranchId branchId = BranchId.of("1234");
		byte[] blob = new byte[100000];
		for (int n=0; n<blob.length; n++) {
			blob[n] = (byte) (n%200);
		}
		
		TestWorkFlow workflow = new TestWorkFlow(3, blob);
		Method method = WorkFlowUtil.getMethod(workflow, "doA");
		
		UUID id = UUID.randomUUID();
				
		WorkFlow<TestWorkFlow> task = ServiceFactory.getService(WorkFlowManager.class).start(id, null, userId, branchId, workflow, method, null);
		
	}

}
