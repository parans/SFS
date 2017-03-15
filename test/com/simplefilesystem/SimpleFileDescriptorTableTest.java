package com.simplefilesystem;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

/**
 * Created by parans on 2/21/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleFileDescriptorTableTest {

    private SimpleFileDescriptorTable sfdt;

    @Before
    public void initialize() {
        sfdt = new SimpleFileDescriptorTable();
    }

    @Test
    public void testGetFreeFileDescriptor() {
        for(int i=0; i<SimpleFileSystemConstants.MAX_FILE_DESCRIPTOR_COUNT ; i++) {
            assertEquals("Unexpected file descriptor", i, sfdt.getFreeFileDescriptor());
        }
        assertEquals("Unexpected value", -1, sfdt.getFreeFileDescriptor());
    }

    @Test
    public void testReleaseFileDescriptorInvalidFileDescTest() {
        assertEquals("Unexpected value", -2, sfdt.releaseFiledescriptor(100));
        assertEquals("Unexpected value", -2, sfdt.releaseFiledescriptor(-1));
    }

    @Test
    public void testReleaseFileDescriptorHappyCaseTest() {
        for(int i=0; i<SimpleFileSystemConstants.MAX_FILE_DESCRIPTOR_COUNT ; i++) {
            assertEquals("Unexpected file descriptor", i, sfdt.getFreeFileDescriptor());
        }

        for(int i=0; i<SimpleFileSystemConstants.MAX_FILE_DESCRIPTOR_COUNT; i++) {
            assertEquals("Unexpected return value", 0, sfdt.releaseFiledescriptor(i));
        }
    }

    @Test
    public void testAttachFileToFileDescriptorValidation() {
        SimpleFileDescriptor sfd = null;
        assertEquals("Unexpected return value", -2, sfdt.attachFileToFileDescriptor(sfd));
        sfd = new SimpleFileDescriptor(null, -9, SimpleFileSystemConstants.BLOCK_SIZE);
        sfd.simpleFileRef = null;
        assertEquals("Unexpected return value", -2, sfdt.attachFileToFileDescriptor(sfd));
        sfd.simpleFileRef = new SimpleFile("kaka", 3, 8, 10, 5L, 10L, 3);
        sfd.currentBlockId = -9;
        assertEquals("Unexpected return value", -2, sfdt.attachFileToFileDescriptor(sfd));
        sfd.currentBlockId = 10;
        sfd.offset = SimpleFileSystemConstants.BLOCK_SIZE;
        assertEquals("Unexpected return value", -2, sfdt.attachFileToFileDescriptor(sfd));
    }

    @Test
    public void testAttachFileToFileDescriptorHappyCase() {
        SimpleFileDescriptor sfd = new SimpleFileDescriptor(null, -9, SimpleFileSystemConstants.BLOCK_SIZE);
        sfd.simpleFileRef = new SimpleFile("kaka", 3, 8, 10, 5L, 10L, 3);
        sfd.currentBlockId = SimpleFileSystemConstants.USER_SPACE_OFFSET+3;
        sfd.offset = SimpleFileSystemConstants.BLOCK_SIZE-3;
        int fileDesc = sfdt.attachFileToFileDescriptor(sfd);
        assertTrue("Unexpected return value", fileDesc >=0 && fileDesc < SimpleFileSystemConstants.MAX_FILE_DESCRIPTOR_COUNT);
    }
}
