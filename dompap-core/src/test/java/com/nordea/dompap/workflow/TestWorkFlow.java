package com.nordea.dompap.workflow;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TestWorkFlow implements Serializable {

	String s;
	int n;
	transient String t;
	List<String> list;
	Map<Integer, String> map;
	byte[] bytes;
	String myId;
	
	public TestWorkFlow(int i, byte[] blob) {
		log.info("<init> i="+i+", blob.length="+blob.length);
		n = i;
		s = "The number "+i+".";
		t = "The square is "+i*i;
		list = new ArrayList<>();
		map = new HashMap<>();
		for (int j=0; j<i; j++) {
			list.add("#"+j);
			map.put(j, "$"+j+"$");
		}
		bytes = blob;
	}
	
	public Method doA(WorkFlow wf) {
		log.info("doing doA");
		
		myId = wf.getId().toString();
		if (n%3 ==0) {
			log.error("failing because n="+n);
			throw new NullPointerException("Test exception from doA- n="+n);
		}
		s = s + "doA("+n+")";
		return WorkFlowUtil.getMethod(this, "doB");
	}

	public Method doB() {
		log.info("doing doB");
		if (n%3 ==1) {
			log.error("failing because n="+n);
			throw new NullPointerException("Test exception from doB - n="+n);
		}
		// Check that size of workflow content is reduced. 
		bytes = null;
		s = "step B did: ["+s+"]";

		if (n%2 == 0) {
			log.info("continuing to doC, because n="+n);
			return WorkFlowUtil.getMethod(this, "doC");
		} else {
			log.info("ending, because n="+n);
			return null;
		}
	}

	@FinalState(archiveAfterDays=3)
	public Method doC() {
		log.info("doing doC");
		return null;
	}
	
	public Method doException() throws Exception {
		// Throw nested exception
		try {
			doFail();
			return null;			
		} catch (Exception e) {
			throw new IllegalArgumentException("Wrapping exception", e);
		}		
	}
	
	private void doFail() throws Exception {
		// Fail
		throw new NullPointerException("Actual error");
	}
	
	public Method doX() {
		log.info("doing doX");
		return null;
	}
	
}
