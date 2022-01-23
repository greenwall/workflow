package com.nordea.dompap.util;

import static org.junit.Assert.assertEquals;

import javax.resource.ResourceException;

import org.junit.Test;

import com.nordea.dompap.jdbc.SqlUtils;

/**
 * 
 * @author G90511(Hitesh Karel)
 *
 */
public class SQLUtilTest {

    @Test
    public void testLowerOneHourInterval() throws ResourceException {
        String intervalCompatibleString = SqlUtils.getIntervalCompatibleString(10);
        assertEquals("0 0:10:00", intervalCompatibleString);
    }

    @Test
    public void testGreateThanOneHourInterval() throws ResourceException {
        String intervalCompatibleString = SqlUtils.getIntervalCompatibleString(120);
        assertEquals("0 2:0:00", intervalCompatibleString);
    }

    @Test
    public void testGreateThanDayInterval() throws ResourceException {
        String intervalCompatibleString = SqlUtils.getIntervalCompatibleString(1444);
        assertEquals("1 0:4:00", intervalCompatibleString);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeInterval() throws IllegalArgumentException {
        SqlUtils.getIntervalCompatibleString(-120);
    }

}
