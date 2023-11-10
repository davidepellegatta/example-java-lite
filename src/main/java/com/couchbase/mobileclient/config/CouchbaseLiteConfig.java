package com.couchbase.mobileclient.config;

import com.couchbase.lite.*;
import com.couchbase.mobileclient.database.DBManager;
import com.couchbase.mobileclient.database.DBManager.DBManagerBuilder;
import com.couchbase.mobileclient.replicator.ReplicationListenersManager;
import com.couchbase.mobileclient.replicator.ReplicationListenersManager.ReplicationListenersManagerBuilder;
import com.couchbase.mobileclient.replicator.ReplicationManager;
import com.couchbase.mobileclient.replicator.ReplicationManager.ReplicationManagerBuilder;
import com.couchbase.mobileclient.utils.ConfigPrinter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Data
@EnableConfigurationProperties(CouchbaseLiteProperties.class)
@ConfigurationPropertiesScan
@Configuration
public class CouchbaseLiteConfig {

    static {
        CouchbaseLite.init();
    }

    final ConfigPrinter printer = new ConfigPrinter();
    final CouchbaseLiteProperties properties;

    public CouchbaseLiteConfig(CouchbaseLiteProperties properties) {
        this.properties = properties;
    }

    @Bean
    public ReplicationListenersManager listenerManager(Executor executor) {
        return new ReplicationListenersManagerBuilder(executor, properties.getRemote().getListeners()).build();
    }

    @Bean
    public Executor executor() {
        return Executors.newCachedThreadPool();
    }

    @Bean
    public DBManager dbManager() {
        return new DBManagerBuilder(this.properties.getLocal(), properties.getLog()).build();
    }

    @Bean
    public ReplicationManager replicatorManager(DBManager dbManager, ReplicationListenersManager listenerManager) throws CouchbaseLiteException {
        Set<Collection> collections = dbManager.getDatabase().getScope(properties.getLocal().getScope().getName()).getCollections();
        return new ReplicationManagerBuilder(this.properties.getRemote(), listenerManager, collections).build();
    }


}
