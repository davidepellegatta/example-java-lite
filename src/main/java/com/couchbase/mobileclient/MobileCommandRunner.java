package com.couchbase.mobileclient;

import com.couchbase.mobileclient.database.DBManager;
import com.couchbase.mobileclient.replicator.ReplicationManager;
import com.couchbase.mobileclient.config.CouchbaseLiteProperties;
import com.couchbase.mobileclient.utils.ConfigPrinter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class MobileCommandRunner implements CommandLineRunner {

    final ApplicationContext context;
    static final String STATUS = "inserted";
    static final String COLLECTION = "demo"; //TODO read from app.properties
    final ConfigPrinter printer = new ConfigPrinter();

    final CouchbaseLiteProperties properties;
    final DBManager database;
    final ReplicationManager replication;

    public MobileCommandRunner(ApplicationContext context, DBManager dbManager, ReplicationManager replicator, CouchbaseLiteProperties properties) {
        this.context = context;
        this.database = dbManager;
        this.replication = replicator;
        this.properties = properties;
    }

    private void welcome() {
        printer.printWelcome("Begin LOAD SYNC");
        log.info("-- Configuration Properties: ");
        log.info("{}",properties);
        printer.printLine();
    }

    @SneakyThrows
    private void printCountStatus() {
        log.info("Documents {} Count: {}",COLLECTION, database.count(COLLECTION));
        long count = database.countByStatus(STATUS);
        log.info("Total \"{}\" Documents in CBlite.{}: {}",STATUS, COLLECTION,count);
    }


    @Override
    public void run(String... args) throws Exception {
        welcome();

      //  printCountStatus();

        replication.getListenerManager().addStatusListenerCallback(this::onExit);

        replication.start();

      //  replication.getLatch().await(); // TODO

    }

    private void onExit()  {
        printer.printLine();
        //TODO print replication summary here
        log.info("End synchronisation...");
        printer.printLine();
        printer.printFooter("END LOAD SYNC - Shutting down the system...");
        System.exit(0);

    }


}
