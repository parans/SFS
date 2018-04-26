package com.simplefilesystem;

import com.VirtualDisk.VirtualDisk;
import com.VirtualDisk.VirtualDiskHelper;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by parans on 11/29/16.
 */
public class SimpleFileSystem {

    public static final int BLOCK_SIZE = 4096;

    ReentrantLock sfsFileCreateMutex = new ReentrantLock(true /*ensure fairness*/);

    public static final long CONCURRENT_CREATE_WAIT_TIMEOUT = 500L;
    public static final int FILE_NAME_LEN = 15;

    //file system code has a lot of synchronization
    protected SimpleFileAllocationTable sfat;

    protected SimpleFileMetadataTable sfmt;

    protected SimpleFileBlockAllocator sfba;

    protected SimpleFileDescriptorTable sfdt;

    protected VirtualDisk virtualDisk;

    public SimpleFileSystem(SimpleFileAllocationTable sfat, SimpleFileMetadataTable sfmt,
                            SimpleFileBlockAllocator sfba, SimpleFileDescriptorTable sfdt,
                            VirtualDisk virtualDisk) {
        this.sfmt = sfmt;
        this.sfat = sfat;
        this.sfba = sfba;
        this.sfdt = sfdt;
        this.virtualDisk = virtualDisk;
    }

    /**
     *
     * @param fileName
     * @return 0 on success and -1 on failure
     */
    public int fsCreate(final String fileName) {

        int success = 0;
        int freeBlockIndex;
        SimpleFile simpleFile;

        if(fileName == null) {
            return -1;
        }

        String fName = fileName.trim();

        if(fName.length() > FILE_NAME_LEN || fName.length() == 0) {
            return -1;
        }

        if(sfmt.getFileCount() >= SimpleFileSystemConstants.MAX_FILE_COUNT) {
            return -1;
        }

        //If file does not exists, then try to create one now
        //First allocate a block from the free block allocator
        //This method will not block, if it not able to get a free block -1 is returned
        freeBlockIndex = sfba.getFreeBlockIndex();

        if(freeBlockIndex < 0
                || freeBlockIndex >= SimpleFileSystemConstants.DISK_BLOCKS
                || freeBlockIndex < SimpleFileSystemConstants.USER_SPACE_OFFSET ) {
            return -1;
        }

        simpleFile = new SimpleFile(fName, freeBlockIndex, freeBlockIndex,
                0, System.currentTimeMillis(),
                System.currentTimeMillis(), 0);

        try {
            if(sfsFileCreateMutex.tryLock(CONCURRENT_CREATE_WAIT_TIMEOUT, TimeUnit.MILLISECONDS)) {
                //If I was able to acquire the lock
                //check if file exists
                if(sfmt.exists(fName)) {
                    success = -1;
                }
                if(sfmt.put(fileName, simpleFile) < 0) {
                    success = -1;
                }
            }

        } catch (InterruptedException ie) {
            //The thread was interrupted while trying to acquire the lock
            success = -1;
            System.out.println(ie.getMessage());
        }
        finally {
            sfsFileCreateMutex.unlock();
            sfba.releaseFreeBlock(freeBlockIndex);
        }
        return success;
    }

    /**
     *
     * @param fileName
     * @return 0 on success and -1 on failure
     */
    int fsDelete(String fileName) {
        //Need to handle errors a little better
        int failure = -1;

        //This check is needed because CHM will throw NullPointerException
        if(fileName == null) {
            return failure;
        }

        //You should take care of open file descriptors and its blocks
        int curBlockIndex, nextBlockIndex;
        SimpleFile simpleFile = sfmt.getFile(fileName);
        if(simpleFile == null) {
            return failure;
        }

        synchronized (simpleFile)
        {
           if(simpleFile.fileDescriptorCount > 0) {
               return failure;
           }
            //The link to the blocks are reset
            curBlockIndex = simpleFile.startBlockId;

            while(curBlockIndex != -1 && curBlockIndex > SimpleFileSystemConstants.USER_SPACE_OFFSET)
            {
                nextBlockIndex = sfat.getValueAt(curBlockIndex);
                if(sfat.resetValueAt(curBlockIndex) < 0 ) {
                        System.out.println("fsDelete, restValue error");
                }
                //Release currentBlock back to free blocks list
                //Reset the block on the disk;

                //Even if there is a failure in releasing th block to disk we
                // continue with the deletion but we log a panic
                if(sfba.releaseFreeBlock(curBlockIndex) < 0) {
                    System.out.println("fsDelete, releaseFreeBlock error");
                }
                curBlockIndex = nextBlockIndex;

            }
            //Now remove the file record from the metadata table
            if(curBlockIndex == -1)
            {
                simpleFile.startBlockId = -1;
                if (sfmt.remove(fileName) == 0) {
                    failure = 0;
                }
            }
            else {
                //This means that the file is allocated some system blocks and needs to log a panic
                System.out.println("fsDelete, trying to delete a system block:"+curBlockIndex);
            }
        }
        return failure;
    }

