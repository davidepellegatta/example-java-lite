package com.couchbase.mobileclient.utils;

import com.couchbase.lite.*;
import com.couchbase.mobileclient.config.CouchbaseLiteProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class DBUtils {
/*
    final ConfigPrinter printer = new ConfigPrinter();


    public void setupDatabaseLogs(CouchbaseLiteProperties.LogProperties properties) {
        File logPath = new File(properties.getPath());
        if (!logPath.exists()) {
            if(!logPath.mkdirs()) {
                log.error("Log path: {} folder cannot be created", logPath);
            }
        }
        LogFileConfiguration logCfg = new LogFileConfiguration(logPath.getAbsolutePath());
        logCfg.setMaxSize(properties.getMaxSize());
        logCfg.setMaxRotateCount(properties.getRotationCount());
        logCfg.setUsePlaintext(properties.isPlainText());
        Database.log.getFile().setConfig(logCfg);
        Database.log.getFile().setLevel(properties.getLevel());
    }

    public void copyDatabase(CouchbaseLiteProperties.LocalDBProperties properties, DatabaseConfiguration cfg) throws CouchbaseLiteException {
        printer.printSection("Begin Copy Database");
        File srcDir = new File(properties.getDownloadPath());
        File srcDb = properties.getUnzippedDbFolderFile();
        File srcDbZip = properties.getZippedDbFile();
        File dstDb = properties.getDbFolderFile();

        if (dstDb.exists()) {
            log.warn(" -------------> Target database {} already exist!!!", dstDb.getAbsolutePath());
        } else {
            // Unzip database
            if(srcDbZip.exists()) {
                log.info("download path: {}", srcDbZip.getAbsolutePath());
                log.info("unzipped path: {}", srcDb.getAbsolutePath());
                ZipUtils.unzip(srcDbZip.getAbsolutePath(), srcDir);
            }else {
                log.warn("Skipping unzipping download database, source zip file not found at {}",srcDbZip.getAbsolutePath());
            }

            if(srcDb.exists()) {
                log.info("Coping database: {}",srcDb);
                log.info("... to database: {}",cfg.getDirectory());
                log.info(" encrypted with: {}", cfg.getEncryptionKey());
                Database.copy(srcDb, properties.getDatabase(), cfg);
            } else {
                log.warn("Skipping copy database, source database not found at {}",srcDb.getAbsolutePath());
            }
        }
        log.info("Target Database size: {} bytes",properties.getSQLLiteDBFile().length());
        printer.printSectionFooter("End Copy Database");
    }

    public void flushPreviousDB(CouchbaseLiteProperties.LocalDBProperties properties) {
        log.info("deleting '{}' folder... {}",properties.getDbFolderFile().getAbsolutePath(), FileUtils.deleteDirectory(properties.getDbFolderFile().getAbsoluteFile()) ? "OK": "FAILED");
        log.info("deleting '{}' folder... {}",properties.getUnzippedDbFolderFile().getAbsolutePath(), FileUtils.deleteDirectory(properties.getUnzippedDbFolderFile()) ? "OK": "FAILED");
    }

    public void prepareDatabase(CouchbaseLiteProperties.LocalDBProperties properties, DatabaseConfiguration cfg, CouchbaseLiteProperties.LogProperties logProperties) throws CouchbaseLiteException {
        if (properties.isFlushPreviousDb())
            flushPreviousDB(properties);
        // Copy database & Download from ftp
        if (properties.isCopyDb()) {
            // SFTPUtils.download(configFiles);
            // copy database for using new UUID
            copyDatabase(properties, cfg);
        }
        setupDatabaseLogs(logProperties);
    }

    public void setupCollections(CouchbaseLiteProperties.LocalDBProperties properties, Database database) {
        CouchbaseLiteProperties.ScopeProperties scope = properties.getScope();
        scope.getCollections().forEach(collection -> {
            try {
                database.createCollection(collection, scope.getName());
            } catch (CouchbaseLiteException e) {
                log.error("{} creating collection {}.{}", e.getClass().getSimpleName(), scope.getName(), collection);
                throw new RuntimeException(e);
            }
        });
    }

*/

}
