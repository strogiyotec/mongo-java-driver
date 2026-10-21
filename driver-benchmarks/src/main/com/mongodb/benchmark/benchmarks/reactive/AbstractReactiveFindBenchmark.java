/*
 * Copyright 2008-present MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongodb.benchmark.benchmarks.reactive;

import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.json.JsonReader;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractReactiveFindBenchmark<T> extends AbstractReactiveMongoBenchmark {
    protected MongoCollection<T> collection;

    private final String resourcePath;
    private final Class<T> clazz;

    private int fileLength;

    protected AbstractReactiveFindBenchmark(final String name, final String resourcePath, final Class<T> clazz) {
        super(name);
        this.resourcePath = resourcePath;
        this.clazz = clazz;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        collection = client.getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAME, clazz);
        byte[] bytes = readAllBytesFromRelativePath(resourcePath);

        fileLength = bytes.length;

        MongoDatabase setUpDatabase = client.getDatabase(DATABASE_NAME);
        await(setUpDatabase.drop());

        BsonDocument document = new BsonDocumentCodec().decode(new JsonReader(new String(bytes, StandardCharsets.UTF_8)),
                DecoderContext.builder().build());
        List<BsonDocument> documents = new ArrayList<>(NUM_INTERNAL_ITERATIONS);
        for (int i = 0; i < NUM_INTERNAL_ITERATIONS; i++) {
            BsonDocument copy = document.clone();
            copy.put("_id", new BsonInt32(i));
            documents.add(copy);
        }
        await(setUpDatabase.getCollection(COLLECTION_NAME, BsonDocument.class).insertMany(documents));
    }

    @Override
    public int getBytesPerRun() {
        return fileLength * NUM_INTERNAL_ITERATIONS;
    }
}
