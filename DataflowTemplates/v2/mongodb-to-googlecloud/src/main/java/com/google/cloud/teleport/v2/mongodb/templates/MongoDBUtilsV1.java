package com.google.cloud.teleport.v2.mongodb.templates;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Class containing util methods to create table scheme in big query table. Below is a sample document for which util methods in
 * this class can generate schema in big query.
 * {
 *   "saleDate": "1363797399112",
 *   "items": [
 *     {
 *       "name": "notepad",
 *       "tags": [
 *         "office",
 *         "writing",
 *         "school"
 *       ],
 *       "quantity": 5
 *     },
 *     {
 *       "name": "printer paper",
 *       "tags": [
 *         "office",
 *         "stationary"
 *       ],
 *       "quantity": 2
 *     }
 *   ],
 *   "storeLocation": "San Diego",
 *   "customer": {
 *     "gender": "M",
 *     "age": 61,
 *     "email": "ucikosusu@sid.uz",
 *     "satisfaction": 1
 *   },
 *   "couponUsed": false,
 *   "purchaseMethod": "Online"
 * }
 */
public class MongoDBUtilsV1 {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDBUtilsV1.class);

    /** Maps and Returns the Datatype form MongoDb To BigQuery. If value is of type arraylist then it checks its contents
     * and returns type based on its contents.*/
    public static String getTableSchemaDataType(Object value, String s) {
        switch (s) {
            case "java.lang.Integer":
                return "INTEGER";
            case "java.lang.Boolean":
                return "BOOLEAN";
            case "org.bson.Document":
                return "STRUCT";
            case "java.lang.Double":
            case "org.bson.types.Decimal128":
                return "FLOAT";
            case "java.util.ArrayList":
                ArrayList ls = (ArrayList)value;
                if(ls.size() > 0) {
                    Object val = ls.get(0);
                    if (val instanceof String) {
                        return "STRING";
                    }
                    if (val instanceof Float) {
                        return "FLOAT";
                    }
                    if (val instanceof Integer) {
                        return "INTEGER";
                    }
                }
                return "STRUCT";
            case "org.bson.types.ObjectId":
                return "STRING";
        }
        return "STRING";
    }

    /**
     * Method that decides upon mode of TableFieldSchema, if its REPEATED or NULLABLE and returns
     * @param s
     * @return
     */
    public static String getModeForTableColumn(String s) {
        switch (s) {
            case "java.util.ArrayList":
                return "REPEATED";
            default:
                return "NULLABLE";
        }
    }

    /** Get a Document from MongoDB to generate the schema for BigQuery. */
    public static Document getMongoDbDocument(String uri, String dbName, String collName) {
        MongoClient mongoClient = MongoClients.create(uri);
        MongoDatabase database = mongoClient.getDatabase(dbName);
        MongoCollection<Document> collection = database.getCollection(collName);
        Document doc = collection.find().first();
        return doc;
    }

    /**
     * Method that gets invoked when object that is under processing is an array of bson documents or a nested bson document
     * @param outerSchema
     * @param value
     */
    public static void getTableFieldSchemaComplex(TableFieldSchema outerSchema, Object value){
        List<TableFieldSchema> ls = new ArrayList<>();
        //If its a nested bson document
        if(value instanceof Document)
        {
            Document internalDoc = (Document) value;
            prepareListOfTableFieldSchFromBson(internalDoc,ls);
        }
        //if its an array of bson documents
        else if(value instanceof ArrayList){
            List valueList = (ArrayList)value;
            if(valueList.size() > 0) {
                if(valueList.get(0) instanceof Document) {
                    //Getting one document from array of documents to determine schema for this document
                    Document obj = (Document) valueList.get(0);
                    prepareListOfTableFieldSchFromBson(obj, ls);
                }
            }
        }
        outerSchema.setFields(ls);
    }

    /**
     * Method that iterates on contents of internal bson document, and comes up with corresponding table field schema for this document.
     * If any of the content of document is again an array of primitive fields(like array of string) or bson document then table
     * field schema is prepared accordingly and added to list of TableFieldSchema
     * @param internalDoc
     * @param ls
     */
    public static void prepareListOfTableFieldSchFromBson(Document internalDoc, List<TableFieldSchema> ls){
        Set<String> keySet = internalDoc.keySet();
        Iterator<String> itr = keySet.iterator();
        while(itr.hasNext()) {
            String key = itr.next();
            LOG.info(":Key is:" + key);
            Object internalVal = internalDoc.get(key);
            LOG.info(":Value is:" + internalVal.getClass().getName());
            String type = getTableSchemaDataType(internalVal, internalVal.getClass().getName());
            if(!"java.util.ArrayList".equals(internalVal.getClass().getName()) && !"org.bson.Document".equals(internalVal.getClass().getName()))
                //If contents of internal bson document is not array of bson document then create TableFieldSchema element
                //and add it to list of TableFieldSchema
                ls.add(new TableFieldSchema().setType(type).setName(key).setMode(getModeForTableColumn(type)));
            else {
                //If contents of internal document are array of primitives or another bson document then create TableFieldSchema
                //accordingly.
                TableFieldSchema outerSchema = new TableFieldSchema();
                outerSchema.setType(type);
                outerSchema.setName(key);
                if("java.util.ArrayList".equals(internalVal.getClass().getName()))
                    outerSchema.setMode(getModeForTableColumn("java.util.ArrayList"));
                ls.add(outerSchema);
            }
        }
    }

    /**
     * Method that gets first document in mongo collection, iterates on contents of document, checks if value under iteration
     * is of type, bson document or array or of primitive type and accordingly prepares TableFieldSchema object and adds it to list
     * of TableFieldSchema used to creating table schema in BigQuery
     * @param uri
     * @param database
     * @param collection
     * @return
     */
    public static TableSchema getTableFieldSchema(
            String uri, String database, String collection) {
        List<TableFieldSchema> bigquerySchemaFields = new ArrayList<>();
        Document document = getMongoDbDocument(uri, database, collection);

        document.forEach(
                (key, value) -> {
                    LOG.info("Key is:"+key+":Value is:"+value);
                    //If value under iteration is of type bson Document
                    if(value instanceof Document) {
                        TableFieldSchema outerSchema = new TableFieldSchema();
                        outerSchema.setType("STRUCT");
                        outerSchema.setName(key);
                        getTableFieldSchemaComplex(outerSchema, value);
                        bigquerySchemaFields.add(outerSchema);
                    }
                    //If value under iteration is of type array
                    else if(value instanceof ArrayList){
                        TableFieldSchema outerSchema = new TableFieldSchema();
                        outerSchema.setType(getTableSchemaDataType(value, value.getClass().getName()));
                        outerSchema.setMode("REPEATED");
                        outerSchema.setName(key);
                        getTableFieldSchemaComplex(outerSchema, value);
                        bigquerySchemaFields.add(outerSchema);
                    }
                    //If its of primitive type
                    else {
                        LOG.info("Type is:"+value.getClass().getName());
                        bigquerySchemaFields.add(
                                new TableFieldSchema()
                                        .setName(key)
                                        .setType(getTableSchemaDataType(value, value.getClass().getName())).setMode(getModeForTableColumn(value.getClass().getName())));
                    }
                });
        LOG.info("bigquerySchemaFields is:"+bigquerySchemaFields);
        TableSchema bigquerySchema = new TableSchema().setFields(bigquerySchemaFields);
        return bigquerySchema;
    }

}

