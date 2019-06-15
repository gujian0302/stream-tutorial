package com.guguji.kstream;

import java.util.Arrays;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;

public class WorkCountApplication {

  public static void main(String[] args) {
    Properties config = new Properties();
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "workcount-application");
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

    final Serde<String> stringSerde = Serdes.String();

    StreamsBuilder builder = new StreamsBuilder();
    KStream<String, String> textLines = builder.stream("streams-plaintext-input", Consumed.with(stringSerde, stringSerde));
    KTable<String, Long> wordCounts = textLines
        .flatMapValues(textLine -> Arrays.asList(textLine.toLowerCase().split("\\W+")))
        .groupBy((key, work) -> work).count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("counts-store"));

    wordCounts.toStream().to("WordsWithCountsTopics", Produced.with(Serdes.String(), Serdes.Long()));
    KafkaStreams streams = new KafkaStreams(builder.build(), config);
    streams.start();
  }

}
