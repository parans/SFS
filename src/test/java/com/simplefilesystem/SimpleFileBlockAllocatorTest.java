package com.simplefilesystem;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by parans on 2/11/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleFileBlockAllocatorTest {

    private SimpleFileBlockAllocator sfba;

    @Before
    public void initialize() {
        sfba = new SimpleFileBlockAllocator();
    }

    @Test
    public void testValidateFreeBlockListNull() {
        assertFalse("Must return false but returned true", sfba.validateFreeBlockIndexList(null));
    }

    @Test
    public void testValidateFreeBlockListBigList() {
        List<Integer> fbil = new ArrayList<>();
        for(int i=0; i<=SimpleFileSystemConstants.USER_SPACE_OFFSET; i++) {
            fbil.add(10);
        }
        assertFalse("Must return false but returned true", sfba.validateFreeBlockIndexList(fbil));
    }

    @Test
    public void testValidateFreeBlockListInvalidValue() {
        List<Integer> fbil = new ArrayList<>();
        for(int i=0; i<SimpleFileSystemConstants.USER_SPACE_OFFSET; i++) {
            fbil.add(-4);
        }
        assertFalse("Must return false but returned true", sfba.validateFreeBlockIndexList(fbil));
        fbil.set(10, SimpleFileSystemConstants.FAT_TABLE_SIZE+3);
        assertFalse("Must return false but returned true", sfba.validateFreeBlockIndexList(fbil));
    }

    @Test
    public void testValidateFreeBlockListHappyCase() {
        List<Integer> fbil = new ArrayList<>();
        for(int i=0; i<SimpleFileSystemConstants.USER_SPACE_OFFSET; i++) {
            fbil.add(SimpleFileSystemConstants.USER_SPACE_OFFSET + 1);
        }
        assertTrue("Must return true but returned false", sfba.validateFreeBlockIndexList(fbil));
    }

    @Test
    public void testInitInvalidFreeBlockList() {
        List<Integer> fbil = new ArrayList<>();
        for(int i=0; i<SimpleFileSystemConstants.USER_SPACE_OFFSET; i++) {
            fbil.add(10);
        }
        assertEquals("Must return false but returned true", -2, sfba.depositFreeBlocks(fbil));
    }

    @Test
    public void testInitHappyCaseDup() {
        List<Integer> fbil = new ArrayList<>();
        for(int i=0; i<SimpleFileSystemConstants.USER_SPACE_OFFSET; i++) {
            fbil.add(SimpleFileSystemConstants.USER_SPACE_OFFSET + 1);
        }
        assertEquals("Must return true but returned false", 0, sfba.depositFreeBlocks(fbil));
    }

    @Test
    public void testInitHappyCaseNoDup() {
        List<Integer> fbil = new ArrayList<>();
        for(int i=0; i<SimpleFileSystemConstants.USER_SPACE_OFFSET; i++) {
            fbil.add(SimpleFileSystemConstants.USER_SPACE_OFFSET + i);
        }
        assertEquals("Must return true but returned false", 0, sfba.depositFreeBlocks(fbil));
    }

    @Test
    public void testGetFreeBlockIndex() {
        List<Integer> fbil = new ArrayList<>();
        for(int i=0; i<SimpleFileSystemConstants.USER_SPACE_OFFSET; i++) {
            fbil.add(SimpleFileSystemConstants.USER_SPACE_OFFSET + i);
        }
        sfba.depositFreeBlocks(fbil);
        assertNotEquals("Invalid value", -1, sfba.getFreeBlockIndex());
    }

    @Test
    public void testGetFreeBlockIndexNoFreeBlock() {
        assertEquals("Invalid value", -1, sfba.getFreeBlockIndex());
    }

    @Test
    public void testAddFreeBlockIndexLesser() {
        assertEquals("Expected false but was true", -2, sfba.releaseFreeBlock(SimpleFileSystemConstants.USER_SPACE_OFFSET-1));
    }

    @Test
    public void testAddFreeBlockIndexGreater() {
        assertEquals("Expected false but was true", -2, sfba.releaseFreeBlock(SimpleFileSystemConstants.FAT_TABLE_SIZE));
    }

    @Test
    public void testAddFreeBlockIndex() {
        assertEquals("Expected true but was false", 0, sfba.releaseFreeBlock(SimpleFileSystemConstants.USER_SPACE_OFFSET));
    }

    @Test
    public void testAddFreeBlockIndexFalse() {
        assertEquals("Expected true but was false", 0, sfba.releaseFreeBlock(SimpleFileSystemConstants.USER_SPACE_OFFSET));
    }
}
