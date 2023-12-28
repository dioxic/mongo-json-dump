package com.mongodb.jsondump;

import com.mongodb.MongoNamespace;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

import static com.mongodb.jsondump.MongoUtil.nsContains;

public class Application implements Callable<Integer> {

    @Option(names = {"--uri"}, description = "mongodb uri", defaultValue = "mongodb://localhost:27017")
    private String uri;

    @Option(names = {"--concurrency"}, description = "concurrency factor", defaultValue = "4")
    private Integer concurrency;

    @Option(names = {"-o", "--output"}, description = "output directory", defaultValue = "dump")
    private Path outputDir;

    @Option(
            names = {"--nsExclude"},
            description = "exclude matching namespaces",
            arity = "0..*",
            converter = NamespaceConverter.class)
    private List<MongoNamespace> nsExclude;

    @Override
    public Integer call() throws Exception {

        try (MongoClient client = MongoClients.create(uri)) {
            MongoUtil.getNamespaces(client)
                    .filter(ns -> nsExclude == null || !nsContains(ns, nsExclude))
                    .map(ns -> client.getDatabase(ns.getDatabaseName()).getCollection(ns.getCollectionName()))
                    .map(coll -> {
                        String collectionName = coll.getNamespace().getCollectionName();
                        Path path = outputDir.resolve(collectionName + ".json");
                        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                            return Flux.from(coll.find())
                                    .flatMap(doc -> Mono.just(doc.toJson()), concurrency)
                                    .doOnNext(json -> {
                                        try {
                                            writer.write(json);
                                            writer.newLine();
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    })
                                    .count()
                                    .map(count -> count + " docs written for " + collectionName + " collection")
                                    .block();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .doOnNext(System.out::println)
                    .blockLast();
        }

        return 0;
    }

    BiConsumer<String, SynchronousSink<Integer>> handler = (input, sink) -> {
        if (input.matches("\\D")) {
            sink.error(new NumberFormatException());
        } else {
            sink.next(Integer.parseInt(input));
        }
    };

    public static void main(String... args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

}
