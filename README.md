# Java 8 Lazy type

> General purpose, thread safe and high performance lazy type

[![Build Status](https://travis-ci.org/jmnarloch/lazy.svg?branch=master)](https://travis-ci.org/jmnarloch/lazy)
[![Coverage Status](https://coveralls.io/repos/jmnarloch/lazy/badge.svg?branch=master&service=github)](https://coveralls.io/github/jmnarloch/lazy?branch=master)

## Setup

Add the library to your project:

```xml
<dependency>
  <groupId>io.jmnarloch</groupId>
  <artifactId>lazy</artifactId>
  <version>1.0.0</version>
</dependency>
```
 
## Usage

Usage is really simple, you can create a lazy reference to any object. The instance is being provided by a `Supplier`.
The `Supplier` will be evaluated upon the first call of the `get` method.

```java
final Lazy<ComplexObject> value = Lazy.create(() -> new ComplexObject());

// get the object instance
ComplexObject object = value.get();
```

The implementation is guaranteed to be thread safe.

## Performance

The actual `Lazy` type implementation uses lock free implementation giving it very good performance compared to lock 
variant whenever it uses the `synchronized` Java keyword or directly `Lock` type.

| Benchmark | Mode | Cnt | Score | Error | Units |
| LazyBenchmark.benchmarkGet | thrpt | 20 | 271440905,189 ± 2721909,603 | ops/s |
| LazyBenchmark.benchmarkInitializeAndGet | thrpt | 20 | 274012023,081 ± 1720484,338 | ops/s |

## License

Apache 2.0
