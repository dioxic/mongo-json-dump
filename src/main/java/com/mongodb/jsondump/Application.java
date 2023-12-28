package com.mongodb.jsondump;

import com.mongodb.MongoNamespace;
import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.bson.Document;
import org.bson.RawBsonDocument;
import org.bson.UuidRepresentation;
import org.bson.codecs.*;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriter;
import org.bson.json.JsonWriterSettings;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static com.mongodb.jsondump.MongoUtil.nsContains;
import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.withUuidRepresentation;

@Command(name = "mongojsondump")
public class Application implements Callable<Integer> {

    @Option(names = {"--uri"}, description = "mongodb uri", defaultValue = "mongodb://localhost:27017")
    private String uri;

    @Option(names = {"-o", "--output"}, description = "output directory", defaultValue = "dump")
    private Path outputDir;

    @Option(names = {"--includeSystem"}, description = "include system databases (config, admin, local)", defaultValue = "false")
    private Boolean includeSystemCollections;

    @Option(names = {"--limit"}, description = "limit the document per collection")
    private Integer limit;

    @Option(names = {"--batchSize"}, description = "cursor batch size")
    private Integer batchSize = 10000;

    @Option(
            names = {"--nsExclude"},
            description = "exclude matching namespaces",
            arity = "0..*",
            converter = NamespaceConverter.class)
    private List<MongoNamespace> nsExclude;

    @Override
    public Integer call() throws Exception {

        if (!includeSystemCollections) {
            if (nsExclude == null) {
                nsExclude = new ArrayList<>();
            }
            nsExclude.add(new MongoNamespace("admin.*"));
            nsExclude.add(new MongoNamespace("local.*"));
            nsExclude.add(new MongoNamespace("config.*"));
        }

        Files.createDirectories(outputDir);
        JsonWriterSettings jws = JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build();
        Codec<Document> DEFAULT_CODEC =
                withUuidRepresentation(fromProviders(asList(new ValueCodecProvider(),
                        new CollectionCodecProvider(), new IterableCodecProvider(),
                        new BsonValueCodecProvider(), new DocumentCodecProvider(), new MapCodecProvider())), UuidRepresentation.STANDARD)
                        .get(Document.class);
        RawBsonDocumentCodec rawCodec = new RawBsonDocumentCodec();

        try (MongoClient client = MongoClients.create(uri)) {
            MongoUtil.getNamespaces(client)
                    .filter(ns -> nsExclude == null || !nsContains(ns, nsExclude))
                    .map(ns -> client.getDatabase(ns.getDatabaseName()).getCollection(ns.getCollectionName(), RawBsonDocument.class))
                    .map(coll -> {
                        String collectionName = coll.getNamespace().getCollectionName();
                        Path path = outputDir.resolve(collectionName + ".json");

                        System.out.println("Processing " + collectionName + "...");

                        try (BufferedWriter fileWriter = Files.newBufferedWriter(path)) {
                            FindPublisher<RawBsonDocument> cursor = coll.find().batchSize(batchSize);
                            if (limit != null) {
                                cursor = cursor.limit(limit);
                            }

                            return Flux.from(cursor).parallel()
                                    .runOn(Schedulers.parallel())
                                    .doOnNext(doc -> {
                                        StringWriter stringWriter = new StringWriter();
                                        JsonWriter jsonWriter = new JsonWriter(stringWriter, jws);
                                        rawCodec.encode(jsonWriter, doc, EncoderContext.builder().build());
                                        try {
                                            fileWriter.write(stringWriter.append("\n").toString());
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    })
                                    .sequential()
                                    .count()
                                    .map(count -> count + " docs written for " + coll.getNamespace() + " collection")
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

    public static void main(String... args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

}
