package com.couchbase.mobileclient.config;

import com.couchbase.lite.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.stream.Collectors.joining;

@Data
@ConfigurationProperties(prefix = "couchbase")
@ConfigurationPropertiesScan
public class CouchbaseLiteProperties {

    RemoteProperties remote = new RemoteProperties();
    LocalDBProperties local = new LocalDBProperties();
    LogProperties log = new LogProperties();

    public String toString() {
        return """
                
                couchbase:
                \t local: %s
                \t remote: %s
                \t log: %s
                """.formatted(local, remote, log);
    }


    @Data
    public static class CollectionProperties {
        List<String> channelsFilter = new ArrayList<>();
        List<String> documentIDsFilter = new ArrayList<>();


        public String toString() {
            return """
                    
                    \t\t\t\tdocumentIDs-filter: %s
                    \t\t\t\tchannels-filter: %s""".formatted(documentIDsFilter.isEmpty()? "--none--": join(",", documentIDsFilter),channelsFilter.isEmpty()? "--none--": join(",", channelsFilter));
        }
    }

    @Data
    @Builder
    public static class ListenerProperties {
        @Builder.Default
        long maxSize = 10_000;
        @Builder.Default
        Duration maxDuration = Duration.ofMinutes(1);

        public String toString() {
            return """
                    
                    \t\t\t\tmax-size: %d
                    \t\t\t\tmax-duration: %s""".formatted(maxSize, maxDuration);
        }
    }


    @Data
    public static class AuthenticatorProperties {
        String username="test";
        String password="password";

        public String toString() {
            return """
                    
                    \t\t\tusername: %s
                    \t\t\tpassword: %s""".formatted(username, "*".repeat(password.length()));
        }
    }

    @Data
    public static class WebsocketProperties {
        long timeout = 10_000;
        long heartbeat = 15_000;

        public String toString() {
            return """
                    
                    \t\t\ttimeout: %d
                    \t\t\theartbeat: %d""".formatted(timeout, heartbeat);
        }
    }

    @Data
    public static class RemoteProperties {
        public static final String DEFAULT_SERVER = "127.0.0.1";
        public static final int DEFAULT_PORT = 4984;
        public static final String DEFAULT_DATABASE = "db";
        public static final String DEFAULT_ENDPOINT_URL = "ws://%s:%d/%s".formatted(DEFAULT_SERVER, DEFAULT_PORT, DEFAULT_DATABASE);

        String endpointUrl = DEFAULT_ENDPOINT_URL;
        boolean continuous = false;
        ReplicatorType replicatorType = ReplicatorType.PUSH_AND_PULL;
        boolean resetCheckpoint = false;
        Map<String, ListenerProperties> listeners = new HashMap<>();
        Map<String, CollectionProperties> collections = new HashMap<>();
        WebsocketProperties websocket;
        AuthenticatorProperties authenticator;

        public Endpoint getEndpoint() {
            Endpoint endpoint;
            try {
                endpoint = new URLEndpoint(new URI(endpointUrl));
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
            return endpoint;
        }

        private String listenersToString() {
            return listeners.entrySet().stream().map(e -> "\n\t\t\t%s: %s".formatted(e.getKey(), e.getValue())).collect(joining());
        }

        private String collectionsToString() {
            return collections.entrySet().stream().map(e -> "\n\t\t\t%s: %s".formatted(e.getKey(), e.getValue())).collect(joining());
        }

        public String toString() {
            return """
                    
                    \t\tendpoint-url: %s                 
                    \t\tcontinuous: %b
                    \t\treplicator-type: %s
                    \t\treset-checkpoint: %b
                    \t\tlisteners: %s
                    \t\tcollections: %s
                    \t\twebsocket: %s
                    \t\tauthenticator: %s
                    """.formatted(endpointUrl, continuous, replicatorType, resetCheckpoint, listenersToString(),collectionsToString(), websocket, authenticator);
        }
    }

    @Data
    public static class LocalDBProperties {
        String database = "demo";
        String dbPath = "data";
        ScopeProperties scope = new ScopeProperties();
        String downloadPath = "tmpdb";
        boolean preBuilt = false;
        boolean flushPreviousDb = true;
        boolean autoPurge = true;
        String encryptionKey = null;
        boolean copyDb = false;


        public boolean isEncryptedDb() {
            return !Objects.isNull(this.encryptionKey) && !encryptionKey.isEmpty();
        }

        public String getDbFolderName() {
            return this.getDatabase()+".cblite2";
        }

        public File getUnzippedDbFolderFile() {
            return new File(this.getDownloadPath(), getDbFolderName());
        }

        public File getZippedDbFile() {
            return  new File(this.getDownloadPath(), getDbFolderName()+".zip");
        }

        public File getDbFolderFile() {
            return new File(getDbPath(),getDbFolderName());
        }

        public File getSQLLiteDBFile() {
            return new File(new File(this.getDbPath(),getDbFolderName()),"db.sqlite3");
        }

        public String getSqliteURL() {
            return format("jdbc:sqlite:%s",this.getSQLLiteDBFile().getAbsolutePath());
        }

        public String toString() {
            return """
                    
                    \t\tdatabase: %s                   
                    \t\tdb-path: %s
                    \t\tscope: %s
                    \t\tdownload-path: %s
                    \t\tpre-built: %b
                    \t\tflush-previous-db: %b
                    \t\tauto-purge: %b
                    \t\tencryption: %b
                    \t\tcopy-db: %b
                    """.formatted(database,dbPath, scope,downloadPath,preBuilt,flushPreviousDb,autoPurge, isEncryptedDb(), copyDb);
        }

    }

    @Data
    public static class LogProperties {
      String path = "logs";
      LogLevel level = LogLevel.INFO;
      long maxSize = 10;
      int rotationCount = 10;
      boolean plainText = false;

      public String toString() {
          return """
                  
                  \t\tpath: %s
                  \t\tlevel: %s
                  \t\tmax-size: %d
                  \t\trotation-count: %d
                  \t\tplaintext: %b
                  """.formatted(path, level, maxSize, rotationCount, plainText);
      }
    }

    @Data
    public static class ScopeProperties {
        public static final String DEFAULT_SCOPE = "_default";
        public static final String DEFAULT_COLLECTION = "_default";

        String name = DEFAULT_SCOPE;
        List<String> collections = List.of(DEFAULT_COLLECTION);

        public String toString() {
            return """

                    \t\t\tname: %s
                    \t\t\tcollections: %s""".formatted(name, join(",", collections));
        }
    }
}
