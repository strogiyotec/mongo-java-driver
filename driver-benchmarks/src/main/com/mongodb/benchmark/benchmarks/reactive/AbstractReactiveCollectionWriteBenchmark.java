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
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.json.JsonReader;

import java.nio.charset.StandardCharsets;

public abstract class AbstractReactiveCollectionWriteBenchmark<T> extends AbstractReactiveMongoBenchmark {
    private final String resourcePath;
    private final Class<T> clazz;
    private byte[] bytes;
    protected int fileLength;
    protected T document;
    protected int numInternalIterations;
    protected int numDocuments;
    protected MongoCollection<T> collection;
    protected MongoDatabase database;

    protected AbstractReactiveCollectionWriteBenchmark(final String name, final String resourcePath, final int numInternalIterations,
                                                       final int numDocuments, final Class<T> clazz) {
        super(name);
        this.resourcePath = resourcePath;
        this.clazz = clazz;
        this.numInternalIterations = numInternalIterations;
        this.numDocuments = numDocuments;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        bytes = readAllBytesFromRelativePath(resourcePath);
        fileLength = bytes.length;
        document = createDocument();
        database = client.getDatabase(DATABASE_NAME);
        collection = database.getCollection(COLLECTION_NAME, clazz);
        await(database.drop());
    }

    @Override
    public void before() throws Exception {
        super.before();
        await(collection.drop());
    }

    protected T createDocument() {
        Codec<T> codec = client.getDatabase(DATABASE_NAME).getCodecRegistry().get(clazz);
        return codec.decode(new JsonReader(new String(bytes, StandardCharsets.UTF_8)), DecoderContext.builder().build());
    }

    @Override
    public int getBytesPerRun() {
        return fileLength * numInternalIterations * numDocuments;
    }
}
