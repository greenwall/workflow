package com.nordea.dompap.util;

import java.util.Calendar;
import java.util.Date;

import com.nordea.next.dompap.domain.util.TimestampUtil;
import org.junit.Assert;
import org.junit.Test;

public class TimestampUtilTest {
	
	@Test
	public void test() {
		int days = 1;
		TimestampUtil timestampUtil = new TimestampUtil();
		Date timestamp = timestampUtil.getTimestamp();
		Calendar cTimestamp = Calendar.getInstance();
		cTimestamp.setTime(timestamp);
		
		Date futureTimestamp = timestampUtil.addDays(timestamp, 1);
		Calendar cFutureTimestamp = Calendar.getInstance();
		cFutureTimestamp.setTime(futureTimestamp);

		long milliseconds = cFutureTimestamp.getTime().getTime() - cTimestamp.getTime().getTime();
		long daysInMilliseconds = days * 24 * 60 * 60 * 1000;
		Assert.assertEquals(daysInMilliseconds, milliseconds);
	}
}
