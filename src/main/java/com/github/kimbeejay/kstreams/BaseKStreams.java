package com.github.kimbeejay.kstreams;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.DefaultProductionExceptionHandler;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;
import java.util.Properties;

public abstract class BaseKStreams {

  private final Configuration config;
  private final KafkaStreams streams;

  public BaseKStreams() {
    this(new Configuration());
  }

  public BaseKStreams(@NotNull Configuration config) {
    Objects.requireNonNull(config);

    this.config = config;
    this.streams = new KafkaStreams(getTopology(), buildProps());
  }

  @NotNull
  public abstract Topology getTopology();

  protected Properties buildProps() {
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, getConfig().getConsumerGroupName());
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, getConfig().getServers());
    props.put(
        StreamsConfig.DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG,
        DefaultProductionExceptionHandler.class.getName());

    if (getConfig().isDebug()) {
      props.put(ProducerConfig.LINGER_MS_CONFIG, "100");
      props.put(ProducerConfig.BATCH_SIZE_CONFIG, "163840");
    }

    return props;
  }

  public void close() {
    close(null);
  }

  public void close(Duration duration) {
    if (Objects.isNull(duration)) {
      duration = getConfig().getConsumerKillTimeout();
    }

    getStreams().close(duration);
  }

  @NotNull
  public KafkaStreams getStreams() {
    return this.streams;
  }

  @NotNull
  public Configuration getConfig() {
    return this.config;
  }
}