    /**
     *
     * @param fileName
     * @return filedescriptor or -1 on failure
     */
    int fsOpen(String fileName) {
        int fd = -1;
        SimpleFileDescriptor simpleFileDescriptor = null;

        //This check is needed else the CHM will throw null pointer exception
        if(fileName == null)
        {
            return fd;
        }

        //file may or may not exist, if there is an ongoing delete
        SimpleFile simpleFile = sfmt.getFile(fileName);
        if(simpleFile == null)
        {
            //The file has been deleted
            return fd;
        }
        //The file was deleted after this reference was taken and happy case of file not deleted
        synchronized (simpleFile)
        {
            if(simpleFile.startBlockId == -1)
            {
                return fd;
            }
            simpleFileDescriptor = new SimpleFileDescriptor(simpleFile, 0, simpleFile.startBlockId);
            fd = sfdt.attachFileToFileDescriptor(simpleFileDescriptor);
            if(fd >= 0)
            {
                simpleFile.fileDescriptorCount++;
            }
        }
        return fd;
    }

    /**
     * Should I flush the contents to disk
     * @param fileDescriptor
     * @return
     */
    int fsClose(int fileDescriptor) {
        int failure = -1;
        SimpleFileDescriptor simpleFileDescriptor = null;

        if(fileDescriptor > 31 || fileDescriptor < 0) {
            return failure;
        }

        simpleFileDescriptor = sfdt.getFileForFileDescriptor(fileDescriptor);

        if(simpleFileDescriptor == null) {
            return failure;
        }

        //When file is open deletion does not happen
        synchronized(simpleFileDescriptor) {
            //When 2 threads are trying to close the same fd
            if(simpleFileDescriptor.simpleFileRef != null) {
                sfdt.resetValueAt(fileDescriptor);
                //Release filedescriptor back to free filedescriptor pool
                sfdt.releaseFiledescriptor(new Integer(fileDescriptor));
                //This the case when the same file is being opened
                synchronized (simpleFileDescriptor.simpleFileRef) {
                    simpleFileDescriptor.simpleFileRef.fileDescriptorCount--;
                    simpleFileDescriptor.simpleFileRef.lastModificationTime = System.currentTimeMillis();
                }

                simpleFileDescriptor.simpleFileRef = null;
                simpleFileDescriptor.currentBlockId = -1;
                failure = 0;
            }
        }
        return failure;
    }


    public int getEndIndexOfBlock(SimpleFile fileRef, int blockId) {
        int endIndex = -1;
        if(blockId <0 || blockId >=SimpleFileSystemConstants.DISK_BLOCKS) {
            System.out.println("BlockIds out of bounds");
            return endIndex;
        }
        //This was the final block
        if(sfat.getValueAt(blockId) == -1) {
            endIndex = fileRef.endOfFileIndex;
        }
        else {
            //This was not the final block
            endIndex = SimpleFileSystemConstants.BLOCK_SIZE - 1;
        }
        return endIndex;
    }

    /**
     *
     * @param fileDescriptor
     * @param buffer
     * @param size
     * @return readBytes on success and -1 on failure
     */
    int fsRead(int fileDescriptor, char[] buffer, int size) {

        int readSize = 0;
        int remainingSize = 0, estimatedSize = 0, endOfBlockIndex = -1, readableSize = 0;
        int userBufferOffset = 0;

        byte[] blockBuffer = new byte[BLOCK_SIZE];
        SimpleFileDescriptor simpleFileDescriptor = null;

        if(buffer == null || size < 0)
        {
            return -1;
        }
        estimatedSize = buffer.length < size ? buffer.length : size;

        if(fileDescriptor > 31 || fileDescriptor < 0)
        {
            return -1;
        }

        //The file has already been closed
        simpleFileDescriptor = sfdt.getFileForFileDescriptor(fileDescriptor);
        if(simpleFileDescriptor == null)
        {
            return -1;
        }

        synchronized (simpleFileDescriptor)
        {
            //This is case when file was closed in parallel when this thread was waiting for the lock
            if(simpleFileDescriptor.simpleFileRef == null && simpleFileDescriptor.currentBlockId == -1
                    && simpleFileDescriptor.offset == -1)
            {
                return -1;
            }
            remainingSize = estimatedSize;
            while(remainingSize > 0)
            {
                if (VirtualDiskHelper.blockRead(virtualDisk, simpleFileDescriptor.currentBlockId, blockBuffer) < 0)
                {
                    System.out.println("Reading from disk failed:");
                    break;
                }

                endOfBlockIndex = getEndIndexOfBlock(simpleFileDescriptor.simpleFileRef, simpleFileDescriptor.currentBlockId);
                if (endOfBlockIndex < 0)
                {
                    System.out.println("Could not get end index of file");
                    break;
                }

                readableSize = (endOfBlockIndex - simpleFileDescriptor.offset) + 1;
                readSize = remainingSize <= readableSize ? remainingSize : readableSize;
                try
                {
                    System.arraycopy(blockBuffer, simpleFileDescriptor.offset, buffer, userBufferOffset, readSize);
                    userBufferOffset += readSize;
                }
                catch(Exception e)
                {
                    System.out.println("failed to copy data from block buffer to user buffer:"+e.getMessage());
                    break;
                }

                simpleFileDescriptor.offset = simpleFileDescriptor.offset + readSize;
                //This is the final block of the file
                if (simpleFileDescriptor.currentBlockId == simpleFileDescriptor.simpleFileRef.finalBlockId)
                {
                    break;
                }

                if(simpleFileDescriptor.offset % SimpleFileSystemConstants.BLOCK_SIZE == 0) {
                    simpleFileDescriptor.currentBlockId = sfat.getValueAt(simpleFileDescriptor.currentBlockId);
                }
                remainingSize = remainingSize - readSize;
            }
            simpleFileDescriptor.simpleFileRef.lastModificationTime = System.currentTimeMillis();
        }
        return userBufferOffset;
    }

