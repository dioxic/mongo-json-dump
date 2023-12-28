package com.mongodb.jsondump;

import com.mongodb.MongoNamespace;
import com.mongodb.reactivestreams.client.MongoClient;
import reactor.core.publisher.Flux;

import java.util.List;

public class MongoUtil {

    public static Flux<MongoNamespace> getNamespaces(MongoClient client) {
        return Flux.from(client.listDatabaseNames())
                .flatMap(db ->
                        Flux.from(client.getDatabase(db).listCollectionNames())
                                .map(coll -> new MongoNamespace(db, coll))
                );
    }

    public static boolean nsMatch(MongoNamespace ns, MongoNamespace filter) {
        return ((ns.getDatabaseName().equals(filter.getDatabaseName()) || filter.getDatabaseName().equals("*"))
                && (ns.getCollectionName().equals(filter.getCollectionName()) || filter.getCollectionName().equals("*")));
    }

    public static boolean nsContains(MongoNamespace ns, List<MongoNamespace> filter) {
        return filter.stream().anyMatch(nsFilter -> nsMatch(ns, nsFilter));
    }

}
