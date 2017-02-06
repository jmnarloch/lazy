/**
 * Copyright (c) 2015-2017 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jmnarloch.util.lazy;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the {@link Lazy} class.
 *
 * @author Jakub Narloch
 */
public class LazyTest {

    @Before
    public void setUp() throws Exception {

        SimplePojo.resetInstanceCount();
        EmbeddedLazy.resetInstanceCount();
    }

    @Test
    public void shouldCreateLazyValue() {

        // given
        final Lazy<String> value = Lazy.create(() -> "Value");

        // when
        final String result = value.get();

        // then
        assertNotNull(result);
        assertEquals("Value", result);
    }

    @Test
    public void shouldEvaluateValueOnlyOnce() {

        // given
        final Lazy<SimplePojo> value = Lazy.create(() -> new SimplePojo("Value"));

        // when
        value.get();
        // and
        value.get();

        // then
        assertEquals(1, SimplePojo.getInstanceCount());
    }

    @Test
    public void shouldMapLazyValue() {

        // given
        final Lazy<SimplePojo> value = Lazy.create(() -> new SimplePojo("Value"));

        // when
        final Lazy<String> result = value.map(SimplePojo::getValue);

        // then
        assertNotNull(result);
        assertEquals("Value", result.get());
    }

    @Test
    public void shouldMapLazyValueWithLazyEvaluation() {

        // given
        final Lazy<SimplePojo> value = Lazy.create(() -> new SimplePojo("Value"));

        // when
        final Lazy<String> result = value.map(SimplePojo::getValue);

        // then
        assertNotNull(result);
        assertEquals(0, SimplePojo.getInstanceCount());
        assertEquals("Value", result.get());
        assertEquals(1, SimplePojo.getInstanceCount());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailToInitializeOnMappingNullValue() {

        // given
        final Lazy<SimplePojo> value = Lazy.create(() -> new SimplePojo(null));

        // then
        value.map(SimplePojo::getValue).get();
    }

    @Test
    public void shouldFlatMapLazyValueWithLazyEvaluation() {

        // given
        final Lazy<EmbeddedLazy<String>> value = Lazy.create(() -> new EmbeddedLazy<>(Lazy.create(() -> "Value")));

        // when
        final Lazy<String> result = value.flatMap(EmbeddedLazy::getValue);

        // then
        assertNotNull(result);
        assertEquals(0, EmbeddedLazy.getInstanceCount());
    }

    @Test
    public void shouldFlatMapLazyValue() {

        // given
        final Lazy<EmbeddedLazy<String>> value = Lazy.create(() -> new EmbeddedLazy<>(Lazy.create(() -> "Value")));

        // when
        final Lazy<String> result = value.flatMap(EmbeddedLazy::getValue);

        // then
        assertNotNull(result);
        assertEquals(0, EmbeddedLazy.getInstanceCount());
        assertEquals("Value", result.get());
        assertEquals(1, EmbeddedLazy.getInstanceCount());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnNullValue() {

        // given
        final Lazy<Object> value = Lazy.create(() -> null);

        // then
        value.get();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnSupplierError() {

        // given
        final Lazy<Object> value = Lazy.create(() -> {
            throw new RuntimeException();
        });

        // then
        value.get();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnConsecutiveCallsWhenFailed() {

        // given
        final Lazy<Object> value = Lazy.create(() -> {
            throw new RuntimeException();
        });

        // and
        try {
            value.get();
            fail();
        } catch (IllegalStateException e) {
            // should fail
        }

        // then
        value.get();
    }
}