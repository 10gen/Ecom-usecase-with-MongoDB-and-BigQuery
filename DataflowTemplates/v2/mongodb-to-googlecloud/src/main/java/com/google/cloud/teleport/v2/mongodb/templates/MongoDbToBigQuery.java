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

import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.cloud.teleport.v2.mongodb.options.MongoDbToBigQueryOptions.BigQueryWriteOptions;
import com.google.cloud.teleport.v2.mongodb.options.MongoDbToBigQueryOptions.MongoDbOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.mongodb.MongoDbIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * The {@link BigQueryToMongoDb} pipeline is a batch pipeline which ingests data from MongoDB and
 * outputs the resulting records to BigQuery.
 */
public class MongoDbToBigQuery {

	  private static final Logger LOG = LoggerFactory.getLogger(MongoDbToBigQuery.class);

  /**
   * Options supported by {@link MongoDbToBigQuery}
   *
   * <p>Inherits standard configuration options.
   */
  public interface Options extends PipelineOptions, MongoDbOptions, BigQueryWriteOptions {}
  static final DateTimeFormatter TIMEFORMAT =
	      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
  private static class ParseAsDocumentsFn extends DoFn<String, Document> {
    @ProcessElement
    public void processElement(ProcessContext context) {
      context.output(Document.parse(context.element()));
    }
  }

  public static void main(String[] args) {
    Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
    run(options);
  }

  public static boolean run(Options options) {
    Pipeline pipeline = Pipeline.create(options);
    TableSchema bigquerySchema =
            MongoDBUtilsV1.getTableFieldSchema(
                options.getMongoDbUri(),
                options.getDatabase(),
                options.getCollection());

    String userOption = options.getUserOption();
    pipeline
        .apply(
            "Read Documents",
            MongoDbIO.read()
                .withBucketAuto(true)
                .withUri(options.getMongoDbUri())
                .withDatabase(options.getDatabase())
                .withCollection(options.getCollection()))
        .apply(
            "Transform to TableRow",
            ParDo.of(
                new DoFn<Document, TableRow>() {
                    ObjectMapper mapper = new ObjectMapper();
                  @ProcessElement
                  public void process(ProcessContext c) {
                    Document document = c.element();
                  
                    String docString=document.toJson();
                    TableRow row = new TableRow();
                    if (userOption.equals("FLATTEN")) {
                    JsonNode jsonNode;
                    try {
                      LOG.info(":Document from mongodb is:"+docString);
                      jsonNode = mapper.readValue(docString, JsonNode.class);
                      jsonNode.fieldNames().forEachRemaining(field -> {
                        JsonNode node = jsonNode.get(field);
                                                   
                        if (node.isObject()) {
                          if (node.isContainerNode()) {
						   //If its a nested bson document, then flow goes under isContainer Node
                            row.put(field, node);
                          } else {
						  //In case of _id field it goes into this else field
                            row.put(field, String.valueOf(node.asLong()));
                          }
                        } else
                          row.put(field, jsonNode.get(field));
                      });
                      LOG.info("In apply:");
                    } catch (Exception ex) {
                      ex.printStackTrace();
                    }
                   
                  }
                    else {
                    	  Gson gson = new GsonBuilder().create();
                          HashMap<String, Object> parsedMap =
                              gson.fromJson(document.toJson(), HashMap.class);
                      LocalDateTime localdate = LocalDateTime.now(ZoneId.of("UTC"));
                      row.set("id", parsedMap.get("_id").toString())
                          .set("source_data", parsedMap.toString())
                          .set("timestamp", localdate.format(TIMEFORMAT));
                    }
                  
                    
                    
                    
                    c.output(row);
                  }
                }))
        .apply(
            "Write to Bigquery",
            BigQueryIO.writeTableRows()
                .to(options.getOutputTableSpec())
                .withSchema(bigquerySchema)
                .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
                .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
    pipeline.run();
    return true;
  }
}
