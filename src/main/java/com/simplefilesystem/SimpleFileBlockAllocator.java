package com.simplefilesystem;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by parans on 12/1/16.
 */
public class SimpleFileBlockAllocator {

    public static final long FREE_BLOCK_ALLOCATION_WAIT_TIME = 300;

    ReentrantLock freeBlocksListMutex = new ReentrantLock(true);
    //This is a way to avoid duplicates
    private boolean[] blockAvailabilityMatrix;
    //Reserved system blocks should never appear in the free block list
    private Queue<Integer> freeBlocksList;

    public SimpleFileBlockAllocator() {
        freeBlocksList = new LinkedList<>();
        blockAvailabilityMatrix = new boolean[SimpleFileSystemConstants.FAT_TABLE_SIZE];
    }

    public boolean validateFreeBlockIndexList(List<Integer> fbil) {
        if(fbil == null) {
            return false;
        }
        //Cannot have more free blocks than user space blocks
        if(fbil.size() > SimpleFileSystemConstants.USER_SPACE_OFFSET) {
            return false;
        }

        for(int freeBlockId : fbil) {
            if(freeBlockId < SimpleFileSystemConstants.USER_SPACE_OFFSET ||
                    freeBlockId >= SimpleFileSystemConstants.FAT_TABLE_SIZE) {
                return false;
            }
        }
        return true;
    }

    /**
     * This function is written to support lower granularity while releasing free blocks
     * @param freeBlockIndexList
     * @return -1 when failed to deposit free block because we are unable to acquire lock or we are interrupted,
     * -2 when we get a validation exception
     */
    public int depositFreeBlocks(List<Integer> freeBlockIndexList) {
        int status = -1;
        if(!validateFreeBlockIndexList(freeBlockIndexList)) {
            return -2;
        }

        try {
            if(freeBlocksListMutex.tryLock(FREE_BLOCK_ALLOCATION_WAIT_TIME, TimeUnit.MILLISECONDS)) {
                for(int blockId : freeBlockIndexList) {
                    if(!blockAvailabilityMatrix[blockId]) {
                        freeBlocksList.offer(blockId);
                        blockAvailabilityMatrix[blockId] = true;
                    }
                }
                //Status is returned as 0 even if we are not able to offer
                // a block which is already in the list, it does not manifest as an error
                status = 0;
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        finally {
            freeBlocksListMutex.unlock();
        }
        return status;
    }

    /**
     * Maintain as a list only in memory, on disk FAT is the representation
     * @return -1 on failure
     */
    public int getFreeBlockIndex() {
        int freeBlockIndex = -1;
        try {
            //Try to get a lock on the list
            if(freeBlocksListMutex.tryLock(FREE_BLOCK_ALLOCATION_WAIT_TIME, TimeUnit.MILLISECONDS)) {
                //now I was able to lock the data structure
                if(!freeBlocksList.isEmpty()) {
                    freeBlockIndex = freeBlocksList.poll();
                    blockAvailabilityMatrix[freeBlockIndex] = false;
                }
                //If there are no free blocks do not wait just return
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        finally {
            freeBlocksListMutex.unlock();
        }
        return freeBlockIndex;
    }

    public int releaseFreeBlock(int freeBlockIndex) {
        int status = -1;
        if(freeBlockIndex < SimpleFileSystemConstants.USER_SPACE_OFFSET ||
                freeBlockIndex >= SimpleFileSystemConstants.FAT_TABLE_SIZE) {
            return -2;
        }

        try {
            //Try to get a lock on the list
            if(freeBlocksListMutex.tryLock(FREE_BLOCK_ALLOCATION_WAIT_TIME, TimeUnit.MILLISECONDS)) {
                if(!blockAvailabilityMatrix[freeBlockIndex]) {
                    freeBlocksList.offer(freeBlockIndex);
                    blockAvailabilityMatrix[freeBlockIndex] = true;
                }
                //Don't return as error even if we are not freeing an already freed block
                status = 0;
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        finally {
            freeBlocksListMutex.unlock();
        }
        return status;
    }
}