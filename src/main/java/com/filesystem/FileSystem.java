package com.filesystem;

/**
 * Created by parans on 11/29/16.
 */
public interface FileSystem {

    int fsOpen(String fileName);

    int fsClose(int fileDescriptor);

    int fsCreate(String fileName);

    int fsDelete(String fileName);

    int fsRead(int fileDescriptor, byte[] buffer, int size);

    int fsWrite(int fileDescriptor, byte[] buffer, int size);

    int fsFileSize(int filedescriptor);

    int fslSeek(int fileDescriptor, int offset);

    int fs_truncate(int fileDescriptor, int length);

}
