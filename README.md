Base module that built to avoid writing boilerplate source code for each Kafka Stream application.
Currently, it wraps:
  - basic kafka connection and in/out streams configuration. Some `var`s are exposed to be overwritten via `ENV` variables;
  - KafkaStreams builder that contains topology definition etc.

### TOC
  0. [Prerequisites](#prerequisites)
  1. [Dependencies](#dependencies)
  2. [Exposed `ENV` variables](#exposed-env-variables)
  3. [How to](#how-to)
     - [Configure](#configure)
     - [Implement](#implement)
     - [Build](#build)
     

#### Prerequisites
  - [IntelliJ IDEA](https://www.jetbrains.com/idea/)
  - Java 1.8 (OpenJDK 11 is recommended)

#### Dependencies
  - [com.github.johnrengelman.shadow](https://github.com/johnrengelman/shadow); version "6.1.0"
  - [org.jetbrains:annotations](https://github.com/JetBrains/java-annotations); version: "20.1.0"
  - [com.typesafe:config](https://github.com/lightbend/config); version: "1.4.1"
  - [org.apache.kafka:kafka-streams](https://kafka.apache.org/documentation/streams/); version: "2.5.0"

#### Exposed `ENV` variables
| Var | Type | Default | Description |
| --- | ---- | ------- | ----------- |
| `KSTREAMS_DEBUG` | `boolean` | `false` | |
| `KSTREAMS_CONSUMER_GROUP_NAME` | `string` | `consumer` | Kafka consumer group name |
| `KSTREAMS_SERVERS` | `string` | `localhost:29092` | Kafka broker addresses |
| `KSTREAMS_INPUT_NAME` | `string` | `in` | Where from (topic name) we gonna get stream data |
| `KSTREAMS_INPUT_PARTITIONS` | `int` | `1` | Number of desired partitions |
| `KSTREAMS_INPUT_REPLICATION_FACTOR` | `int` | `1` | Number of desired replication factor |
| `KSTREAMS_OUTPUT_NAME` | `string` | `out` | Where to (topic name) we gonna put stream data |

#### How to
##### Configure
Default configuration properties could be overwritten in 2 ways: `application.conf` or `ENV` variables.
In `application.conf` you could define only those variables you want to change while implementation this module.
Or maybe you might change `ENV` variables while running your application.
Look at exposed `ENV` variables above or `reference.conf` in `resources`.

##### Implement
Implementation is pretty simple. All you need is ~~love~~ to extend abstract class `StreamWrapper` and describe your `Topology`.

```java
public class Stream extends BaseKStreams {

  @Override
  public @NotNull
  Topology getTopology() {
    final StreamsBuilder builder = new StreamsBuilder();

    builder
        .stream(
            getConfig().getInput().getName(),
            Consumed.with(Serdes.String(), Serdes.ByteArray()))
        .map((key, value) -> KeyValue.pair(key, Arrays.copyOf(value, 8, value.length)))
        .to(
            getConfig().getOutput().getName(),
            Produced.with(Serdes.String(), Serdes.ByteArray()));

    return builder.build();
  }
}
```

After that step, we could start it.
```java
public class Application {

  public static void main(String[] args) {
    final CountDownLatch latch = new CountDownLatch(1);
    final Stream stream = new Stream();

    // intercept SIGTERM
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread("streams-shutdown-hook") {
              @Override
              public void run() {
                stream.close();
                latch.countDown();
              }
            });

    try {
      stream.getStreams().start();
      latch.await();
    } catch (Throwable e) {
      e.printStackTrace();
      System.exit(1);
    }

    System.exit(0);
  }
}
```

##### Build
Ensure that you already have `gradle.properties` and defined `gitlab_token` and `gitlab_project`.
All other stuff `Gradle` and `make` will do for us, just
```bash
make
```
