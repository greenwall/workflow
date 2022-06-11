package com.nordea.dompap.boot;

import com.nordea.dompap.workflow.FinalState;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Method;

import static com.nordea.dompap.workflow.WorkflowUtil.getMethod;

@Slf4j
public class BootTestWorkFlow implements Serializable {

	String s;
	transient String x;

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

	public static Method stepA1 = method("stepA1");
	public Method stepA1() {
		log.info("doing stepA1");
		if ("injected".equals(x)) {
			return stepC;
		} else {
			return null;
		}
	}

}