    /**
     *
     * @param fileDescriptor
     * @param buffer
     * @param size
     * @return no. of bytes written or -1 on error
     */

    int fsWrite(int fileDescriptor, byte[] buffer, int size) {
        //Be mindful that max file size allowed is 16MB
        //Another condition is Disk is full, cannot write anymore bytes
        int writeSize = 0;
        int remainingSize = 0, estimatedSize = 0, userBufferOffset = 0, writableSize = 0, newFreeBlock;

        byte[] blockBuffer = new byte[BLOCK_SIZE];
        SimpleFileDescriptor simpleFileDescriptor = null;

        if(buffer == null || size < 0)
        {
            return -1;
        }
        estimatedSize = buffer.length < size ? buffer.length : size;

        if(fileDescriptor > 31 || fileDescriptor < 0)
        {
            return -1;
        }

        //The file has already been closed
        simpleFileDescriptor = sfdt.getFileForFileDescriptor(fileDescriptor);
        if(simpleFileDescriptor == null)
        {
            return -1;
        }

        synchronized (simpleFileDescriptor)
        {
            //This is case when file was closed in parallel when this thread was waiting for the lock
            if(simpleFileDescriptor.simpleFileRef == null && simpleFileDescriptor.currentBlockId == -1
                    && simpleFileDescriptor.offset == -1)
            {
                return -1;
            }

            remainingSize = estimatedSize;
            while(remainingSize > 0 && simpleFileDescriptor.simpleFileRef.fileSize < SimpleFileSystemConstants.MAX_FILE_SIZE)
            {
                //This means that you have reached the end of the block and the file you need to allocate a new one
                if(simpleFileDescriptor.offset == SimpleFileSystemConstants.BLOCK_SIZE &&
                        simpleFileDescriptor.currentBlockId == simpleFileDescriptor.simpleFileRef.finalBlockId)
                {
                    newFreeBlock = sfba.getFreeBlockIndex();
                    if(newFreeBlock == -1)
                    {
                        System.out.println("Allocating free block has failed:");
                        break;
                    }

                    if(sfat.setValueAt(simpleFileDescriptor.simpleFileRef.finalBlockId, newFreeBlock) < 0)
                    {
                        System.out.println("Setting file allocation table has failed");
                        break;
                    }
                    simpleFileDescriptor.simpleFileRef.finalBlockId = newFreeBlock;
                    simpleFileDescriptor.currentBlockId = newFreeBlock;
                    simpleFileDescriptor.offset = 0;
                }

                if (VirtualDiskHelper.blockRead(virtualDisk, simpleFileDescriptor.currentBlockId, blockBuffer) < 0)
                {
                    System.out.println("Reading from block buffer failed:");
                    break;
                }

                writableSize = SimpleFileSystemConstants.BLOCK_SIZE - simpleFileDescriptor.offset;
                writeSize = remainingSize <= writableSize ? remainingSize : writableSize;
                try
                {
                    System.arraycopy(buffer, userBufferOffset, blockBuffer, simpleFileDescriptor.offset, writeSize);
                    userBufferOffset += writeSize;
                }
                catch(Exception e)
                {
                    System.out.println("Copy from user buffer to block buffer failed:"+e.getMessage());
                    break;
                }

                if (VirtualDiskHelper.blockWrite(virtualDisk, simpleFileDescriptor.currentBlockId, blockBuffer) < 0)
                {
                    System.out.println("Writing block buffer to disk failed");
                    break;
                }

                simpleFileDescriptor.simpleFileRef.fileSize += writeSize;
                if(simpleFileDescriptor.currentBlockId == simpleFileDescriptor.simpleFileRef.finalBlockId)
                {
                    simpleFileDescriptor.simpleFileRef.endOfFileIndex = simpleFileDescriptor.offset;
                }
                simpleFileDescriptor.offset = simpleFileDescriptor.offset + writeSize;

                remainingSize = remainingSize - writeSize;
            }
            simpleFileDescriptor.simpleFileRef.lastModificationTime = System.currentTimeMillis();
        }
        return userBufferOffset;
    }

}
