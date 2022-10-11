/*
 * Copyright (C) 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.teleport.v2.mongodb.templates;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.cloud.teleport.v2.mongodb.options.MongoDbToBigQueryOptions.BigQueryWriteOptions;
import com.google.cloud.teleport.v2.mongodb.options.MongoDbToBigQueryOptions.MongoDbOptions;
import com.google.cloud.teleport.v2.mongodb.options.MongoDbToBigQueryOptions.PubSubOptions;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The pipeline is a streaming pipeline which reads data pushed to PubSub
 * from MongoDB Changestream and outputs the resulting records to BigQuery.
 */
public class MongoDbToBigQueryCdc {

  private static final Logger LOG = LoggerFactory.getLogger(MongoDbToBigQueryCdc.class);

  /** Options interface. */
  public interface Options
      extends PipelineOptions, MongoDbOptions, PubSubOptions, BigQueryWriteOptions {}

  /** class ParseAsDocumentsFn. */
  private static class ParseAsDocumentsFn extends DoFn<String, Document> {

    @ProcessElement
    public void processElement(ProcessContext context) {
      context.output(Document.parse(context.element()));
    }
  }

  /**
   * Main entry point for pipeline execution.
   *
   * @param args Command line arguments to the pipeline.
   */
  public static void main(String[] args) {

    Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
    run(options);
  }

  /** Pipeline to read data from PubSub and write to MongoDB. */
  public static boolean run(Options options) {
    Pipeline pipeline = Pipeline.create(options);
    options.setStreaming(true);
    TableSchema bigquerySchema =
        MongoDBUtilsV1.getTableFieldSchema(
            options.getMongoDbUri(),
            options.getDatabase(),
            options.getCollection());

    String userOption = options.getUserOption();
    String inputTopic = options.getInputTopic();

    pipeline
        .apply("Read PubSub Messages", PubsubIO.readStrings().fromTopic(inputTopic))
        .apply(
            "Read and transform data",
            MapElements.via(
                    new SimpleFunction<String, TableRow>() {
                      ObjectMapper mapper = new ObjectMapper();
                      @Override
                      public TableRow apply(String docString) {
                        TableRow row = new TableRow();
                        JsonNode jsonNode;
                        try {
                          LOG.info(":Document from pubsub is:"+docString);
                          jsonNode = mapper.readValue(docString, JsonNode.class);
                          jsonNode.fieldNames().forEachRemaining(field -> {
                            JsonNode node = jsonNode.get(field);

                            if (node.isObject()) {
                              //If its a nested bson document, then flow goes under isContainer Node
                              if (node.isContainerNode()) {
                                row.put(field, node);
                              }//In case of _id field it goes into this else field
                              else {
                                row.put(field, String.valueOf(node.asLong()));
                              }
                            }
                            //If field under iteration is of primitive datatype then it comes into else
                            else
                              row.put(field, jsonNode.get(field));
                          });
                          LOG.info("In apply:");
                        } catch (Exception ex) {
                          ex.printStackTrace();
                        }
                        return row;
                      }
                    }))
        .apply(
            BigQueryIO.writeTableRows()
                .to(options.getOutputTableSpec())
                .withSchema(bigquerySchema)
                .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
                .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
    pipeline.run();
    return true;
  }
}
