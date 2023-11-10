package com.couchbase.mobileclient.utils;

import org.springframework.util.FileSystemUtils;

import java.io.File;

public class FileUtils {

    public static boolean deleteDirectory(File file) {
       return FileSystemUtils.deleteRecursively(file.getAbsoluteFile());
    }
}
