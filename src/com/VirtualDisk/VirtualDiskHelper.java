package com.VirtualDisk;

import com.simplefilesystem.SimpleFileSystemConstants;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by parans on 12/21/16.
 * This class will be a helper and will not maintain state
 */
public class VirtualDiskHelper {
    //this data structure is misplaced, this has to be moved into fs management module
    private static Map<String, VirtualDisk> virtualDiskMap = new HashMap<String, VirtualDisk>();

    /**
     * This method will create a file and initialize the file to zeroes.
     * This method will not overwrite the file if it already exists.
     *
     * @param diskName
     * @return VirtualDisk
     */
    public static VirtualDisk makeVirtualDisk(final String diskName)
    {
        VirtualDisk vdisk = null;
        byte[] buf = new byte[SimpleFileSystemConstants.BLOCK_SIZE];

        //You should probably validate disk name

        if (diskName == null) {
            System.out.println("makeVirtualdisk: invalid file name\n");
            return null;
        }

        File file = new File(diskName);
        if(file == null) {
            System.out.println("Error creating disk\n");
            return null;
        }

        try {
            if(file.exists()) {
                System.out.println("Error creating disk, choose a different name\n");
                return null;
            }

            if(!file.createNewFile()) {
                System.out.println("Error creating disk\n");
                return null;
            }

            FileOutputStream os = new FileOutputStream(file);
            Arrays.fill(buf, (byte)0);
            for (int cnt = 0; cnt < SimpleFileSystemConstants.DISK_BLOCKS; ++cnt)
                os.write(buf);
            os.close();

            vdisk = new VirtualDisk(file, diskName);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return vdisk;
    }

    /**
     * Seems like this function is not useful in java
     * @param virtualDisk
     * @return
     */
    static boolean openVirtualDisk(final VirtualDisk virtualDisk)
    {
        //disk name null check
        if(virtualDisk == null) {
            System.out.println("Virtual disk handle is null\n");
            return false;
        }
        String diskName = virtualDisk.vDiskName;
        if (diskName == null) {
            System.out.println("open_disk: invalid file name\n");
            return false;
        }

        File file = virtualDisk.file;
        if(file == null) {
            System.out.println("File initialization failure\n");
            return false;
        }

        if(!file.exists()) {
            System.out.println("File does not exist cannot open\n");
            return false;
        }

        if(!file.canRead()) {
            if(!file.setReadable(true)) {
                System.out.println("open_disk: Cannot set file as readble, open failed\n");
                return false;
            }
        }

        if(!file.canWrite()) {
            if(!file.setWritable(true)) {
                System.out.println("open_disk: Cannot set file as writable, open failed\n");
                return false;
            }
        }
        return true;
    }

    /**
     * Seems like this function is quite useless in java
     * @param virtualDisk
     * @return
     */
    public static boolean closeVirtualDisk(final VirtualDisk virtualDisk)
    {
        if(virtualDisk == null) {
            System.out.println("Virtual disk handle is null\n");
            return false;
        }
        String diskName = virtualDisk.vDiskName;
        //disk name null check
        if (diskName == null) {
            System.out.println("Invalid file name\n");
            return false;
        }

        File file = virtualDisk.file;
        if(file == null) {
            System.out.println("File initialization failure\n");
            return false;
        }

        if(!file.exists()) {
            System.out.println("File does not exist cannot close\n");
            return false;
        }

        if(!file.canRead() && !file.canWrite()) {
            System.out.println("File is already closed\n");
            return false;
        }

        if(file.canRead()) {
            if(!file.setReadable(true)) {
                System.out.println("Cannot close file, close failed");
                return false;
            }
        }

        if(file.canWrite()) {
            if(!file.setWritable(true)) {
                System.out.println("Cannot close file, close failed");
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param virtualDisk
     * @param block
     * @param buffer
     * @return 0 on success and -2 for validation failures and -1 for exceptions
     */
    public static int blockRead(final VirtualDisk virtualDisk, int block, byte[] buffer)
    {
        int success = 0;
        if(virtualDisk == null) {
            System.out.println("Virtual disk handle is null\n");
            return -2;
        }
        String diskName = virtualDisk.vDiskName;

        //disk name null check
        if (diskName == null) {
            System.out.println("Invalid file name");
            return -2;
        }

        File file = virtualDisk.file;
        if(file == null) {
            System.out.println("File initialization failure");
            return -2;
        }

        if(!file.exists()) {
            System.out.println("File does not exist cannot read\n");
            return -2;
        }

        if(!file.canRead()) {
            System.out.println("Cannot read file, read failed");
            return -2;
        }

        if ((block < 0) || (block >= SimpleFileSystemConstants.DISK_BLOCKS)) {
            System.out.println("block_read: block index out of bounds");
            return -2;
        }

        if(buffer == null) {
            System.out.println("block_read: buffer is null");
            return -2;
        }

        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(block * SimpleFileSystemConstants.BLOCK_SIZE);
            if(raf.read(buffer, 0, SimpleFileSystemConstants.BLOCK_SIZE) < 0) {
                System.out.println("Failed to read");
                return -1;
            }
        }
        catch(FileNotFoundException e) {
            System.out.println(e.getMessage());
            return -1;
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
            return -1;
        }

        return success;
    }

    /**
     *
     * @param virtualDisk
     * @param block
     * @param buffer
     * @return -2 for validation exceptions, -1 on error and 0 on success
     */
    public static int blockWrite(final VirtualDisk virtualDisk, int block, byte[] buffer)
    {
        int success = 0;

        if(virtualDisk == null) {
            System.out.println("Virtual disk handle is null\n");
            return -2;
        }
        String diskName = virtualDisk.vDiskName;

        //disk name null check
        if (diskName == null) {
            System.out.println("Invalid file name");
            return -2;
        }

        File file = virtualDisk.file;
        if(file == null) {
            System.out.println("File initialization failure");
            return -2;
        }

        if(!file.exists()) {
            System.out.println("File does not exist cannot write\n");
            return -2;
        }

        if(!file.canWrite()) {
            System.out.println("Cannot write file, write failed");
            return -2;
        }

        if ((block < 0) || (block >= SimpleFileSystemConstants.DISK_BLOCKS)) {
            System.out.println("block_read: block index out of bounds");
            return -2;
        }

        if(buffer == null || buffer.length == 0) {
            System.out.println("block_read: Nothing to write");
            return -2;
        }

        try {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(block * SimpleFileSystemConstants.BLOCK_SIZE);
            raf.write(buffer, 0, buffer.length);
        }
        catch(FileNotFoundException e) {
            System.out.println(e.getMessage());
            return -1;
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
            return -1;
        }

        return success;
    }

    public static boolean destroyVirtualDisk(final VirtualDisk virtualDisk) {
        if(virtualDisk == null) {
            System.out.println("Virtual disk handle is null\n");
            return false;
        }
        String diskName = virtualDisk.vDiskName;
        //disk name null check
        if (diskName == null) {
            System.out.println("Invalid file name");
            return false;
        }

        File file = virtualDisk.file;
        if(file == null) {
            System.out.println("File initialization failure");
            return false;
        }

        if(!file.exists()) {
            System.out.println("File does not exist");
            return false;
        }

        if(!file.delete()) {
            System.out.println("File deletion failed");
            return false;
        }
        return true;
    }
}
