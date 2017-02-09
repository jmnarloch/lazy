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
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A lazy reference to a object instance.
 *
 * <p>
 *
 * The instance of the object will be supplied by the {@link Supplier} instance and initialized on the first execution
 * of {@link #get()}. The initialization is guaranteed to performed only once.
 *
 * <p>
 *
 * In case of any error whenever the provided returns a {@code null} or throws a exception a
 * {@link IllegalStateException} will throw. Any consecutive call to {@link #get()} will throw
 * {@link IllegalStateException} as well.
 *
 * Thread safety: The implementation is thread safe.
 *
 * @author Jakub Narloch
 */
public interface Lazy<T> {

    /**
     * Retrieves the object instance.
     *
     * <p>
     *
     * If the value hasn't been initialized previously it will initialized upon first execution.
     *
     * @return the object instances
     */
    T get();

    /**
     * Applies the mapping function to the value.
     *
     * @param map the mapping function
     * @param <U> The result type
     * @return new lazy evaluated expression
     */
    default <U> Lazy<U> map(Function<T, U> map) {
        return create(() -> map.apply(get()));
    }

    /**
     * Applies the mapping function to the resulted {@link Lazy} value.
     *
     * @param map the mapping function
     * @param <U> The result type
     * @return new lazy evaluated expression
     */
    default <U> Lazy<U> flatMap(Function<T, Lazy<U>> map) {
        return create(() -> map.apply(get()).get());
    }

    /**
     * Creates new instance of lazy reference type.
     *
     * @param supplier the instance supplier
     * @param <T>      the instance type
     * @return the lazy reference to instance
     * @throws IllegalStateException if the supplier fails to provide a non-null a value, or the supplier
     *                               throws an exception
     */
    static <T> Lazy<T> create(Supplier<T> supplier) {
        return new LockFreeLazy<>(supplier);
    }

    class LockFreeLazy<T> implements Lazy<T> {

        private static final Object INITIALIZING = new Object();

        private static final Object ERROR = new Object();

        private static final AtomicReferenceFieldUpdater<LockFreeLazy, Object> instanceUpdater =
                AtomicReferenceFieldUpdater.newUpdater(LockFreeLazy.class, Object.class, "instance");

        private final Supplier<T> supplier;

        private volatile Object instance;

        LockFreeLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T get() {
            final Object value = getValue();
            checkError(value);
            if (isValueSet(value)) {
                return (T) value;
            }
            return initializeAndGet();
        }

        @SuppressWarnings("unchecked")
        private T initializeAndGet() {
            if (trySetValue()) {
                setValue();
            }
            final Object value = waitForValue();
            checkError(value);
            return (T) value;
        }

        private boolean isValueSet(Object value) {
            return Objects.nonNull(value) && value != INITIALIZING && value != ERROR;
        }

        @SuppressWarnings("unchecked")
        private boolean trySetValue() {
            return instanceUpdater.compareAndSet(this, null, INITIALIZING);
        }

        @SuppressWarnings("unchecked")
        private void setValue() {
            try {
                T newValue = supplier.get();
                if (Objects.isNull(newValue)) {
                    throw new IllegalStateException("Supplier failed to provide the value");
                }
                instanceUpdater.set(this, newValue);
            } catch (Exception ex) {
                instanceUpdater.set(this, ERROR);
                throw new IllegalStateException("Failed to initialize the lazy instance", ex);
            }
        }

        private Object getValue() {
            return instance;
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
