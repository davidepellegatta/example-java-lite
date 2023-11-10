package com.couchbase.mobileclient.listeners;

import com.couchbase.lite.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.couchbase.lite.internal.utils.StringUtils.join;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Slf4j
public class AutoPurgeDocumentChangeListener implements DocumentReplicationListener {

    final Map<String, Collection> collections = new HashMap<>();

    public Map<String, Collection> getCollectionsMap(DocumentReplication replication) {
        if(collections.isEmpty()) {
            collections.putAll(replication.getReplicator().getConfig().getCollections()
                    .stream().collect(Collectors.toMap(k -> k.getScope().getName()+"."+k.getName(), Function.identity())));
        }
        return collections;
    }

     void purgeAll(Collection collection, List<String> ids) {
        //TODO Reactive Async
        ids.forEach(it -> {
            try {
                collection.purge(it);
            } catch (CouchbaseLiteException e) {
                log.error("Exception purging {} doc. ",it,e);
            }
        });
    }

    @Override
    public void replication(DocumentReplication replication) {
        if(!replication.isPush()) {
            log.info("Checking replicated deleted docs... ");
            final Map<String, Collection> collectionMap = getCollectionsMap(replication);
            Map<Collection, List<String>> ids= replication.getDocuments().stream().filter(x -> x.getFlags().stream().anyMatch(Predicate.isEqual(DocumentFlag.DELETED).or(Predicate.isEqual(DocumentFlag.ACCESS_REMOVED))))
                    .peek(d -> log.info(" - Removed document: {}",format("{id: \"%s\" , flags: [%s] }", d.getID(), join(",", d.getFlags()))))
                    .collect(Collectors.groupingBy(k -> collectionMap.get(k.getCollectionScope()+"."+k.getCollectionName()), Collectors.mapping(ReplicatedDocument::getID, toList())));

            log.info("Checking completed");
            ids.forEach(this::purgeAll);
        }
    }

}
