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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 */
public interface Lazy<T> {

    T get();

    default <U> Lazy<U> map(Function<T, U> map) {
        return create(() -> map.apply(get()));
    }

    default <U> Lazy<U> flatMap(Function<T, Lazy<U>> map) {
        return create(() -> map.apply(get()).get());
    }

    static <T> Lazy<T> create(Supplier<T> supplier) {
        return new LockFreeLazy<>(supplier);
    }

    class LockFreeLazy<T> implements Lazy<T> {

        private static final Object INITIALIZING = new Object();

        private static final Object ERROR = new Object();

        private final Supplier<T> supplier;

        private final AtomicReference instance = new AtomicReference();

        LockFreeLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T get() {
            Object value = getValue();
            checkError(value);
            if (isValueSet(value)) {
                return (T) value;
            }

            if (trySetValue()) {
                setValue();
            }

            value = waitForValue();
            checkError(value);
            return (T) value;
        }

        private boolean isValueSet(Object value) {
            return value != null && value != INITIALIZING && value != ERROR;
        }

        @SuppressWarnings("unchecked")
        private boolean trySetValue() {
            return instance.compareAndSet(null, INITIALIZING);
        }

        @SuppressWarnings("unchecked")
        private void setValue() {
            try {
                T newValue = supplier.get();
                if (Objects.isNull(newValue)) {
                    throw new IllegalStateException("Supplier failed to provide the value");
                }
                instance.set(newValue);
            } catch (Exception ex) {
                instance.set(ERROR);
                throw new IllegalStateException("Failed to initialize the lazy instance", ex);
            }
        }

        private Object getValue() {
            return instance.get();
        }

        private Object waitForValue() {
            Object value;
            do {
                value = getValue();
            } while (value == INITIALIZING);
            return value;

        }

        private void checkError(Object value) {
            if (value == ERROR) {
                throw new IllegalStateException("Supplier failed to initialize the value");
            }
        }
    }
}
