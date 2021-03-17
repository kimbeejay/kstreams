package com.github.kimbeejay.kstreams;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.time.Duration;
import java.util.Objects;

public class Configuration {

  private static final String ns = "kstreams";
  private static final String nsDebug = ns + ".debug";
  private static final String nsServers = ns + ".servers";

  private static final String nsConsumer = ns + ".consumer";
  private static final String nsConsumerGroup = nsConsumer + ".group";
  private static final String nsConsumerGroupName = nsConsumerGroup + ".name";
  private static final String nsConsumerKill = nsConsumer + ".kill";
  private static final String nsConsumerKillTimeout = nsConsumerKill + ".timeout";

  private static final String nsInput = ns + ".input";
  private static final String nsInputName = nsInput + ".name";
  private static final String nsInputPartitions = nsInput + ".partitions";
  private static final String nsInputReplication = nsInput + ".replication";
  private static final String nsInputReplicationFactor = nsInputReplication + ".factor";

  private static final String nsOutput = ns + ".output";
  private static final String nsOutputName = nsOutput + ".name";

  private final boolean isDebug;
  private final String servers;
  private final String consumerGroupName;
  private final Duration consumerKillTimeout;
  private final Topic input;
  private final Topic output;

  public Configuration() {
    this(ConfigFactory.load());
  }

  public Configuration(Config config) {
    Objects.requireNonNull(config);

    this.isDebug = config.getBoolean(nsDebug);
    this.servers = config.getString(nsServers);
    this.consumerGroupName = config.getString(nsConsumerGroupName);
    this.consumerKillTimeout = Duration.ofSeconds(config.getInt(nsConsumerKillTimeout));

    this.input = new Topic(
        config.getString(nsInputName),
        config.getInt(nsInputPartitions),
        config.getInt(nsInputReplicationFactor));

    this.output = new Topic(
        config.getString(nsOutputName));

    Objects.requireNonNull(this.servers);
    Objects.requireNonNull(this.consumerGroupName);
    Objects.requireNonNull(this.consumerKillTimeout);

    config.checkValid(ConfigFactory.defaultReference(), ns);
  }

  public boolean isDebug() {
    return isDebug;
  }

  public String getServers() {
    return servers;
  }

  public String getConsumerGroupName() {
    return consumerGroupName;
  }

  public Duration getConsumerKillTimeout() {
    return consumerKillTimeout;
  }

  public Topic getInput() {
    return input;
  }

  public Topic getOutput() {
    return output;
  }

  public static class Topic {
    final private String name;
    final private int partitions;
    final private int replicationFactor;

    public Topic(String name) {
      this(name, 1, 1);
    }

    public Topic(String name, int partitions, int replicationFactor) {
      Objects.requireNonNull(name);

      this.name = name;
      this.partitions = partitions;
      this.replicationFactor = replicationFactor;
    }

    public String getName() {
      return name;
    }

    public int getPartitions() {
      return partitions;
    }

    public int getReplicationFactor() {
      return replicationFactor;
    }
  }
}
