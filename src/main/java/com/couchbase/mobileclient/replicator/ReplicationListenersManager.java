package com.couchbase.mobileclient.replicator;

import com.couchbase.lite.DocumentReplicationListener;
import com.couchbase.lite.ListenerToken;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorChangeListener;
import com.couchbase.mobileclient.listeners.AutoPurgeDocumentChangeListener;
import com.couchbase.mobileclient.listeners.StatusChangeListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.couchbase.mobileclient.config.CouchbaseLiteProperties.*;

@Slf4j
@Data
public class ReplicationListenersManager {

    final Executor executor;
    final List<DocumentReplicationListener> documentListeners = new ArrayList<>();
    final List<ReplicatorChangeListener> changeListeners = new ArrayList<>();
    final List<ListenerToken> listenerTokens = new ArrayList<>();

    public ReplicationListenersManager() {
        this(Executors.newCachedThreadPool());
    }

    public ReplicationListenersManager(Executor executor) {
        this(executor, List.of(), List.of());
    }

    public ReplicationListenersManager(Executor executor, List<DocumentReplicationListener> documentListeners, List<ReplicatorChangeListener> changeListeners/*, List<ListenerToken> listenerTokens*/) {
        this.executor = executor;
        this.documentListeners.addAll(documentListeners);
        this.changeListeners.addAll(changeListeners);
    }

    public void removeListenerTokens() {
        listenerTokens.forEach(ListenerToken::remove);
    }


    public void addStatusListenerCallback(Runnable onExit) {
        ((StatusChangeListener)changeListeners.get(0)).setOnExitCallback(onExit); //TODO redesign callback hook
    }


    public void setupListeners(Replicator replicator) {
        //TODO reset listeners
        if(replicator==null) { return ; }
        this.documentListeners.forEach(listener -> this.listenerTokens.add(replicator.addDocumentReplicationListener(this.executor, listener)));
        this.changeListeners.forEach(listener -> this.listenerTokens.add(replicator.addChangeListener(listener)));
    }

    public static class ReplicationListenersManagerBuilder {
        final List<DocumentReplicationListener> documentListeners = List.of(new AutoPurgeDocumentChangeListener());
        final List<ReplicatorChangeListener> changeListeners = List.of(new StatusChangeListener());
        final List<ListenerToken> listenerTokens = new ArrayList<>();
        final Map<String, ListenerProperties> properties = new HashMap<>();
        final Executor executor;

        public ReplicationListenersManagerBuilder(Executor executor, Map<String, ListenerProperties> properties) {
            this.executor = executor;
            this.properties.putAll(properties);
        }

        private void setupListeners(Replicator replicator) {
            //TODO build listeners from properties
            if(replicator==null) { return ; }
            documentListeners.forEach(listener -> listenerTokens.add(replicator.addDocumentReplicationListener(executor, listener)));
            changeListeners.forEach(listener -> listenerTokens.add(replicator.addChangeListener(listener)));
        }

        public ReplicationListenersManager build() {
            //TODO build listeners automatically from properties
            return new ReplicationListenersManager(executor, documentListeners, changeListeners);
        }
    }
}
