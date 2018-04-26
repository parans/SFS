package com.simplefilesystem;

/**
 * Created by parans on 11/29/16.
 */
public class SimpleFile {
    String fileName;
    int startBlockId;
    volatile int finalBlockId;
    int fileSize;
    long creationTime;
    long lastModificationTime;
    volatile int endOfFileIndex;
    //Although this field does not belong here, it is easier for validation so this filed will in memory but will not be serialized
    int fileDescriptorCount;


    SimpleFile(String fileName, int startBlockId, int finalBlockId, int fileSize, long creationTime,
               long modificationTime, int fileDescriptorCount) {
        this.fileName = fileName;
        this.startBlockId = startBlockId;
        this.finalBlockId = finalBlockId;
        this.fileSize = fileSize;
        this.creationTime = creationTime;
        this.lastModificationTime = modificationTime;
        this.fileDescriptorCount = fileDescriptorCount;
    }


}
