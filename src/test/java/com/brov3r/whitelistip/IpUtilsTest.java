package com.brov3r.whitelistip;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The {@code IpUtilsTest} class contains unit tests for the {@link IpUtils} class.
 * This class ensures the accuracy and reliability of the methods used for IP validation, range checking,
 * and country code retrieval.
 */
public class IpUtilsTest {
    /**
     * Tests the {@link IpUtils#getIpCountryCode(String, String)} method.
     * It verifies that the method correctly returns the country code associated with a given IP address.
     */
    @Test
    public void testIpCountry() {
        String api = System.getenv("api");

        assertEquals("ZZ", IpUtils.getIpCountryCode("127.0.0.1", api));
        assertEquals("GB", IpUtils.getIpCountryCode("23.106.56.14", api));
    }

    /**
     * Tests the {@link IpUtils#ipInRange(String, String)} method.
     * It checks various IP ranges and ensures that the method correctly identifies if an IP falls within a given range.
     */
    @Test
    public void testRangeIP() {
        assertTrue(IpUtils.ipInRange("192.168.0.3", "192.168.0.1 - 192.168.0.5"));
        assertTrue(IpUtils.ipInRange("192.168.0.1", "192.168.0.1 - 192.168.0.5"));
        assertTrue(IpUtils.ipInRange("192.168.0.5", "192.168.0.1 - 192.168.0.5"));
        assertFalse(IpUtils.ipInRange("192.168.0.0", "192.168.0.1 - 192.168.0.5"));
        assertFalse(IpUtils.ipInRange("192.168.0.6", "192.168.0.1 - 192.168.0.5"));
        assertTrue(IpUtils.ipInRange("192.168.0.1", "192.168.0.1 - 192.168.0.1"));
        assertFalse(IpUtils.ipInRange("192.168.0.2", "192.168.0.1 - 192.168.0.1"));
        assertFalse(IpUtils.ipInRange("10.0.0.50", "192.168.0.1 - 192.168.0.1"));
        assertTrue(IpUtils.ipInRange("10.0.0.50", "10.0.0.0 - 10.0.0.255"));
        assertTrue(IpUtils.ipInRange("10.0.0.0", "10.0.0.0 - 10.0.0.255"));
        assertTrue(IpUtils.ipInRange("10.0.0.255", "10.0.0.0 - 10.0.0.255"));

        assertTrue(IpUtils.ipInRange("999.999.999.999", "192.168.0.1 - 192.168.0.5"));
        assertTrue(IpUtils.ipInRange("192.168.0", "192.168.0.1 - 192.168.0.5"));
        assertTrue(IpUtils.ipInRange("192.168.0.a", "192.168.0.1 - 192.168.0.5"));
        assertTrue(IpUtils.ipInRange("", "192.168.0.1 - 192.168.0.5"));

        assertTrue(IpUtils.ipInRange("192.168.0.3", "192.168.0.5 - 192.168.0.1"));
        assertTrue(IpUtils.ipInRange("192.168.0.3", "192.168.0.1"));
        assertTrue(IpUtils.ipInRange("192.168.0.3", "192.168.0.1 - "));
        assertTrue(IpUtils.ipInRange("192.168.0.3", " - 192.168.0.5"));
    }

    /**
     * Tests the {@link IpUtils#isRangeFormat(String)} method.
     * It verifies that the method correctly identifies if a string is in the proper IP range format.
     */
    @Test
    public void testIsRangeFormat() {
        assertTrue(IpUtils.isRangeFormat("192.168.0.1 - 192.168.0.5"));
        assertFalse(IpUtils.isRangeFormat("192.168.0.1-192.168.0.5"));
        assertTrue(IpUtils.isRangeFormat("192.168.0.1 - 192.168.0"));
        assertFalse(IpUtils.isRangeFormat("192.168.0.1 - "));
        assertFalse(IpUtils.isRangeFormat(" - 192.168.0.5"));
        assertFalse(IpUtils.isRangeFormat("Not a range"));
    }

    /**
     * Tests the {@link IpUtils#isValidIp(String)} method.
     * It checks both valid and invalid IP addresses to ensure the method correctly validates IPs.
     */
    @Test
    public void testIsValidIp() {
        // Valid IP addresses
        assertTrue(IpUtils.isValidIp("192.168.0.1"));
        assertTrue(IpUtils.isValidIp("10.0.0.255"));
        assertTrue(IpUtils.isValidIp("255.255.255.255"));
        assertTrue(IpUtils.isValidIp("0.0.0.0"));

        // Invalid IP addresses
        assertFalse(IpUtils.isValidIp("256.256.256.256"));
        assertFalse(IpUtils.isValidIp("192.168.0"));
        assertFalse(IpUtils.isValidIp("192.168.0.1.1"));
        assertFalse(IpUtils.isValidIp("192.168.0.a"));
        assertFalse(IpUtils.isValidIp("192.168.0.-1"));
        assertFalse(IpUtils.isValidIp("192.168.0.256"));
        assertFalse(IpUtils.isValidIp("192.168..1"));
        assertFalse(IpUtils.isValidIp("192.168.0.01"));
        assertFalse(IpUtils.isValidIp(""));
        assertFalse(IpUtils.isValidIp(" "));
    }

    /**
     * Tests the {@link IpUtils#isValidRange(String)} method.
     * It ensures that the method correctly validates IP ranges.
     */
    @Test
    public void testIsValidRange() {
        // Valid ranges
        assertTrue(IpUtils.isValidRange("192.168.0.1 - 192.168.0.5"));
        assertTrue(IpUtils.isValidRange("10.0.0.1 - 10.0.0.255"));
        assertTrue(IpUtils.isValidRange("0.0.0.0 - 255.255.255.255"));
        assertTrue(IpUtils.isValidRange("192.168.1.1 - 192.168.1.1"));

        // Invalid ranges
        assertFalse(IpUtils.isValidRange("192.168.0.1 - 192.168.0.256"));
        assertFalse(IpUtils.isValidRange("192.168.0.1 - 192.168.0"));
        assertFalse(IpUtils.isValidRange("192.168.0.1 - "));
        assertFalse(IpUtils.isValidRange(" - 192.168.0.5"));
        assertFalse(IpUtils.isValidRange("192.168.0.1 - 192.168.0.1.1"));
        assertFalse(IpUtils.isValidRange("192.168.0.1 192.168.0.5"));
        assertFalse(IpUtils.isValidRange("192.168.0.1 -192.168.0.5"));
        assertFalse(IpUtils.isValidRange("192.168.0.1- 192.168.0.5"));
        assertFalse(IpUtils.isValidRange("192.168.0.1 -192.168.0.5"));
        assertFalse(IpUtils.isValidRange(""));
        assertFalse(IpUtils.isValidRange(" "));
    }
}