package com.simplefilesystem;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
/**
 * Created by parans on 2/25/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleFileMetadataTableTest {

    private SimpleFileMetadataTable sfmt;

    @Before
    public void initialize() {
        sfmt = new SimpleFileMetadataTable();
    }

    @Test
    public void testFileCountTest() {
        assertEquals("Must be equal to 0", 0, sfmt.getFileCount());
        sfmt.put("whatever", new SimpleFile("whatever",0, 10, 5, 9L, 8L, 2));
        sfmt.put("whatever2", new SimpleFile("whatever2", 0, 10, 5, 9L, 8L, 2));
        assertEquals("Must be equal to 2", 2, sfmt.getFileCount());
    }

    @Test
    public void testGetFile() {
        assertEquals("Must be null", null, sfmt.getFile(null));
        SimpleFile x = new SimpleFile("whatever",0, 10, 5, 9L, 8L, 2);
        sfmt.put("whatever", x);
        sfmt.put("whatever2", new SimpleFile("whatever2", 0, 10, 5, 9L, 8L, 2));
        assertEquals("Must be equal to x", x, sfmt.getFile("whatever"));
    }

    @Test
    public void testExists() {
        SimpleFile x = new SimpleFile("whatever",0, 10, 5, 9L, 8L, 2);
        sfmt.put("whatever", x);
        sfmt.put("whatever2", new SimpleFile("whatever2", 0, 10, 5, 9L, 8L, 2));
        assertTrue("Must be true",  sfmt.exists("whatever"));
        assertFalse("Must be false", sfmt.exists(null));
        assertFalse("Must be false", sfmt.exists("shata"));
    }

    @Test
    public void testPutValidation() {
        SimpleFile x = new SimpleFile("whatever",0, 10, 5, 9L, 8L, 2);
        assertEquals("Must be equals to -2", -2, sfmt.put(null, x));
        assertEquals("Must be equals to -2", -2, sfmt.put("whatever", null));
        assertEquals("Must be equals to -2", -2, sfmt.put(null, x));
    }

    @Test
    public void testPutHappyCase() {
        SimpleFile x = new SimpleFile("whatever",0, 10, 5, 9L, 8L, 2);
        assertEquals("Must be equals to 0", 0, sfmt.put("whatever", x));
    }

    @Test
    public void testRemoveHappyCase() {
        SimpleFile x = new SimpleFile("whatever",0, 10, 5, 9L, 8L, 2);
        assertEquals("Must be equals to 0", 0, sfmt.put("whatever", x));
        assertEquals("Must be eqyal to 0", 0, sfmt.remove("whatever"));
    }
}
