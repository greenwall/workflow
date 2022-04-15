package com.nordea.dompap.boot;

import com.nordea.dompap.workflow.FinalState;
import com.nordea.dompap.workflow.WorkFlow;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nordea.dompap.workflow.WorkFlowUtil.getMethod;

@Slf4j
public class BootTestWorkFlow implements Serializable {

	String s;

	public BootTestWorkFlow(String s) {
		this.s = s;
	}

	private static Method method(String name) {
		return getMethod(BootTestWorkFlow.class, name);
	}

	public static Method stepA = method("stepA");
	public Method stepA() {
		log.info("doing stepA");

		s = s + ".stepA";
		return stepB;
	}

	private static Method stepB = method("stepB");
	public Method stepB() {
		log.info("doing stepB");
		s = s+ "stepB";
		return stepC;
	}

	private static Method stepC = method( "stepC");
	@FinalState(archiveAfterDays=3)
	public Method stepC() {
		log.info("doing doC");
		return null;
	}

}
