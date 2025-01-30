package com.couchbase.mobileclient.database;

import com.couchbase.lite.*;
import com.couchbase.lite.Collection;
import com.couchbase.mobileclient.config.CouchbaseLiteProperties;
import com.couchbase.mobileclient.listeners.CounterCollectionChangeListener;
import com.couchbase.mobileclient.utils.DBUtils;
import com.couchbase.mobileclient.utils.FileUtils;
import com.couchbase.mobileclient.utils.ZipUtils;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.time.Instant;
import java.util.*;

import static java.lang.String.format;

@Slf4j
@Data
public class DBManager {
    final DBUtils dbUtils = new DBUtils();
    final CouchbaseLiteProperties.LocalDBProperties properties;
    final CouchbaseLiteProperties.LogProperties logProperties;

    final Database database;
    final List<ListenerToken> listenerTokens = new ArrayList<>();

    public DBManager(CouchbaseLiteProperties.LocalDBProperties properties, CouchbaseLiteProperties.LogProperties logProperties, Database database, List<ListenerToken> tokens) {
        this.properties = properties;
        this.logProperties = logProperties;
        this.database = database;
        this.listenerTokens.addAll(tokens);
    }

    /**
     * List all documents in wundermart.monitorings
     * @return
     * @throws CouchbaseLiteException
     */
    public List<Result> listAllMonitorings() throws CouchbaseLiteException {
        String queryStmt = "SELECT posName total FROM wundermart.monitorings";
        return  database.createQuery(queryStmt).execute().allResults();
    }

    public Long countMonitorings() throws CouchbaseLiteException {
        String queryStmt = "SELECT count(*) total FROM wundermart.monitorings";
        return  database.createQuery(queryStmt).execute().allResults().get(0).getLong("total");
    }

    public void saveAMonitoring() throws CouchbaseLiteException {

        String stringTime = String.valueOf(Instant.now().getEpochSecond());
        String uniqueId = String.format("unique-id-%s", stringTime);
        MutableDocument mutableDoc = new MutableDocument(uniqueId);

        Random random = new Random();
        int randomNumber = random.nextInt(100) + 1; // Generates a number between 1 and 100

        mutableDoc.setString("pos_uuid","52286ee0-45a1-4195-a91c-d72e3c0b1146");
        //mutableDoc.setString("pos_uuid","b87a007c-0302-4e02-8186-5f1dfb01ca8c");
        mutableDoc.setInt("metric1",randomNumber);
        mutableDoc.setString("datetime",stringTime);

        database.getScope("wundermart").getCollection("monitorings").save(mutableDoc);
    }

    public Database getDb() {
        return database;
    }



    private void removeListeners() {
        this.listenerTokens.forEach(ListenerToken::remove);
    }

    @PreDestroy
    public void close() {
        log.info("closing DBmanager...");
        try {
            compactDb();
            removeListeners();
            database.close();
        } catch (CouchbaseLiteException e) {
            log.error("Exception closing database", e);
            throw new RuntimeException(e);
        }
    }

    void purgeAll(String collectionName, List<String> ids) {
        //TODO Reactive Async
        ids.forEach(it -> {
            try {
                Objects.requireNonNull(database.getCollection(collectionName)).purge(it);
            } catch (CouchbaseLiteException e) {
                log.error("Exception purging {} doc. ",it,e);
            }
        });
    }


    private void compactDb() throws CouchbaseLiteException {
        log.info("Compacting DB in progress... {}",database.performMaintenance(MaintenanceType.COMPACT)? "DONE": "SKIPPED");
    }


    @Data
    public static class DBManagerBuilder{
        CouchbaseLiteProperties.LocalDBProperties dbProperties;
        CouchbaseLiteProperties.LogProperties logProperties;

        List<CollectionChangeListener> listeners = List.of(new CounterCollectionChangeListener());
        List<ListenerToken> changeListenerTokens = new ArrayList<>();
        DatabaseConfiguration cfg;
        Database database;

        public DBManagerBuilder(CouchbaseLiteProperties.LocalDBProperties dbProperties, CouchbaseLiteProperties.LogProperties logProperties) {
            this.dbProperties = dbProperties;
            this.logProperties = logProperties;
        }


