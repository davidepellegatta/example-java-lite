package com.couchbase.mobileclient.controllers;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Result;
import com.couchbase.mobileclient.database.DBManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class HelloController {

    final DBManager dbManager;


    public HelloController(DBManager dbManager) {
        this.dbManager = dbManager;
    }


    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

    @GetMapping("/monitoring")
    public List<Result> getMeTheKeys() throws CouchbaseLiteException {
        return dbManager.listAllMonitorings();
    }

    @GetMapping("/monitoring/count")
    public Long count() throws CouchbaseLiteException {
        return dbManager.countMonitorings();
    }

    @GetMapping("/monitoring/save")
    public String create()  throws CouchbaseLiteException {

        dbManager.saveAMonitoring();
        return "success";
    }

}
