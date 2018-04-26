package com.VirtualDisk;

import java.io.File;

/**
 * Created by parans on 12/21/16.
 */
public class VirtualDisk {
    final File file;
    final String vDiskName;

    public VirtualDisk(File file, String vDiskName) {
        this.file = file;
        this.vDiskName = vDiskName;
    }

    public File getFileHandle() {
        return file;
    }
 }
