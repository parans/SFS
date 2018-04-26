package com.VirtualDisk;

import com.simplefilesystem.SimpleFileSystemConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by parans on 12/25/16.
 */
public class VirtualDiskHelperTest {

    static final String diskName = "sfsDisk0";
    VirtualDisk virtualDisk;

    @Before
    public void setUp() {
        virtualDisk = VirtualDiskHelper.makeVirtualDisk(diskName);
    }

    @After
    public void tearDown() {
        VirtualDiskHelper.destroyVirtualDisk(virtualDisk);
    }

    @Test
    public void testMakeVirtualDisk() {
        String name = "sfsDisk1";
        VirtualDisk vdisk = VirtualDiskHelper.makeVirtualDisk(name);

        assertTrue("Vdisk is null", vdisk != null);
        //assertTrue("Seems like the disk is not created", vdisk.vDiskName == diskName);
        assertTrue("Seems like the disk is not created", vdisk.vDiskName == name);
        assertTrue("Seems like the disk is not created", vdisk.file != null);
        VirtualDiskHelper.destroyVirtualDisk(vdisk);
    }

    @Test
    public void testOpenVirtualDisk() {
        assertTrue("Could not open virtualDisk", VirtualDiskHelper.openVirtualDisk(virtualDisk));
    }

    @Test
    public void testCloseVirtualDisk() {
        assertTrue("Could not open virtualDisk", VirtualDiskHelper.closeVirtualDisk(virtualDisk));
    }

    @Test
    public void testBlockRead() {
        byte[] buffer = new byte[SimpleFileSystemConstants.BLOCK_SIZE];
        int blocksId;
        for(int i=0; i<10; i++) {
            blocksId = (int)(Math.random()*10000) % SimpleFileSystemConstants.BLOCK_SIZE;
            assertEquals("Could not read", 0, VirtualDiskHelper.blockRead(virtualDisk, blocksId, buffer));
        }
    }

    @Test
    public void testBlockWrite() {
        byte[] buffer = new byte[SimpleFileSystemConstants.BLOCK_SIZE];
        Arrays.fill(buffer, (byte)'z');
        int blocksId;
        for(int i=0; i<5; i++) {
            blocksId = (int)(Math.random()*10000) % SimpleFileSystemConstants.BLOCK_SIZE;
            assertEquals("Could not read", 0, VirtualDiskHelper.blockWrite(virtualDisk, blocksId, buffer));
        }
    }

    @Test
    public void testDestroyVirtualDisk() {
        String name = "sfsDisk1";
        VirtualDisk vdisk = VirtualDiskHelper.makeVirtualDisk(name);
        assertTrue("Could not destroy virtual disk", VirtualDiskHelper.destroyVirtualDisk(vdisk));
    }
}