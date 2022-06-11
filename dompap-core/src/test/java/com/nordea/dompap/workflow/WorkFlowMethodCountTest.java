package com.nordea.dompap.workflow;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorkFlowMethodCountTest {

	@Test
	public void test()  {
		//int[] periodsInMinutes, int countGreaterThanFirstPeriod, int[] countLessThanPeriod) {
		int[] periods = {3,2,1};
		int[] counts = {10,10,10};		
		WorkflowMethodCount a = new WorkflowMethodCount("class", "method", periods, 10, counts);

		int[] counts2 = {2,2,2};
		WorkflowMethodCount b = new WorkflowMethodCount("class", "method", periods, 1, counts2);
		
		WorkflowMethodCount res = a.subtract(b);
		
		assertEquals(3, res.getPeriodsInMinutes().length);
		assertEquals(3, res.getCountForPeriods().length);
		assertEquals(9, res.getCountGreaterThanFirstPeriod());
		assertEquals(8, res.getCountForPeriods()[0]);
	}

}
