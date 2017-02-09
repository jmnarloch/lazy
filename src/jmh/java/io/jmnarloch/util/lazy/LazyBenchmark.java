package io.jmnarloch.util.lazy;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class LazyBenchmark {

    private Lazy<String> initializedLazy;

    private Lazy<String> uninitializedLazy;

    @Setup
    public void before() {

        initializedLazy = Lazy.create(() -> "Initialized");
        initializedLazy.get();

        uninitializedLazy = Lazy.create(() -> "Uninitialized");
    }

    @Benchmark
    public String benchmarkGet() {
        return initializedLazy.get();
    }

    @Benchmark
    public String benchmarkInitializeAndGet() {
        return uninitializedLazy.get();
    }
}
