package com.mongotopubsub.source;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;

import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MongoDbToPubSub {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDbToPubSub.class);

    public static void main(String[] args) throws Exception {

        String PROJECT_ID = System.getenv("ProjectId");

        String topicId = System.getenv("TopicId");

        MongoClient mongoClient = new MongoClient(new MongoClientURI(System.getenv("MongoURI")));

        MongoDatabase mongoDatabase = mongoClient.getDatabase(System.getenv("Database"));

        MongoCollection mongoCollection =  mongoDatabase.getCollection(System.getenv("Collection"));

        ProjectTopicName topicName = ProjectTopicName.of(PROJECT_ID, topicId);
        List<ApiFuture<String>> futures = new ArrayList<>();
        final Publisher publisher = Publisher.newBuilder(topicName).build();

        Block<ChangeStreamDocument<Document>> publishToPubSub = new Block<ChangeStreamDocument<Document>>() {
            @Override
            public void apply(final ChangeStreamDocument<Document> changeStreamDocument) {


                try {

                    Document fullDocument = (Document)changeStreamDocument.getFullDocument();
                    String id = fullDocument.get("_id").toString();
                    fullDocument.put("_id",id);

                    LOG.info("Document is:"+fullDocument.toJson());

                    ByteString data = ByteString.copyFromUtf8(fullDocument.toJson());
                    PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                            .setData(data)
                            .build();
                    // Schedule a message to be published. Messages are automatically batched.
                    ApiFuture<String> future = publisher.publish(pubsubMessage);

                    futures.add(future);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                }
            }
        };


        mongoCollection.watch().fullDocument(FullDocument.UPDATE_LOOKUP).forEach(publishToPubSub);

    }
}
