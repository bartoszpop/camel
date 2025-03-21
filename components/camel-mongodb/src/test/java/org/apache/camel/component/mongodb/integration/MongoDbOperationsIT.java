/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.mongodb.integration;

import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mongodb.MongoDbConstants;
import org.apache.camel.component.mongodb.MongoDbOperation;
import org.apache.camel.test.infra.core.annotations.RouteFixture;
import org.apache.camel.test.infra.core.api.ConfigurableRoute;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.currentTimestamp;
import static com.mongodb.client.model.Updates.set;
import static org.apache.camel.component.mongodb.MongoDbConstants.CRITERIA;
import static org.apache.camel.component.mongodb.MongoDbConstants.MONGO_ID;
import static org.apache.camel.test.junit5.TestSupport.assertListSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MongoDbOperationsIT extends AbstractMongoDbITSupport implements ConfigurableRoute {

    @BeforeEach
    void checkDocuments() {
        Assumptions.assumeTrue(0 == testCollection.countDocuments(), "The collection should have no documents");
    }

    @Test
    public void testCountOperation() {
        Object result = template.requestBody("direct:count", "irrelevantBody");
        assertTrue(result instanceof Long, "Result is not of type Long");
        assertEquals(0L, result, "Test collection should not contain any records");

        // Insert a record and test that the endpoint now returns 1
        testCollection.insertOne(Document.parse("{a:60}"));
        result = template.requestBody("direct:count", "irrelevantBody");
        assertTrue(result instanceof Long, "Result is not of type Long");
        assertEquals(1L, result, "Test collection should contain 1 record");
        testCollection.deleteOne(new Document());

        // test dynamicity
        dynamicCollection.insertOne(Document.parse("{a:60}"));
        result = template.requestBodyAndHeader("direct:count", "irrelevantBody", MongoDbConstants.COLLECTION,
                dynamicCollectionName);
        assertTrue(result instanceof Long, "Result is not of type Long");
        assertEquals(1L, result, "Dynamic collection should contain 1 record");

    }

    @Test
    public void testInsertString() {
        Object result = template.requestBody("direct:insert",
                new Document(MONGO_ID, "testInsertString").append("scientist", "Einstein").toJson());
        assertTrue(result instanceof Document);
        Document b = testCollection.find(eq(MONGO_ID, "testInsertString")).first();
        assertNotNull(b, "No record with 'testInsertString' _id");
    }

    @Test
    public void testStoreOidOnInsert() {
        Document document = new Document();
        ObjectId oid = template.requestBody("direct:testStoreOidOnInsert", document, ObjectId.class);
        assertEquals(document.get(MONGO_ID), oid);
    }

    @Test
    public void testStoreOidsOnInsert() {
        Document firsDocument = new Document();
        Document secondDoocument = new Document();
        List<?> oids
                = template.requestBody("direct:testStoreOidOnInsert", Arrays.asList(firsDocument, secondDoocument), List.class);
        assertTrue(oids.contains(firsDocument.get(MONGO_ID)));
        assertTrue(oids.contains(secondDoocument.get(MONGO_ID)));
    }

    @Test
    public void testSave() {
        Object[] req = new Object[] {
                new Document(MONGO_ID, "testSave1").append("scientist", "Einstein").toJson(),
                new Document(MONGO_ID, "testSave2").append("scientist", "Copernicus").toJson() };
        Object result = template.requestBody("direct:insert", req);
        assertTrue(result instanceof List);
        assertEquals(2, testCollection.countDocuments(), "Number of records persisted must be 2");

        // Testing the save logic
        Document record1 = testCollection.find(eq(MONGO_ID, "testSave1")).first();
        assertEquals("Einstein", record1.get("scientist"), "Scientist field of 'testSave1' must equal 'Einstein'");
        record1.put("scientist", "Darwin");

        result = template.requestBody("direct:save", record1);
        assertTrue(result instanceof UpdateResult);

        record1 = testCollection.find(eq(MONGO_ID, "testSave1")).first();
        assertEquals("Darwin", record1.get("scientist"),
                "Scientist field of 'testSave1' must equal 'Darwin' after save operation");

    }

    @Test
    public void testSaveWithShardedKey() {
        // Prepare test
        Assumptions.assumeTrue(0 == testCollection.countDocuments(), "The collection should have no documents");

        Object[] req = new Object[] {
                new Document(MONGO_ID, "testSave1").append("scientist", "Einstein").append("country", "Germany").toJson(),
                new Document(MONGO_ID, "testSave2").append("scientist", "Copernicus").append("country", "Poland").toJson() };
        Object result = template.requestBody("direct:insert", req);
        assertTrue(result instanceof List);
        assertEquals(2, testCollection.countDocuments(), "Number of records persisted must be 2");

        // Testing the save logic
        Document record1 = testCollection.find(eq(MONGO_ID, "testSave1")).first();
        assertEquals("Einstein", record1.get("scientist"), "Scientist field of 'testSave1' must equal 'Einstein'");
        record1.put("scientist", "Kepler");

        //Pass sharded collection key as CRITERIA to prevent "MongoWriteException: Failed to target upsert by query :: could not extract exact shard key"
        result = template.requestBodyAndHeader("direct:save", record1, CRITERIA, eq("country", "Germany"));
        assertTrue(result instanceof UpdateResult);

        record1 = testCollection.find(eq(MONGO_ID, "testSave1")).first();
        assertEquals("Kepler", record1.get("scientist"),
                "Scientist field of 'testSave1' must equal 'Kepler' after save operation");

    }

    @Test
    public void testSaveWithoutId() {
        // This document should not be modified
        Document doc = new Document("scientist", "Copernic");
        template.requestBody("direct:insert", doc);
        // save (upsert) a document without Id => insert with new Id
        doc = new Document("scientist", "Einstein");
        assertNull(doc.get(MONGO_ID));
        Object resultObj = template.requestBody("direct:save", doc);
        // Without Id save performs an insert not an update.
        assertInstanceOf(InsertOneResult.class, resultObj);
        InsertOneResult resultInsertOne = (InsertOneResult) resultObj;
        assertNotNull(resultInsertOne.getInsertedId());

        // Testing the save logic
        Document record1 = testCollection.find(eq(MONGO_ID, resultInsertOne.getInsertedId())).first();
        assertEquals("Einstein", record1.get("scientist"),
                "Scientist field of '" + resultInsertOne.getInsertedId() + "' must equal 'Einstein'");
    }

    @Test
    public void testStoreOidOnSaveWithoutId() {
        Document document = new Document();
        ObjectId oid = template.requestBody("direct:testStoreOidOnSave", document, ObjectId.class);
        assertNotNull(oid);
    }

    @Test
    public void testStoreOidOnSave() {
        Document document = new Document(MONGO_ID, new ObjectId("5847e39e0824d6b54194e197"));
        ObjectId oid = template.requestBody("direct:testStoreOidOnSave", document, ObjectId.class);
        assertEquals(document.get(MONGO_ID), oid);
    }

    @Test
    public void testUpdate() {
        for (int i = 1; i <= 100; i++) {
            String body;
            try (Formatter f = new Formatter()) {
                if (i % 2 == 0) {
                    body = f.format("{\"_id\":\"testSave%d\", \"scientist\":\"Einstein\"}", i).toString();
                } else {
                    body = f.format("{\"_id\":\"testSave%d\", \"scientist\":\"Einstein\", \"extraField\": true}", i).toString();
                }
            }
            template.requestBody("direct:insert", body);
        }
        assertEquals(100L, testCollection.countDocuments());

        // Testing the update logic
        Bson extraField = eq("extraField", true);
        assertEquals(50L, testCollection.countDocuments(extraField),
                "Number of records with 'extraField' flag on must equal 50");
        assertEquals(0, testCollection.countDocuments(new Document("scientist", "Darwin")),
                "Number of records with 'scientist' field = Darwin on must equal 0");

        Bson updateObj = combine(set("scientist", "Darwin"), currentTimestamp("lastModified"));

        Exchange resultExchange = template.request("direct:update", new Processor() {
            @Override
            public void process(Exchange exchange) {
                exchange.getIn().setBody(new Bson[] { extraField, updateObj });
                exchange.getIn().setHeader(MongoDbConstants.MULTIUPDATE, true);
            }
        });
        Object result = resultExchange.getMessage().getBody();
        assertTrue(result instanceof UpdateResult);
        assertEquals(50L, resultExchange.getMessage().getHeader(MongoDbConstants.RECORDS_AFFECTED),
                "Number of records updated header should equal 50");

        assertEquals(50, testCollection.countDocuments(new Document("scientist", "Darwin")),
                "Number of records with 'scientist' field = Darwin on must equal 50 after update");
    }

    @Test
    public void testUpdateFromString() {
        // Prepare test
        Assumptions.assumeTrue(0 == testCollection.countDocuments(), "The collection should have no documents");

        for (int i = 1; i <= 100; i++) {
            String body;
            try (Formatter f = new Formatter()) {
                if (i % 2 == 0) {
                    body = f.format("{\"_id\":\"testSave%d\", \"scientist\":\"Einstein\"}", i).toString();
                } else {
                    body = f.format("{\"_id\":\"testSave%d\", \"scientist\":\"Einstein\", \"extraField\": true}", i).toString();
                }
            }
            template.requestBody("direct:insert", body);
        }
        assertEquals(100L, testCollection.countDocuments());

        // Testing the update logic
        Bson extraField = eq("extraField", true);
        assertEquals(50L, testCollection.countDocuments(extraField),
                "Number of records with 'extraField' flag on must equal 50");
        assertEquals(0, testCollection.countDocuments(new Document("scientist", "Darwin")),
                "Number of records with 'scientist' field = Darwin on must equal 0");

        Bson updateObj = combine(set("scientist", "Darwin"), currentTimestamp("lastModified"));

        String updates
                = "[" + extraField.toBsonDocument(Document.class, MongoClientSettings.getDefaultCodecRegistry()).toJson() + ","
                  + updateObj.toBsonDocument(Document.class, MongoClientSettings.getDefaultCodecRegistry()).toJson() + "]";

        Exchange resultExchange = template.request("direct:update", new Processor() {
            @Override
            public void process(Exchange exchange) {
                exchange.getIn().setBody(updates);
                exchange.getIn().setHeader(MongoDbConstants.MULTIUPDATE, true);
            }
        });
        Object result = resultExchange.getMessage().getBody();
        assertTrue(result instanceof UpdateResult);
        assertEquals(50L, resultExchange.getMessage().getHeader(MongoDbConstants.RECORDS_AFFECTED),
                "Number of records updated header should equal 50");

        assertEquals(50, testCollection.countDocuments(new Document("scientist", "Darwin")),
                "Number of records with 'scientist' field = Darwin on must equal 50 after update");
    }

    @Test
    public void testUpdateUsingFieldsFilterHeader() {
        for (int i = 1; i <= 100; i++) {
            String body;
            try (Formatter f = new Formatter()) {
                if (i % 2 == 0) {
                    body = f.format("{\"_id\":\"testSave%d\", \"scientist\":\"Einstein\"}", i).toString();
                } else {
                    body = f.format("{\"_id\":\"testSave%d\", \"scientist\":\"Einstein\", \"extraField\": true}", i).toString();
                }
            }
            template.requestBody("direct:insert", body);
        }
        assertEquals(100L, testCollection.countDocuments());

        // Testing the update logic
        Bson extraField = eq("extraField", true);
        assertEquals(50L, testCollection.countDocuments(extraField),
                "Number of records with 'extraField' flag on must equal 50");
        assertEquals(0, testCollection.countDocuments(new Document("scientist", "Darwin")),
                "Number of records with 'scientist' field = Darwin on must equal 0");

        Bson updateObj = combine(set("scientist", "Darwin"), currentTimestamp("lastModified"));
        HashMap<String, Object> headers = new HashMap<>();
        headers.put(MongoDbConstants.MULTIUPDATE, true);
        headers.put(MongoDbConstants.CRITERIA, extraField);
        Object result = template.requestBodyAndHeaders("direct:update", updateObj, headers);
        assertTrue(result instanceof UpdateResult);
        assertEquals(50L, UpdateResult.class.cast(result).getModifiedCount(),
                "Number of records updated header should equal 50");
        assertEquals(50, testCollection.countDocuments(new Document("scientist", "Darwin")),
                "Number of records with 'scientist' field = Darwin on must equal 50 after update");
    }

    @Test
    public void testRemove() {
        for (int i = 1; i <= 100; i++) {
            String body;
            try (Formatter f = new Formatter()) {
                if (i % 2 == 0) {
                    body = f.format("{\"_id\":\"testSave%d\", \"scientist\":\"Einstein\"}", i).toString();
                } else {
                    body = f.format("{\"_id\":\"testSave%d\", \"scientist\":\"Einstein\", \"extraField\": true}", i).toString();
                }
            }
            template.requestBody("direct:insert", body);
        }
        assertEquals(100L, testCollection.countDocuments());

        // Testing the update logic
        Bson extraField = Filters.eq("extraField", true);
        assertEquals(50L, testCollection.countDocuments(extraField),
                "Number of records with 'extraField' flag on must equal 50");

        Exchange resultExchange = template.request("direct:remove", new Processor() {
            @Override
            public void process(Exchange exchange) {
                exchange.getIn().setBody(extraField);
            }
        });
        Object result = resultExchange.getMessage().getBody();
        assertTrue(result instanceof DeleteResult);
        assertEquals(50L, resultExchange.getMessage().getHeader(MongoDbConstants.RECORDS_AFFECTED),
                "Number of records deleted header should equal 50");

        assertEquals(0, testCollection.countDocuments(extraField),
                "Number of records with 'extraField' flag on must be 0 after remove");

    }

    @Test
    public void testAggregate() {
        pumpDataIntoTestCollection();

        // Repeat ten times, obtain 10 batches of 100 results each time
        List<Bson> aggregate = Arrays.asList(match(or(eq("scientist", "Darwin"), eq("scientist", "Einstein"))),
                group("$scientist", sum("count", 1)));
        Object result = template.requestBody("direct:aggregate", aggregate);
        assertTrue(result instanceof List, "Result is not of type List");

        @SuppressWarnings("unchecked")
        List<Document> resultList = (List<Document>) result;
        assertListSize("Result does not contain 2 elements", resultList, 2);
        // TODO Add more asserts
    }

    @Test
    public void testDbStats() {
        Assumptions.assumeTrue(0 == testCollection.countDocuments(), "The collection should have no documents");

        Object result = template.requestBody("direct:getDbStats", "irrelevantBody");
        assertTrue(result instanceof Document, "Result is not of type Document");
        assertTrue(Document.class.cast(result).keySet().size() > 0, "The result should contain keys");
    }

    @Test
    public void testColStats() {
        // Add some records to the collection (and do it via camel-mongodb)
        for (int i = 1; i <= 100; i++) {
            String body;
            try (Formatter f = new Formatter()) {
                body = f.format("{\"_id\":\"testSave%d\", \"scientist\":\"Einstein\"}", i).toString();
            }
            template.requestBody("direct:insert", body);
        }

        Object result = template.requestBody("direct:getColStats", "irrelevantBody");
        assertTrue(result instanceof Document, "Result is not of type Document");
        assertTrue(Document.class.cast(result).keySet().size() > 0, "The result should contain keys");
    }

    @Test
    public void testCommand() {
        // Call hostInfo, command working with every configuration
        Object result = template.requestBody("direct:command", "{\"hostInfo\":\"1\"}");
        assertTrue(result instanceof Document, "Result is not of type Document");
        assertTrue(Document.class.cast(result).keySet().size() > 0, "The result should contain keys");
    }

    @Test
    public void testOperationHeader() {
        // check that the count operation was invoked instead of the insert
        // operation
        Object result
                = template.requestBodyAndHeader("direct:insert", "irrelevantBody", MongoDbConstants.OPERATION_HEADER, "count");
        assertTrue(result instanceof Long, "Result is not of type Long");
        assertEquals(0L, result, "Test collection should not contain any records");

        // check that the count operation was invoked instead of the insert
        // operation
        result = template.requestBodyAndHeader("direct:insert", "irrelevantBody", MongoDbConstants.OPERATION_HEADER,
                MongoDbOperation.count);
        assertTrue(result instanceof Long, "Result is not of type Long");
        assertEquals(0L, result, "Test collection should not contain any records");

    }

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {

                from("direct:count").to(
                        "mongodb:myDb?database={{mongodb.testDb}}&collection={{mongodb.testCollection}}&operation=count&dynamicity=true");
                from("direct:insert")
                        .to("mongodb:myDb?database={{mongodb.testDb}}&collection={{mongodb.testCollection}}&operation=insert");
                from("direct:testStoreOidOnInsert")
                        .to("mongodb:myDb?database={{mongodb.testDb}}&collection={{mongodb.testCollection}}&operation=insert")
                        .setBody()
                        .header(MongoDbConstants.OID);
                from("direct:save")
                        .to("mongodb:myDb?database={{mongodb.testDb}}&collection={{mongodb.testCollection}}&operation=save");
                from("direct:testStoreOidOnSave")
                        .to("mongodb:myDb?database={{mongodb.testDb}}&collection={{mongodb.testCollection}}&operation=save")
                        .setBody()
                        .header(MongoDbConstants.OID);
                from("direct:update")
                        .to("mongodb:myDb?database={{mongodb.testDb}}&collection={{mongodb.testCollection}}&operation=update");
                from("direct:remove")
                        .to("mongodb:myDb?database={{mongodb.testDb}}&collection={{mongodb.testCollection}}&operation=remove");
                from("direct:aggregate").to(
                        "mongodb:myDb?database={{mongodb.testDb}}&collection={{mongodb.testCollection}}&operation=aggregate");
                from("direct:getDbStats").to("mongodb:myDb?database={{mongodb.testDb}}&operation=getDbStats");
                from("direct:getColStats").to(
                        "mongodb:myDb?database={{mongodb.testDb}}&collection={{mongodb.testCollection}}&operation=getColStats");
                from("direct:command").to("mongodb:myDb?database={{mongodb.testDb}}&operation=command");

            }
        };
    }

    @RouteFixture
    @Override
    public void createRouteBuilder(CamelContext context) throws Exception {
        context.addRoutes(createRouteBuilder());
    }
}
