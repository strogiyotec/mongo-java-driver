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

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoNamespace;
import com.mongodb.benchmark.framework.Benchmark;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.CountDownLatch;

/**
 * Base class for benchmarks of the Reactive Streams driver.
 *
 * <p>Publishers are awaited with a plain {@link Subscriber} rather than a Reactor operator so that the measured
 * Reactor code is only the code used internally by the driver.</p>
 */
public abstract class AbstractReactiveMongoBenchmark extends Benchmark {

    protected static final String DATABASE_NAME = "perftest";
    protected static final String COLLECTION_NAME = "corpus";
    protected static final MongoNamespace NAMESPACE = new MongoNamespace(DATABASE_NAME, COLLECTION_NAME);
    protected MongoClientSettings mongoClientSettings;

    protected MongoClient client;

    protected AbstractReactiveMongoBenchmark(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        if (mongoClientSettings != null) {
            client = MongoClients.create(mongoClientSettings);
        } else {
            client = MongoClients.create();
        }
    }

    @Override
    public void tearDown() throws Exception {
        client.close();
    }

    public AbstractReactiveMongoBenchmark applyMongoClientSettings(final MongoClientSettings mongoClientSettings) {
        this.mongoClientSettings = mongoClientSettings;
        return this;
    }

    /**
     * Subscribes to the publisher, requests all items, and blocks until it terminates.
     *
     * @return the number of items received
     */
    protected static long await(final Publisher<?> publisher) throws InterruptedException {
        AwaitingSubscriber subscriber = new AwaitingSubscriber();
        publisher.subscribe(subscriber);
        subscriber.latch.await();
        if (subscriber.error != null) {
            throw new RuntimeException(subscriber.error);
        }
        return subscriber.count;
    }

    private static final class AwaitingSubscriber implements Subscriber<Object> {
        private final CountDownLatch latch = new CountDownLatch(1);
        private volatile long count;
        private volatile Throwable error;

        @Override
        public void onSubscribe(final Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        @SuppressWarnings("NonAtomicOperationOnVolatileField") // signals are serial per the Reactive Streams spec
        public void onNext(final Object item) {
            count++;
        }

        @Override
        public void onError(final Throwable throwable) {
            error = throwable;
            latch.countDown();
        }

        @Override
        public void onComplete() {
            latch.countDown();
        }
    }
}
