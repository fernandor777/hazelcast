/*
 * Copyright (c) 2008-2026, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.jet.json.impl;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.hazelcast.config.MapConfig;
import com.hazelcast.jet.json.JsonUtil;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.ParallelJVMTest;
import com.hazelcast.test.annotation.QuickTest;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.hazelcast.jet.json.impl.JsonUtilImpl.JSON_BLOCKLIST_DEFAULTS_DISABLED_PROPERTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

@RunWith(HazelcastParallelClassRunner.class)
@Category({QuickTest.class, ParallelJVMTest.class})
public class JsonUtilSecurityTest extends HazelcastTestSupport {

    @Test
    public void beanFrom_rejectsBlocklistedClass() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> JsonUtil.beanFrom("{}", MapConfig.class));

        assertMessageContains(exception.getMessage(), "cannot be deserialized using JSON");
    }

    @Test
    public void beanFrom_rejectsBlocklistedNestedField() {
        JSONObjectException exception = assertThrows(JSONObjectException.class,
                () -> JsonUtil.beanFrom("{\"config\":{}}", Holder.class));

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertMessageContains(exception.getCause().getMessage(), "MapConfig cannot be deserialized using JSON");
    }

    @Test
    public void beanFrom_rejectsBlocklistedArrayElementClass() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> JsonUtil.beanFrom("[{}]", MapConfig[].class));

        assertMessageContains(exception.getMessage(), "cannot be deserialized using JSON");
    }

    @Test
    public void listFrom_rejectsBlocklistedElementClass() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> JsonUtil.listFrom("[{}]", MapConfig.class));

        assertMessageContains(exception.getMessage(), "cannot be deserialized using JSON");
    }

    @Test
    public void beanSequenceFrom_rejectsBlocklistedElementClass() throws IOException {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> JsonUtil.beanSequenceFrom(new java.io.StringReader("{}"), MapConfig.class).next());

        assertMessageContains(exception.getMessage(), "cannot be deserialized using JSON");
    }

    @Test
    public void toJson_rejectsBlocklistedClass() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> JsonUtil.toJson(new MapConfig()));

        assertMessageContains(exception.getMessage(), "cannot be serialized using JSON");
    }

    @Test
    public void toJson_rejectsDataSourceSubclass() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> JsonUtil.toJson(new HikariDataSource()));

        assertMessageContains(exception.getMessage(), "cannot be serialized using JSON");
    }

    @Test
    public void allowedClassCanRoundTrip() throws IOException {
        String json = JsonUtil.toJson(new SafeDto("safe"));
        SafeDto dto = JsonUtil.beanFrom(json, SafeDto.class);

        assertEquals("safe", dto.value);
    }

    @Test
    public void defaultsCanBeDisabledForAnExplicitJsonInstance() throws IOException {
        String previous = System.getProperty(JSON_BLOCKLIST_DEFAULTS_DISABLED_PROPERTY);
        System.setProperty(JSON_BLOCKLIST_DEFAULTS_DISABLED_PROPERTY, "true");
        try {
            JSON json = JSON.builder().register(new JacksonJrFilteringExtension()).build();
            assertNotNull(json.beanFrom(MapConfig.class, "{}"));
            assertNotNull(json.asString(new MapConfig()));
        } finally {
            if (previous == null) {
                System.clearProperty(JSON_BLOCKLIST_DEFAULTS_DISABLED_PROPERTY);
            } else {
                System.setProperty(JSON_BLOCKLIST_DEFAULTS_DISABLED_PROPERTY, previous);
            }
        }
    }

    private static void assertMessageContains(String actual, String expected) {
        org.junit.Assert.assertTrue("Expected <" + actual + "> to contain <" + expected + ">",
                actual.contains(expected));
    }

    public static class SafeDto {
        public String value;

        public SafeDto() {
        }

        SafeDto(String value) {
            this.value = value;
        }
    }

    public static class Holder {
        public MapConfig config;
    }
}
