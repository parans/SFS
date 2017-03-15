package com.simplefilesystem;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.mock;

/**
 * Created by parans on 2/11/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleFileAllocationTableTest {

    private static SimpleFileAllocationTable sfat;

    @BeforeClass
    public static void initialize() {
        sfat = new SimpleFileAllocationTable();
    }

    @Test
    public void testHappyCaseValidate() {
        int[] allocationTable = new int[SimpleFileSystemConstants.FAT_TABLE_SIZE];
        Arrays.fill(allocationTable, 0, SimpleFileSystemConstants.USER_SPACE_OFFSET, -1);
        Arrays.fill(allocationTable, SimpleFileSystemConstants.USER_SPACE_OFFSET,
                SimpleFileSystemConstants.FAT_TABLE_SIZE, SimpleFileSystemConstants.USER_SPACE_OFFSET);
        assertTrue("Validation failed", sfat.validateIndexes(allocationTable));
    }

    @Test
    public void testInvalidLength() {
        int[] allocationTable = new int[SimpleFileSystemConstants.FAT_TABLE_SIZE+5];
        Arrays.fill(allocationTable, 120);
        assertFalse("Validation failed", sfat.validateIndexes(allocationTable));
    }

    @Test
    public void testNullArray() {
        int[] allocationTable = null;
        assertFalse("Validation failed", sfat.validateIndexes(allocationTable));
    }

    @Test
    public void testInvalidValue() {
        int[] allocationTable = new int[SimpleFileSystemConstants.FAT_TABLE_SIZE];
        Arrays.fill(allocationTable, -4);
        assertFalse("Validation failed", sfat.validateIndexes(allocationTable));
    }

    @Test
    public void testInvalidGreaterValue() {
        int[] allocationTable = new int[SimpleFileSystemConstants.FAT_TABLE_SIZE];
        Arrays.fill(allocationTable, 10000);
        assertFalse("Validation failed", sfat.validateIndexes(allocationTable));
    }

    @Test
    public void testFalseInit() {
        int[] allocationTable = new int[SimpleFileSystemConstants.FAT_TABLE_SIZE];
        Arrays.fill(allocationTable, 10000);
        assertNotEquals("Init succeeded, something went wrong", 0, sfat.initFileAllocationTable(allocationTable));
    }

    @Test
    public void testTrueInit() {
        int[] allocationTable = new int[SimpleFileSystemConstants.FAT_TABLE_SIZE];
        Arrays.fill(allocationTable, 0, SimpleFileSystemConstants.USER_SPACE_OFFSET, 30);
        Arrays.fill(allocationTable, SimpleFileSystemConstants.USER_SPACE_OFFSET,
                SimpleFileSystemConstants.FAT_TABLE_SIZE, SimpleFileSystemConstants.USER_SPACE_OFFSET);
        assertEquals("Init failed", 0, sfat.initFileAllocationTable(allocationTable));
    }

    @Test
    public void testGetValueHappyCase() {
        int[] allocationTable = new int[SimpleFileSystemConstants.FAT_TABLE_SIZE];
        Arrays.fill(allocationTable, 0, SimpleFileSystemConstants.USER_SPACE_OFFSET, 30);
        Arrays.fill(allocationTable, SimpleFileSystemConstants.USER_SPACE_OFFSET,
                SimpleFileSystemConstants.FAT_TABLE_SIZE, SimpleFileSystemConstants.USER_SPACE_OFFSET);
        sfat.initFileAllocationTable(allocationTable);
        assertEquals("Unexpected value", SimpleFileSystemConstants.USER_SPACE_OFFSET, sfat.getValueAt(5213));
    }

    @Test
    public void testGetValueExceptionCase() {
        assertEquals("Unexpected value", -1, sfat.getValueAt(-60));
    }

    @Test
    public void testResetValueHappyCase() {
        int[] allocationTable = new int[SimpleFileSystemConstants.FAT_TABLE_SIZE];
        Arrays.fill(allocationTable, 0, SimpleFileSystemConstants.USER_SPACE_OFFSET, 30);
        Arrays.fill(allocationTable, SimpleFileSystemConstants.USER_SPACE_OFFSET,
                SimpleFileSystemConstants.FAT_TABLE_SIZE, SimpleFileSystemConstants.USER_SPACE_OFFSET);
        sfat.initFileAllocationTable(allocationTable);
        assertEquals("Unexpected value", 0, sfat.resetValueAt(5213));
    }

    @Test
    public void testResetValueExceptionCase() {
        assertEquals("Unexpected value", -1, sfat.resetValueAt(20000));
    }
}
