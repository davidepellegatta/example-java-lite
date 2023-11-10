package com.couchbase.mobileclient.utils;

import lombok.extern.log4j.Log4j2;
import lombok.val;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Log4j2
public class ZipUtils {

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public static void unzip(String zipPath, File destDir) {

            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
                byte[] buffer = new byte[1024];
                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    File newFile = newFile(destDir, zipEntry);
                    if (zipEntry.isDirectory()) {
                        if (!newFile.isDirectory() && !newFile.mkdirs()) {
                            throw new IOException("Failed to create directory " + newFile);
                        }
                    } else {
                        // fix for Windows-created archives
                        File parent = newFile.getParentFile();
                        if (!parent.isDirectory() && !parent.mkdirs()) {
                            throw new IOException("Failed to create directory " + parent);
                        }

                        // write file content
                        try(FileOutputStream fos = new FileOutputStream(newFile)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }
                    zipEntry = zis.getNextEntry();
                }
                zis.closeEntry();
            } catch (FileNotFoundException e) {
                log.error("Zip File {} not found!",zipPath, e);
            } catch (IOException e) {
                log.error("Exception unzipping {} file. ",zipPath, e);
            }
    }

    public static void unzip2(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if(!dir.exists()) dir.mkdirs();
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try (
            FileInputStream fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);)
            {
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                log.info("Unzipping to "+newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method zips the directory
     * @param dir
     * @param zipDirName
     */
    private void zipAll(File dir, String zipDirName) {
        try (FileOutputStream fos = new FileOutputStream(zipDirName);
             ZipOutputStream zos = new ZipOutputStream(fos)){
            List<String> filesListInDir = populateFilesList(dir);
            //now zip files one by one
            //create ZipOutputStream to write to the zip file

            for(String filePath : filesListInDir){
                log.info("Zipping "+filePath);
                //for ZipEntry we need to keep only relative file path, so we used substring on absolute path
                ZipEntry ze = new ZipEntry(filePath.substring(dir.getAbsolutePath().length()+1, filePath.length()));
                zos.putNextEntry(ze);
                //read the file and write to ZipOutputStream
                FileInputStream fis = new FileInputStream(filePath);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
                fis.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method populates all the files in a directory to a List
     * @param dir
     * @throws IOException
     */
    private List<String> populateFilesList(File dir) throws IOException {
        return populateFilesList(new ArrayList<>(), dir);
    }

    /**
     * This method populates all the files in a directory to a List
     * @param dir
     * @throws IOException
     */
    private List<String> populateFilesList(List<String> filesListInDir, File dir) {
        File[] files = dir.listFiles();
        for(File file : files){
            if(file.isFile()) filesListInDir.add(file.getAbsolutePath());
            else populateFilesList(filesListInDir, file);
        }
        return filesListInDir;
    }

/*    public static String unzipDatabase(String source, ConfigFiles pocConfiguration)
    {
        log.info("-------------------------------------- Begin Unzip Database -------------------")
        unzipAll(pocConfiguration, source);
        log.info("Unzip file: $source");
        log.info("-------------------------------------- End Unzip Database -------------------")
        return source;
    }

    static void unzipAll(ConfigFiles pocConfiguration, String zipFile) throws FileNotFoundException {
        unzip(new FileInputStream(zipFile), System.getProperty("user.dir")+"${File.separator}tmpdb${File.separator}${pocConfiguration.couchbaseCatalogueDatabase}.cblite2")
    }

    static void zipAll(String directory, String zipFile)
    {
        File sourceFile = new File(directory);
        ZipOutputStream zipOutputStrea = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
        zipOutputStrea.putNextEntry( it -> {
            zipFiles(it, sourceFile, "");
        });
    }


    static void zipFiles(ZipOutputStream zipOut, File sourceFile, String parentDirPath) throws IOException {
        ByteArray data = new ByteArray(2048);
        for (f :sourceFile.listFiles())
        {
            if (f.isDirectory)
            {
                val entry = new ZipEntry(f.name + File.separator);
                entry.time = f.lastModified();
                entry.isDirectory;
                entry.size = f.length();
                getLogger()?.info("Adding Directory: " + f.name);
                zipOut.putNextEntry(entry);
                //Call recursively to add files within this directory
                zipFiles(zipOut, f, f.name);
            }
            else
            {
                if (!f.name.contains(".zip"))
                { //If folder contains a file with extension ".zip", skip it
                    FileInputStream(f).use { fi ->
                        BufferedInputStream(fi).use { origin ->
                        val path = parentDirPath + File.separator + f.name
                    getLogger()?.info("Adding file: $path")
                    val entry = ZipEntry(path);
                    entry.time = f.lastModified();
                    entry.isDirectory
                    entry.size = f.length();
                    zipOut.putNextEntry(entry)
                    while (true) {
                        val readBytes = origin.read(data);
                        if (readBytes == -1) {
                            break
                        }
                        zipOut.write(data, 0, readBytes)
                    }
                }
                }
                }
                else
                {
                    zipOut.closeEntry();
                    zipOut.close();
                }
            }
        }
    }


    static void unzip(InputStream in, String destination) throws IOException {
        ByteArray buffer = new ByteArray(1024);
        ZipInputStream zis = new ZipInputStream(in);
        ZipEntry ze = zis.getNextEntry();
        while (ze != null)
        {
            String fileName = ze.getName();
            File newFile = new File(destination, fileName);
            if (ze.isDirectory())
            {
                newFile.mkdirs();
            }
            else
            {
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                Integer len;
                while (zis.read(buffer).also { len = it } > 0)
                {
                    fos.write(buffer, 0, len)
                }
                fos.close();
            }
            ze = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
        in.close();
    }

 */

}
