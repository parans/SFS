package com.simplefilesystem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by parans on 11/29/16.
 */
public class SimpleFileMetadataTable {

    AtomicInteger fileCount;

    Map<String, SimpleFile> fileMdMap;

    SimpleFileMetadataTable() {
        fileCount = new AtomicInteger();
        fileMdMap = new ConcurrentHashMap<>();
    }

    public int getFileCount() {

        return fileCount.get();
    }

    /**
     * We don't want this method to
     * @param key
     * @return
     */
    public SimpleFile getFile(final String key) {
        if(key == null) {
            return null;
        }
        return fileMdMap.get(key);
    }

    /**
     * We don't want this method to be throwing an exception, so doing a null check
     * @param fileName
     * @return
     */
    public boolean exists(final String fileName) {
        if(fileName == null) {
            return false;
        }
        return fileMdMap.containsKey(fileName);
    }

    /**
     * The semantics of the fileName and simpleFile is upto the application,
     * Only null validations are done here, all other types of validation should
     * be taken care by the application logic
     * @param fileName
     * @param simpleFile
     * @return
     */
    public int put(final String fileName, final SimpleFile simpleFile) {
        int status = -1;

        if(fileName == null) {
            return -2;
        }

        /**
         * Performing a comprehensive validation of the simple file should
         * be the responsibility of the application logic and not the responsibility
         * of this file map, therefore we perform only null checks
         */
        if(simpleFile == null) {
            return -2;
        }

        try {
            //If we were unable to put item
            fileMdMap.put(fileName, simpleFile);
            fileCount.incrementAndGet();
            status = 0;
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return status;
    }

    public int remove(final String fileName) {
        int status = 0;
        SimpleFile simpleFile = null;

        if(fileName == null) {
            return -2;
        }

        try {
            //If we were unable to put item
            simpleFile = fileMdMap.remove(fileName);
            if(simpleFile == null) {
                status = -1;
            }
            else {
                fileCount.decrementAndGet();
            }
        } catch(Exception e) {
            status = -1;
            System.out.println(e.getMessage());
        }
        return status;
    }

    public void clear() {
        fileMdMap.clear();
        fileCount.getAndSet(0);
    }
}
