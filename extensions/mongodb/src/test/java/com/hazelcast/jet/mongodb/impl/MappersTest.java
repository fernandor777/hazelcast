/*
 * Copyright 2024 Hazelcast Inc.
 *
 * Licensed under the Hazelcast Community License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://hazelcast.com/hazelcast-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.jet.mongodb.impl;

import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.annotation.ParallelJVMTest;
import com.hazelcast.test.annotation.QuickTest;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import org.bson.Document;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(HazelcastParallelClassRunner.class)
@Category({QuickTest.class, ParallelJVMTest.class})
public class MappersTest {

    @Test
    public void bsonClassMapperIsNotSerializableAndStillMapsDocuments() {
        Function<Document, Person> mapper = Mappers.toClass(Person.class);

        assertFalse(mapper instanceof Serializable);
        assertEquals("Ada", mapper.apply(new Document("name", "Ada")).name);
    }

    @Test
    public void bsonStreamMapperIsNotSerializable() {
        BiFunction<ChangeStreamDocument<Document>, Long, Person> mapper = Mappers.streamToClass(Person.class);

        assertFalse(mapper instanceof Serializable);
    }

    public static class Person {
        public String name;

        public Person() {
        }
    }
}
