package com.simplefilesystem;

import com.VirtualDisk.VirtualDisk;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by parans on 12/30/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class SFSFileDeleteTest {
    private static final String FILE_NAME = "Victor";
    private static final int START_BLOCK_ID = 5000;
    private static final int FINAL_BLOCK_ID = 5004;
    private static final int FILE_SIZE = 16384;

    private static SimpleFileSystem sfsHandle;

    private static SimpleFileMetadataTable sfmt;
    private static SimpleFileBlockAllocator sfba;

    private static SimpleFileDescriptorTable sfdt;
    private static SimpleFileAllocationTable sfat;
    private static VirtualDisk virtualDisk;
    private static SimpleFile simplefile;

    @Before
    public void initialize() {
        sfmt = new SimpleFileMetadataTable();
        sfat = new SimpleFileAllocationTable();
        sfba = new SimpleFileBlockAllocator();
        sfdt = new SimpleFileDescriptorTable();

        simplefile = new SimpleFile(FILE_NAME, START_BLOCK_ID, FINAL_BLOCK_ID, FILE_SIZE, System.currentTimeMillis(),
                System.currentTimeMillis(), 0);

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
    public void testFsDelete() {
        sfmt.put(FILE_NAME, simplefile);
        sfat.setValueAt(START_BLOCK_ID, START_BLOCK_ID + 1);
        sfat.setValueAt(START_BLOCK_ID + 1, START_BLOCK_ID + 2);
        sfat.setValueAt(START_BLOCK_ID + 2, START_BLOCK_ID + 20);
        sfat.setValueAt(START_BLOCK_ID + 3, START_BLOCK_ID + 21);
        sfat.resetValueAt(START_BLOCK_ID + 4);
        assertEquals("File deletion has failed", 0, sfsHandle.fsDelete(FILE_NAME));
    }

    @Test
    public void testFsDeleteFileCount0() {
        assertEquals("File creation has succeeded", -1, sfsHandle.fsDelete(FILE_NAME));
    }

    @Test
    public void testFsDeleteEmptyFileName() {
        assertEquals("File deletion has succeeded", -1, sfsHandle.fsDelete(" "));
    }

    @Test
    public void testFsDeleteFileDoesNotExist() {
        sfmt.put("Shata", simplefile);
        assertEquals("File deletion was successful", -1, sfsHandle.fsDelete(FILE_NAME));
    }

    @Test
    public void testFsDeleteOpenFileDescriptor() {
        simplefile.fileDescriptorCount = 2;
        sfmt.put(FILE_NAME, simplefile);
        assertEquals("File deletion was successful", -1, sfsHandle.fsDelete(FILE_NAME));
    }

    @Test
    public void testFsDeleteInvalidBlocks() {
        sfmt.put(FILE_NAME, simplefile);
        sfat.setValueAt(START_BLOCK_ID, 6000);
        sfat.setValueAt(6000, 120);
        sfat.setValueAt(120, 8000);
        sfat.setValueAt(8000, 5004);
        sfat.setValueAt(5004, -1);
        assertEquals("File deletion has succeeded", -1, sfsHandle.fsDelete(FILE_NAME));
    }
}
