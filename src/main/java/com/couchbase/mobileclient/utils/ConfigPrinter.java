package com.couchbase.mobileclient.utils;

import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.mobileclient.config.CouchbaseLiteProperties;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;

@Slf4j
public class ConfigPrinter {

    public void printSection(String sectionName) {
        printWelcome(sectionName);
    }
    public void printWelcome(String title) {
        log.info("-------------------------------------- {} -------------------",title);
    }

    public void printFooter(String footer) {
        log.info("-------------------------------------- {} -------------------", footer);
    }

    public void printSectionFooter(String sectionFooter) {
        printFooter(sectionFooter);
    }

    public void printReplicationConfig(CouchbaseLiteProperties.RemoteProperties remote) {
        log.info("{}",remote);
    }

    public void printDatabaseProperties(CouchbaseLiteProperties.LocalDBProperties local, DatabaseConfiguration cfg) {
        log.info("{}",local);
        log.info("\t\tencryption key: {}",local.isEncryptedDb()? cfg.getEncryptionKey(): "none"); //TODO integrate key as property
    }

    public void printLine() {
        log.info("-- ------------------------------------------------------------ --");
    }
}
