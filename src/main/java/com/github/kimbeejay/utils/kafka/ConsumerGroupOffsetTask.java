package com.github.kimbeejay.utils.kafka;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ConsumerGroupOffsetTask {
  private static final Logger logger = LoggerFactory.getLogger(TopicCreateTask.class);

  public static void main(String[] args) {
    String topicName = "";
    long offset = 0;

    if (args.length != 2) {
      logger.info("Arguments must be provided: topicName offset");
      System.exit(0);
    }

    topicName = args[0].trim();
    offset = Math.max(0, Long.parseLong(args[1]));

    if (topicName.length() == 0) {
      logger.info("Arguments must be provided: topicName offset");
      System.exit(0);
    }

    Config config = ConfigFactory.load();
    KafkaConsumer<byte[], byte[]> konsumer =
        new KafkaConsumer<>(getDefaultConsumerProperties(config));
    Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();

    List<PartitionInfo> partitions = konsumer.partitionsFor(topicName);
    if (partitions.size() == 0) {
      logger.info("Could not find any partitions for topic: " + topicName);
      System.exit(1);
    }

    for (PartitionInfo partition : partitions) {
      offsets.put(
          new TopicPartition(partition.topic(), partition.partition()),
          new OffsetAndMetadata(offset));
    }

    konsumer.commitSync(offsets);
    logger.info(
        "Offsets were re-set to '"
            + offset
            + "' for topic '"
            + topicName
            + "' in '"
            + partitions.size()
            + "' partitions.");
  }

  protected static Properties getDefaultConsumerProperties(@NotNull Config config) {
    Properties props = new Properties();
    props.put("bootstrap.servers", config.getString("kstreams.servers"));
    props.put("group.id", config.getString("kstreams.consumer.group.name"));
    props.put("enable.auto.commit", true);
    props.put("session.timeout.ms", "30000");
    props.put("auto.offset.reset", "earliest");
    props.put("key.deserializer", ByteArrayDeserializer.class.getName());
    props.put("value.deserializer", ByteArrayDeserializer.class.getName());

    return props;
  }
}
