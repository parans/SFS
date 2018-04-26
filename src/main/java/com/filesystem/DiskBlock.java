package com.filesystem;

/**
 * Created by parans on 12/1/16.
 */
public class DiskBlock {
    int blockId;
    public static final int DISK_BLOCK_SIZE = 4096;

    public DiskBlock(int blockId) {
        if(blockId < 0) {
            //throw new Exception("BlockId cannot be negative");
        }
    }
}
