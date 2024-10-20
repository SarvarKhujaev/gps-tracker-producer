package com.ssd.mvd.kafka.kafkaConfigs;

import com.ssd.mvd.inspectors.LogInspector;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

public final class KafkaProducerInterceptor extends LogInspector implements ProducerInterceptor< String, byte[] > {
    public KafkaProducerInterceptor() {
        super( KafkaProducerInterceptor.class );
    }

    @Override
    public ProducerRecord< String, byte[] > onSend(
            @lombok.NonNull final ProducerRecord< String, byte[] > producerRecord
    ) {
        producerRecord.headers().add(
                KafkaProducerInterceptor.class.getName(),
                KafkaProducerInterceptor.class.getName().getBytes()
        );

        return producerRecord;
    }

    @Override
    public void onAcknowledgement(
            @lombok.NonNull final RecordMetadata recordMetadata,
            final Exception e
    ) {
        super.logging(
                String.join(
                        SPACE_WITH_DOUBLE_DOTS,
                        "Message was sent to topic",
                        recordMetadata.topic(),
                        "and offset",
                        String.valueOf( recordMetadata.offset() ),
                        "and timestamp",
                        String.valueOf( recordMetadata.offset() )
                )
        );
    }

    @Override
    public void close() {
        super.logging( KafkaProducerInterceptor.class );
    }

    @Override
    public void configure( final Map< String, ? > map ) {}
}
