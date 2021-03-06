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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple test object type with the embedded {@link Lazy} type.
 *
 * @param <T> the value type
 *
 * @author Jakub Narloch
 */
public class EmbeddedLazy<T> {

    private static final AtomicInteger INSTANCES = new AtomicInteger(0);

    private final Lazy<T> value;

    public EmbeddedLazy(Lazy<T> value) {
        this.value = value;
        INSTANCES.incrementAndGet();
    }

    public Lazy<T> getValue() {
        return value;
    }

    public static int getInstanceCount() {
        return INSTANCES.get();
    }

    public static void resetInstanceCount() {
        INSTANCES.set(0);
    }
}
