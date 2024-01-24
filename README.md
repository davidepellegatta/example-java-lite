# Couchbase lite +3.1 & Springboot
Couchbase Lite +3.1.1 generic client template &amp; Springboot integration 3.1.2 using Custom Scope &amp; Collections

```cmd
cd quickstart
docker-compose up
```

## Create a `db` database in SGW  

Using scopes/collections: 

```cmd
curl -X PUT "http://localhost:4985/db/" -H "accept: */*" -H "Content-Type: application/json" -d "{ \"name\": \"db\", \"bucket\": \"mobile\", \"scopes\": { \"custom\": { \"collections\": { \"typeA\": { \"sync\": \"function(doc, oldDoc, meta) { if(doc.channels) { channel(doc.destination); }else{ throw({forbidden: \\\"document'\\\"+doc._id+\\\"'doesn't contain channels field to sync\\\"}); } }\" }, \"typeB\": { \"sync\": \"function(doc, oldDoc, meta) { if(doc.channels) { channel(doc.destination); }else{ throw({forbidden: \\\"document'\\\"+doc._id+\\\"'doesn't contain channels field to sync\\\"}); } }\" } } } }, \"revs_limit\": 20, \"allow_conflicts\": false, \"num_index_replicas\": 0}"
```

## Create the `userdb1/password` SGW `db` database user/password

```cmd
curl -u sync_gateway:password -X POST "http://localhost:4985/db/_user/" -H "accept: */*" -H "Content-Type: application/json" -d "{\"name\":\"userdb1\",\"password\":\"password\",\"collection_access\":{\"custom\":{\"typeA\":{\"admin_channels\":[\"blue\"]},\"typeB\":{\"admin_channels\":[\"blue\"]}}},\"email\":\"userdb1@company.com\"}"
```