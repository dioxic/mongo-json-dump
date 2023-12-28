package com.mongodb.jsondump;

import com.mongodb.MongoNamespace;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class MongoUtilTest {

    private final MongoClient client = MongoClients.create();

    @Test
    public void getNamespaces() {
        List<MongoNamespace> namespaces = MongoUtil.getNamespaces(client).collectList().block();

        assert namespaces != null;
        namespaces.forEach(System.out::println);
        assertFalse(namespaces.isEmpty());
    }

    @Test
    @Disabled
    public void getNamespacesFlux() {

        StepVerifier.create(MongoUtil.getNamespaces(client))
                .expectNext(new MongoNamespace("admin.system.version"))
                .expectComplete()
                .verify();
    }

    @Test
    public void print() {
        Flux.just(new MongoNamespace("test.people"), new MongoNamespace("test.cities"))
                .log()
                //.filter(ns -> nsExclude == null || !nsContains(ns, nsExclude))
                .subscribe(System.out::println);
    }

    @Test
    public void basic() {
        Flux<String> source = Flux.just("thing1", "thing2");

        StepVerifier.create(source)
                .expectNext("thing1")
                .expectNext("thing2")
                .verifyComplete();
    }

}
