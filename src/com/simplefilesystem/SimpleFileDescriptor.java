package com.simplefilesystem;

/**
 * Created by parans on 12/1/16.
 */
public class SimpleFileDescriptor {
    SimpleFile simpleFileRef;
    int offset;
    int currentBlockId;

    public SimpleFileDescriptor(SimpleFile simpleFile, int offset, int currentBlockId) {
        this.simpleFileRef = simpleFile;
        this.offset = offset;
        this.currentBlockId = currentBlockId;
    }

    public boolean validateFileDescriptor() {
        if(simpleFileRef == null) {
            return false;
        }
        //TODO:Validate SimpleFile
        else if(offset < 0 || offset >= SimpleFileSystemConstants.BLOCK_SIZE) {
            return false;
        }
        else if (currentBlockId < SimpleFileSystemConstants.USER_SPACE_OFFSET
                || currentBlockId >= SimpleFileSystemConstants.DISK_BLOCKS){
            return false;
        }
        return true;
    }
}
