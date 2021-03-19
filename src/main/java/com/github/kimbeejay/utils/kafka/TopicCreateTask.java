package com.github.kimbeejay.utils.kafka;

import com.jasongoodwin.monads.Try;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class TopicCreateTask {
  private static final Logger logger = LoggerFactory.getLogger(TopicCreateTask.class);

  public static void main(String[] args) {
    if (args.length == 0) {
      logger.info("At least 1 topic should be provided to add it to the Kafka cluster");
      System.exit(0);
    }

    Config config = ConfigFactory.load();
    Properties props = new Properties();
    props.put("bootstrap.servers", config.getString("kstreams.servers"));

    AdminClient client = AdminClient.create(props);
    final Map<String, NewTopic> topics = new HashMap<>();
    NewTopic topic;

    for (String topicName : args) {
      topic =
          new NewTopic(
              topicName,
              Integer.parseInt(config.getString("kstreams.input.partitions")),
              Short.parseShort(config.getString("kstreams.input.replication.factor")));
      topic.configs(getDefaultConfigs());
      topics.put(topicName, topic);
    }

    try {
      logger.info("Starting Kafka topic creation process");

      CreateTopicsResult result = client.createTopics(topics.values());
      result
          .values()
          .forEach(
              (s, future) -> {
                NewTopic t = topics.get(s);
                future.whenComplete(
                    (aVoid, error) ->
                        Optional.ofNullable(error)
                            .map(Try::failure)
                            .orElse(Try.successful(null))
                            .onFailure(
                                throwable ->
                                    logger.error("Topic creation didn't complete:", throwable))
                            .onSuccess(
                                o ->
                                    logger.info(
                                        String.format(
                                            "Topic %s, has been successfully created with %s partitions and replicated %s times",
                                            t.name(),
                                            t.numPartitions(),
                                            t.replicationFactor() - 1))));
              });

      result.all().get();
    } catch (InterruptedException | ExecutionException e) {
      if (!(e.getCause() instanceof TopicExistsException)) {
        e.printStackTrace();
      }
    } finally {
      client.close();
    }
  }

  protected static Map<String, String> getDefaultConfigs() {
    Map<String, String> configs = new HashMap<>();
    configs.put("cleanup.policy", "delete");
    configs.put("max.message.bytes", "1000012");
    configs.put("min.insync.replicas", "1");
    configs.put("retention.bytes", "-1");
    configs.put("retention.ms", "14515200000");
    configs.put("segment.index.bytes", "10485760");
    configs.put("compression.type", "snappy");
    return configs;
  }
}
