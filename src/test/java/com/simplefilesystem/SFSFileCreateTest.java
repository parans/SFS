package com.simplefilesystem;

import com.VirtualDisk.VirtualDisk;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This is an integration test suite, there is no need to mock
 * Created by parans on 12/26/16.
 */
//@RunWith(MockitoJUnitRunner.class)
public class SFSFileCreateTest {

    private static final String FILE_NAME = "Victor";
    private static SimpleFileSystem sfsHandle;

    private SimpleFileMetadataTable sfmt;
    private SimpleFileBlockAllocator sfba;

    private SimpleFileDescriptorTable sfdt;
    private SimpleFileAllocationTable sfat;
    private VirtualDisk virtualDisk;

    @Before
    public void initialize() {
        sfmt = new SimpleFileMetadataTable();
        sfba = new SimpleFileBlockAllocator();
        sfat = new SimpleFileAllocationTable();
        sfdt = new SimpleFileDescriptorTable();
        File disk0;
        disk0 = new File("/Users/parans/testDisk");
        virtualDisk = new VirtualDisk(disk0, "simpleTestDisk");
        sfsHandle = new SimpleFileSystem(sfat, sfmt, sfba, sfdt, virtualDisk);
    }

    @After
    public void tearDown()
    {
        virtualDisk.getFileHandle().delete();
    }

    @Test
    public void testFsCreate() {
        List<Integer> freeBlocksList = new LinkedList<>();
        freeBlocksList.add(4096);
        freeBlocksList.add(5001);
        assertEquals("Failed to deposit free blocks", 0, sfba.depositFreeBlocks(freeBlocksList));
        assertEquals("File creation has failed", 0, sfsHandle.fsCreate(FILE_NAME));
        SimpleFile simpleFile = sfmt.getFile(FILE_NAME);
        assertEquals(FILE_NAME, simpleFile.fileName);
        assertEquals(4096, simpleFile.startBlockId);
    }

    @Test
    public void testFsCreateMaxFilecount() {
        int i=0;
        List<Integer> freeBlocksList = new LinkedList<>();
        for(i=0; i<SimpleFileSystemConstants.USER_SPACE_OFFSET; i++) {
            freeBlocksList.add(SimpleFileSystemConstants.USER_SPACE_OFFSET+i);
        }
        assertEquals("Failed to deposit free blocks", 0, sfba.depositFreeBlocks(freeBlocksList));

        for(i=0; i<SimpleFileSystemConstants.MAX_FILE_COUNT; i++) {
            assertEquals("File creation has failed", 0, sfsHandle.fsCreate(FILE_NAME+i));
        }
        assertEquals("File creation has succeeded", -1, sfsHandle.fsCreate(FILE_NAME+i));
    }

    @Test
    public void testFsCreateEmptyFileName() {
        assertEquals("File creation has succeeded", -1, sfsHandle.fsCreate(" "));
    }

    @Test
    public void testFsCreateDuplicateFiles() {
        int i = 0;
        List<Integer> freeBlocksList = new LinkedList<>();
        for(i=0; i<SimpleFileSystemConstants.USER_SPACE_OFFSET; i++) {
            freeBlocksList.add(SimpleFileSystemConstants.USER_SPACE_OFFSET+i);
        }
        assertEquals("Failed to deposit free blocks", 0, sfba.depositFreeBlocks(freeBlocksList));
        assertEquals("File creation has failed", 0, sfsHandle.fsCreate(FILE_NAME));
        assertEquals("File creation has succeeded", -1, sfsHandle.fsCreate(FILE_NAME));
    }

    @Test
    public void testFsCreateInvalidBlocks() {
        assertEquals("File creation has succeeded", -1, sfsHandle.fsCreate(FILE_NAME));
        sfba = mock(SimpleFileBlockAllocator.class);
        when(sfba.getFreeBlockIndex()).thenReturn(-1,
                SimpleFileSystemConstants.DISK_BLOCKS,
                SimpleFileSystemConstants.USER_SPACE_OFFSET-1);
        assertEquals("File creation has succeeded", -1, sfsHandle.fsCreate(FILE_NAME));
        assertEquals("File creation has succeeded", -1, sfsHandle.fsCreate(FILE_NAME));
        assertEquals("File creation has succeeded", -1, sfsHandle.fsCreate(FILE_NAME));
        assertEquals("File creation has succeeded", -1, sfsHandle.fsCreate(FILE_NAME));
    }
}
