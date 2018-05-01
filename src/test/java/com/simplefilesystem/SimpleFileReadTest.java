package com.simplefilesystem;

import com.VirtualDisk.VirtualDisk;
import com.VirtualDisk.VirtualDiskHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Created by parans on 3/1/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleFileReadTest {

    private static final String FILE_NAME = "Victor";
    private static final int START_BLOCK_ID = 5000;
    private static final int FINAL_BLOCK_ID = 5004;
    private static final int FILE_SIZE = 16384;

    private SimpleFileSystem sfsHandle;

    private SimpleFileMetadataTable sfmt;
    private SimpleFileBlockAllocator sfba;

    private SimpleFileDescriptorTable sfdt;
    private SimpleFileAllocationTable sfat;
    private VirtualDisk virtualDisk;
    private SimpleFile simplefile;
    private SimpleFileDescriptor simpleFileDescriptor;

    @Before
    public void initialize() {
        sfmt = new SimpleFileMetadataTable();
        sfat = new SimpleFileAllocationTable();
        sfba = new SimpleFileBlockAllocator();
        sfdt = new SimpleFileDescriptorTable();

        simplefile = new SimpleFile(FILE_NAME, START_BLOCK_ID, START_BLOCK_ID, 0, System.currentTimeMillis(),
                System.currentTimeMillis(), 1);

        simpleFileDescriptor = new SimpleFileDescriptor(simplefile, 0, simplefile.startBlockId);

        virtualDisk = VirtualDiskHelper.makeVirtualDisk("testDisk");

        sfsHandle = new SimpleFileSystem(sfat, sfmt, sfba, sfdt, virtualDisk);
    }

    @After
    public void tearDown()
    {
        VirtualDiskHelper.destroyVirtualDisk(virtualDisk);
    }

    @Test
    public void testFsWrite() {
        sfmt.put(FILE_NAME, simplefile);
        int fd = sfdt.attachFileToFileDescriptor(simpleFileDescriptor);

        sfba.releaseFreeBlock(FINAL_BLOCK_ID);
        sfba.releaseFreeBlock(FINAL_BLOCK_ID+3);

        byte[] buffer = new byte[10];
        for(int i=0; i<10; i++) {
            buffer[i] = 'a';
        }
        assertEquals("File deletion has failed", 10, sfsHandle.fsWrite(fd, buffer, 10));
    }

    @Test
    public void testFsWriteHappyCase() {
        sfmt.put(FILE_NAME, simplefile);
        int fd = sfdt.attachFileToFileDescriptor(simpleFileDescriptor);

        sfba.releaseFreeBlock(FINAL_BLOCK_ID);
        sfba.releaseFreeBlock(FINAL_BLOCK_ID+3);

        byte[] buffer = new byte[SimpleFileSystemConstants.BLOCK_SIZE];
        for(int i=0; i<SimpleFileSystemConstants.BLOCK_SIZE; i++) {
            buffer[i] = 'a';
        }
        assertEquals("File deletion has failed", SimpleFileSystemConstants.BLOCK_SIZE, sfsHandle.fsWrite(fd, buffer, SimpleFileSystemConstants.BLOCK_SIZE));
    }
}