        protected void setup(){
            this.cfg =  databaseConfiguration();
            try {
                prepareDatabase();
                this.database = new Database(dbProperties.getDatabase(), cfg);
                setupCollections();
            } catch (CouchbaseLiteException e ) {
                log.error("Exception instantiating database", e);
            }
        }

        private DatabaseConfiguration databaseConfiguration() {
            DatabaseConfiguration cfg = new DatabaseConfiguration();
            cfg.setDirectory(dbProperties.getDbPath()); //System.getProperty("user.dir"));
            if(dbProperties.isEncryptedDb())
                cfg.setEncryptionKey(new EncryptionKey(dbProperties.getEncryptionKey()));
            return cfg;
        }

        private void setupDatabaseLogs() {
            File logPath = new File(logProperties.getPath());
            if (!logPath.exists()) {
                if(!logPath.mkdirs()) {
                    log.error("Log path: {} folder cannot be created", logPath);
                }
            }
            LogFileConfiguration logCfg = new LogFileConfiguration(logPath.getAbsolutePath());
            logCfg.setMaxSize(logProperties.getMaxSize());
            logCfg.setMaxRotateCount(logProperties.getRotationCount());
            logCfg.setUsePlaintext(logProperties.isPlainText());
            Database.log.getFile().setConfig(logCfg);
            Database.log.getFile().setLevel(logProperties.getLevel());
        }

        private void copyDatabase() throws CouchbaseLiteException {

            File srcDir = new File(dbProperties.getDownloadPath());
            File srcDb = dbProperties.getUnzippedDbFolderFile();
            File srcDbZip = dbProperties.getZippedDbFile();
            File dstDb = dbProperties.getDbFolderFile();

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
                    Database.copy(srcDb, dbProperties.getDatabase(), cfg);
                } else {
                    log.warn("Skipping copy database, source database not found at {}",srcDb.getAbsolutePath());
                }
            }
            log.info("Target Database size: {} bytes",dbProperties.getSQLLiteDBFile().length());

        }

        private void flushPreviousDB() {
            log.info("deleting '{}' folder... {}",dbProperties.getDbFolderFile().getAbsolutePath(), FileUtils.deleteDirectory(dbProperties.getDbFolderFile().getAbsoluteFile()) ? "OK": "FAILED");
            log.info("deleting '{}' folder... {}",dbProperties.getUnzippedDbFolderFile().getAbsolutePath(), FileUtils.deleteDirectory(dbProperties.getUnzippedDbFolderFile()) ? "OK": "FAILED");
        }

        private void prepareDatabase() throws CouchbaseLiteException {
            if (dbProperties.isFlushPreviousDb())
                flushPreviousDB();
            // Copy database & Download from ftp
            if (dbProperties.isCopyDb()) {
                // SFTPUtils.download(configFiles);
                // copy database for using new UUID
                copyDatabase();
            }
            setupDatabaseLogs();
        }

        private void setupCollections() {
            changeListenerTokens.clear();
            log.info("Setting local '{}' database's collections",dbProperties.getDatabase());
            CouchbaseLiteProperties.ScopeProperties scope = dbProperties.getScope();
            scope.getCollections().forEach(collection -> {
                try {
                    log.info(" - creating {}.{} collection", scope.getName(), collection);
                    Collection col = database.createCollection(collection, scope.getName());
                    //TODO setup individual collections listeners
                    setupListeners(col);
                } catch (CouchbaseLiteException e) {
                    log.error("{} creating collection {}.{}", e.getClass().getSimpleName(), scope.getName(), collection);
                    throw new RuntimeException(e);
                }
            });
        }


        public void setupListeners(Collection collection) throws CouchbaseLiteException {
            //TODO reset listeners
            for (CollectionChangeListener listener : listeners) {
                changeListenerTokens.add(collection.addChangeListener(listener));
            }
        }


        public DBManager build() {
            setup();
            return new DBManager(this.dbProperties, this.logProperties, this.database, this.changeListenerTokens); //TODO remove unnecessary log properties
        }
    }
}
