package com.simplefilesystem;

import java.util.Arrays;
import java.util.List;

/**
 * Created by parans on 11/29/16.
 */
public class SimpleFileAllocationTable {

    private int[] fileAllocationTable;

    public SimpleFileAllocationTable() {
        this.fileAllocationTable = new int[SimpleFileSystemConstants.FAT_TABLE_SIZE];
        Arrays.fill(fileAllocationTable, -1);
    }

    public boolean isLiesBetween(int val, int startVal, int endVal) {
        if(val >= startVal && val < endVal) {
            return true;
        }
        return false;
    }

    public boolean validateIndexes(final int[] indexes) {
        if(indexes == null) {
            return false;
        }
        if(indexes.length != SimpleFileSystemConstants.FAT_TABLE_SIZE) {
            return false;
        }

        for(int i=0; i<SimpleFileSystemConstants.USER_SPACE_OFFSET; i++) {
            if(indexes[i] >= SimpleFileSystemConstants.USER_SPACE_OFFSET || indexes[i] < -1) {
                return false;
            }
        }

        for(int i=SimpleFileSystemConstants.USER_SPACE_OFFSET; i<indexes.length; i++) {
            if(!isLiesBetween(indexes[i], SimpleFileSystemConstants.USER_SPACE_OFFSET, SimpleFileSystemConstants.FAT_TABLE_SIZE)) {
                if(indexes[i] == -1)
                {
                    continue;
                }
                else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * This method will be used only to allocate the entire table not parts of the table
     * @param allocationTable
     * @return 0, -1 C style return values
     */

    public int initFileAllocationTable(final int[] allocationTable) {
        if(!validateIndexes(allocationTable))
        {
            return -1;
        }

        for(int i=0; i<allocationTable.length; i++) {
            this.fileAllocationTable[i] = allocationTable[i];
        }
        return 0;
    }

    public int getValueAt(final int index) {
        int val = -1;
        if(!isLiesBetween(index, 0, SimpleFileSystemConstants.FAT_TABLE_SIZE)) {
            return val;
        }
        val = fileAllocationTable[index];
        return val;
    }

    public int resetValueAt(final int index) {
        if(!isLiesBetween(index, 0, SimpleFileSystemConstants.FAT_TABLE_SIZE)) {
            return -1;
        }
        fileAllocationTable[index] = -1;
        return 0;
    }

    //This should never be used, will be used by only test cases
    public int setValueAt(final int index, final int val) {
        if(!isLiesBetween(index, 0, SimpleFileSystemConstants.FAT_TABLE_SIZE)) {
            return -1;
        }

        if(!isLiesBetween(val, 0, SimpleFileSystemConstants.FAT_TABLE_SIZE)) {
            return -1;
        }

        fileAllocationTable[index] = val;
        return 0;
    }
}
