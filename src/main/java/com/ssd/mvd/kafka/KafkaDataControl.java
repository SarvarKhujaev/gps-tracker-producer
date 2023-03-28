package com.ssd.mvd.kafka;

import java.util.*;
import java.util.logging.Logger;
import java.util.function.Supplier;
import java.util.function.Predicate;

import com.ssd.mvd.netty.Position;
import com.ssd.mvd.GpsTrackerApplication;

import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

@lombok.Data
public class KafkaDataControl {
    private static KafkaDataControl instance = new KafkaDataControl();
    private final Logger logger = Logger.getLogger( KafkaDataControl.class.toString() );

    private final Predicate< Position > checkPosition = position ->
            position.getLatitude() > 0
            && position.getLongitude() > 0
            && position.getDeviceTime().after( new Date( 1605006666774L ) );

    private final String KAFKA_BROKER = GpsTrackerApplication
            .context
            .getEnvironment()
            .getProperty( "variables.KAFKA_BROKER" );

    private final String GROUP_ID_FOR_KAFKA = GpsTrackerApplication
            .context
            .getEnvironment()
            .getProperty( "variables.GROUP_ID_FOR_KAFKA" );

    // топик для сырых данных перед обработкой для продакшена
    private final String RAW_GPS_LOCATION_TOPIC_PROD = GpsTrackerApplication
            .context
            .getEnvironment()
            .getProperty( "variables.RAW_GPS_LOCATION_TOPIC_PROD" );

    // топик для сырых данных перед обработкой для дева
    private final String RAW_GPS_LOCATION_TOPIC_DEV = GpsTrackerApplication
            .context
            .getEnvironment()
            .getProperty( "variables.RAW_GPS_LOCATION_TOPIC_DEV" );

    public static KafkaDataControl getInstance () { return instance != null ? instance : ( instance = new KafkaDataControl() ); }

    private final Supplier< Map< String, Object > > getKafkaSenderOptions = () -> Map.of(
            ProducerConfig.ACKS_CONFIG, "-1",
            ProducerConfig.MAX_BLOCK_MS_CONFIG, 33554432 * 20,
            ProducerConfig.CLIENT_ID_CONFIG, this.getGROUP_ID_FOR_KAFKA(),
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.getKAFKA_BROKER(),
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class );

    private final KafkaSender< String, String > kafkaSender = KafkaSender.create(
            SenderOptions.< String, String >create( this.getGetKafkaSenderOptions().get() )
                    .maxInFlight( 1024 ) );

    private KafkaDataControl () { this.getLogger().info( "KafkaDataControl was created" ); }

    public void writeToKafka ( Position position ) {
            this.getKafkaSender()
                    .createOutbound()
                    .send( Mono.just( new ProducerRecord<>(
                            this.getRAW_GPS_LOCATION_TOPIC_PROD(), SerDes
                            .getSerDes()
                            .serialize( position ) ) ) )
                    .then()
                    .doOnError( throwable -> this.getLogger().info( "Error: " + throwable ) )
                    .doOnSuccess( success -> this.getLogger().info( "Kafka got: " + position.getDeviceId() +
                                    " at: " + position.getDeviceTime()
                                    + " for PROD" ) )
                    .subscribe();

            this.getKafkaSender()
                    .createOutbound()
                    .send( Mono.just( new ProducerRecord<>(
                            this.getRAW_GPS_LOCATION_TOPIC_DEV(), SerDes
                            .getSerDes()
                            .serialize( position ) ) ) )
                    .then()
                    .doOnError( throwable -> this.getLogger().info( "Error: " + throwable ) )
                    .doOnSuccess( success -> this.getLogger().info( "Kafka got: " + position.getDeviceId() +
                            " at: " + position.getDeviceTime()
                            + " for DEV" ) )
                    .subscribe(); }

    public void clear () {
        this.logger.info( "Kafka was closed" );
        this.getKafkaSender().close();
        instance = null; }
}
