package com.ssd.mvd.interfaces;

import com.ssd.mvd.inspectors.AvroSchemaInspector;
import org.apache.avro.generic.GenericRecord;

@SuppressWarnings(
        value = "хранит все методы нужные для объектов которые отправляются через Кафку"
)
public interface KafkaEntitiesCommonMethods {
    @lombok.NonNull
    com.ssd.mvd.kafka.kafkaConfigs.KafkaTopics getTopicName();

    @lombok.NonNull
    String getSuccessMessage();

    @lombok.NonNull
    default GenericRecord getEntityRecord() {
        return AvroSchemaInspector.generateGenericRecord( this );
    }

    @lombok.NonNull
    default String generateMessage() {
        return "Kafka got request for topic: " + this.getTopicName();
    }
}
