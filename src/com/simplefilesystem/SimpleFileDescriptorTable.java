package com.simplefilesystem;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by parans on 12/1/16.
 */
public class SimpleFileDescriptorTable {

    private final int MAX_FILE_DESCRIPTOR_COUNT = 32;
    private final long FREE_FILE_DESC_ALLOC_WAIT_TIME = 200L;
    private SimpleFileDescriptor[] fileDescriptorTable;
    private Queue<Integer> freeFileDescriptorList;

    private ReentrantLock freefiledesListMutex = new ReentrantLock(true);

    public SimpleFileDescriptorTable() {
        this.fileDescriptorTable = new SimpleFileDescriptor[MAX_FILE_DESCRIPTOR_COUNT];
        this.freeFileDescriptorList = new LinkedList<>();
        for(int i=0; i<MAX_FILE_DESCRIPTOR_COUNT; i++) {
            fileDescriptorTable[i] = null;
            freeFileDescriptorList.add(i);
        }
    }

    /**
     *
     * @return values -1 and file descriptors from 0-31
     */
    public int getFreeFileDescriptor() {
        int freeFileDescriptor = -1;
        try {
            //Try to get a lock on the list
            if(freefiledesListMutex.tryLock(FREE_FILE_DESC_ALLOC_WAIT_TIME, TimeUnit.MILLISECONDS)) {
                //now I was able to lock the data structure
                if(!freeFileDescriptorList.isEmpty()) {
                    freeFileDescriptor = freeFileDescriptorList.poll();
                }
                //If there are no free blocks do not wait just return
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        finally {
            freefiledesListMutex.unlock();
        }
        return freeFileDescriptor;
    }

    /**
     *
     * @param freeFileDescriptor
     * @return -2, -1 and 0
     */
    public int releaseFiledescriptor(int freeFileDescriptor) {
        int status = -1;
        if(freeFileDescriptor < 0 || freeFileDescriptor > MAX_FILE_DESCRIPTOR_COUNT) {
            return -2;
        }

        try {
            //Try to get a lock on the list
            if(freefiledesListMutex.tryLock(FREE_FILE_DESC_ALLOC_WAIT_TIME, TimeUnit.MILLISECONDS)) {
                if(fileDescriptorTable[freeFileDescriptor] != null) {
                    fileDescriptorTable[freeFileDescriptor] = null;
                    freeFileDescriptorList.offer(freeFileDescriptor);
                }
                status = 0;
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        finally {
            freefiledesListMutex.unlock();
        }
        return status;
    }

    /**
     *
     * @param simpleFileDescriptor
     * @return -2, -1 and fileDescriptor between 0-31
     */
    public int attachFileToFileDescriptor(SimpleFileDescriptor simpleFileDescriptor) {
        if(simpleFileDescriptor == null) {
            return -2;
        }

        int freeFileDescriptor = getFreeFileDescriptor();
        if(freeFileDescriptor < 0) {
            return -1;
        }
        fileDescriptorTable[freeFileDescriptor] = simpleFileDescriptor;
        return freeFileDescriptor;
    }

    public SimpleFileDescriptor getFileForFileDescriptor(int fileDescriptor) {
        if(fileDescriptor < 0 || fileDescriptor > MAX_FILE_DESCRIPTOR_COUNT-1) {
            return null;
        }
        return fileDescriptorTable[fileDescriptor];
    }

    public void resetValueAt(int fileDescriptor) {
        fileDescriptorTable[fileDescriptor] = null;
    }

}
