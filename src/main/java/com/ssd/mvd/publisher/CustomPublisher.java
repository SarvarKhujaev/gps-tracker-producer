package com.ssd.mvd.publisher;

import com.ssd.mvd.interfaces.KafkaEntitiesCommonMethods;
import com.ssd.mvd.inspectors.AnnotationInspector;

import org.apache.kafka.clients.producer.ProducerRecord;

import org.reactivestreams.Subscription;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Publisher;

@SuppressWarnings(
        value = """
                кастомный Publisher, который принимает сообщение и название топика для Кафки

                А также отслеживает состояние передачи сообщения и обрабатывает ошибки или сообщение об удаче
                """
)
@com.ssd.mvd.annotations.ImmutableEntityAnnotation
@com.ssd.mvd.annotations.ServiceParametrAnnotation( propertyGroupName = "KAFKA_VARIABLES.KAFKA_TOPICS" )
public final class CustomPublisher implements Publisher< ProducerRecord< String, byte[] > > {
    private final ProducerRecord< String, byte[] > producerRecord;

    @lombok.NonNull
    @org.jetbrains.annotations.Contract( value = "_ -> !null" )
    public static CustomPublisher from (
            @lombok.NonNull final KafkaEntitiesCommonMethods kafkaEntitiesCommonMethods
    ) {
        return new CustomPublisher( kafkaEntitiesCommonMethods );
    }

    private CustomPublisher(
            final KafkaEntitiesCommonMethods kafkaEntitiesCommonMethods
    ) {
        this.producerRecord = new ProducerRecord<>(
                AnnotationInspector.getVariable(
                        CustomPublisher.class,
                        kafkaEntitiesCommonMethods.getTopicName().name()
                ),
                kafkaEntitiesCommonMethods.getEntityRecord().toString().getBytes()
        );
    }

    @Override
    public void subscribe( @lombok.NonNull final Subscriber subscriber ) {
        subscriber.onSubscribe( new Subscription() {
            @Override
            public void request( final long l ) {
                subscriber.onNext( producerRecord );
                subscriber.onComplete();
            }

            @Override
            public void cancel() {
                subscriber.onError( new Exception( "Message was not sent!!!" ) );
            }
        } );
    }
}
