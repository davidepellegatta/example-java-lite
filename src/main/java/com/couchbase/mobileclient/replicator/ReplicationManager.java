package com.couchbase.mobileclient.replicator;

import com.couchbase.lite.*;
import com.couchbase.lite.Collection;
import com.couchbase.mobileclient.config.CouchbaseLiteProperties;
import com.couchbase.mobileclient.config.CouchbaseLiteProperties.AuthenticatorProperties;
import com.couchbase.mobileclient.config.CouchbaseLiteProperties.RemoteProperties;
import com.couchbase.mobileclient.retry.RetryStrategy;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;

import static java.lang.String.join;
import static java.util.stream.Collectors.toMap;


@Data
@Slf4j
public class ReplicationManager implements AutoCloseable {
    final RemoteProperties properties;
    final Replicator replicator;
    final RetryStrategy retry; //TODO implement RetryStrategy
    final ReplicationListenersManager listenerManager;

    public ReplicationManager(RemoteProperties properties, Replicator replicator, RetryStrategy retry, ReplicationListenersManager listenerManager) {
        this.properties = properties;
        this.replicator = replicator;
        this.retry = retry;
        this.listenerManager = listenerManager;
    }

    @PreDestroy
    public void close() {
        listenerManager.removeListenerTokens();
        replicator.stop();
        replicator.close();
    }

    public void start() throws CouchbaseLiteException {
        // asynchronously
        listenerManager.setupListeners(replicator); //TODO reset listeners
        replicator.start(properties.isResetCheckpoint());
        log.info("Start synchronisation({})...",properties.isResetCheckpoint());
    }

    public void stop() {
        listenerManager.removeListenerTokens();
        replicator.stop();
    }


    public static class ReplicationManagerBuilder {
        final RemoteProperties properties;

        final ReplicationListenersManager listener;

        final ReplicatorBuilder replicatorBuilder;


        public ReplicationManagerBuilder(RemoteProperties properties, ReplicationListenersManager listener, Set<Collection> collections) {
            this.properties = properties;
            this.listener = listener;
            this.replicatorBuilder = new ReplicatorBuilder(properties, collections);
        }


        public ReplicationManager build() {
            Replicator replicator = replicatorBuilder.build();
          //  setupListeners(replicator, executor);
            return new ReplicationManager(this.properties, replicator, null, listener);
        }
    }

    public static class ReplicatorBuilder {
        final RemoteProperties properties;
        final Map<String, Collection> collections = new HashMap<>();

        public ReplicatorBuilder(RemoteProperties properties, Set<Collection> collections) {
            this.properties = properties;
            this.collections.putAll(collections.stream().collect(toMap(Collection::getName, Function.identity() )));
        }

        private Map<Collection, CollectionConfiguration> collectionsConfiguration() {
            Map<String, CouchbaseLiteProperties.CollectionProperties> collectionsConfigs = properties.getCollections();
            Map<Collection, CollectionConfiguration> collectionsMap = new HashMap<>();
            log.info("Setting listeners for collections: {}",join(",",collections.keySet()));
            collectionsConfigs.forEach( (name, p) -> {
                Collection collection = collections.get(name);
                if(collection != null) {
                    CollectionConfiguration collectionCfg = new CollectionConfiguration();
                    if (p.getChannelsFilter() != null && !p.getChannelsFilter().isEmpty()) {
                        collectionCfg.setChannels(p.getChannelsFilter());
                    }
                    if (p.getDocumentIDsFilter() != null && !p.getDocumentIDsFilter().isEmpty()) {
                        collectionCfg.setDocumentIDs(p.getDocumentIDsFilter());
                    }
                    collectionsMap.put(collection, collectionCfg);
                }else {
                    log.warn(" - CollectionReplication {} not found in local database configuration properties", name);
                }
            });
            return collectionsMap; //modify this line if you want to add specific collections configuration
        }

        private ReplicatorConfiguration replicatorConfiguration() {

            Map<Collection, CollectionConfiguration> collectionsCfg = collectionsConfiguration();
            Endpoint endpoint = properties.getEndpoint();
            ReplicatorConfiguration replConfig = new ReplicatorConfiguration(endpoint);

            replConfig.setAuthenticator(new AuthenticatorBuilder(this.properties.getAuthenticator()).build());   //TODO decide if it would be built or provided
            replConfig.setType(properties.getReplicatorType());
            replConfig.setContinuous(properties.isContinuous());
            collectionsCfg.forEach(replConfig::addCollection); // migration from deprecated method replConfig.setChannels(properties.getChannels()); to CollectionConfiguration
            return replConfig;
        }

        public Replicator build() {
            return new Replicator(replicatorConfiguration());
        }
    }

    public static class AuthenticatorBuilder {
        final AuthenticatorProperties properties;
        public AuthenticatorBuilder(AuthenticatorProperties authenticator) {
            this.properties = authenticator;
        }

        public Authenticator build() {
            return new BasicAuthenticator(properties.getUsername(), properties.getPassword().toCharArray());
        }
    }
}
