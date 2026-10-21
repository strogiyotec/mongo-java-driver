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

import com.mongodb.benchmark.benchmarks.BenchmarkSuite;
import com.mongodb.benchmark.framework.BenchmarkResultWriter;
import org.bson.Document;

/**
 * Runs the single and multi document benchmarks against the Reactive Streams driver.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class BenchmarkReactiveSuite extends BenchmarkSuite {

    public static void main(final String[] args) throws Exception {
        runBenchmarks();

        for (BenchmarkResultWriter writer : WRITERS) {
            writer.close();
        }
    }

    private static void runBenchmarks() throws Exception {
        runBenchmark(new ReactiveRunCommandBenchmark<>(DOCUMENT_CODEC));
        runBenchmark(new ReactiveFindOneBenchmark<Document>("./single_and_multi_document/tweet.json", DOCUMENT_CLASS));

        runBenchmark(new ReactiveInsertOneBenchmark<Document>("Small", "./single_and_multi_document/small_doc.json", 10_000,
                DOCUMENT_CLASS, ID_REMOVER));
        runBenchmark(new ReactiveInsertOneBenchmark<Document>("Large", "./single_and_multi_document/large_doc.json", 10,
                DOCUMENT_CLASS, ID_REMOVER));

        runBenchmark(new ReactiveFindManyBenchmark<Document>("./single_and_multi_document/tweet.json", DOCUMENT_CLASS));
        runBenchmark(new ReactiveInsertManyBenchmark<Document>("Small", "./single_and_multi_document/small_doc.json", 10_000,
                DOCUMENT_CLASS));
        runBenchmark(new ReactiveInsertManyBenchmark<Document>("Large", "./single_and_multi_document/large_doc.json", 10,
                DOCUMENT_CLASS));
    }
}
