package com.ssd.mvd.kafka;

import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.logging.Logger;
import java.util.function.Predicate;
import java.util.concurrent.ExecutionException;

import com.ssd.mvd.netty.Position;
import com.ssd.mvd.GpsTrackerApplication;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Data
public class KafkaDataControl {
    private Properties properties;
    private final AdminClient client;
    private final KafkaTemplate< String, String > kafkaTemplate;
    private static KafkaDataControl instance = new KafkaDataControl();
    private final Logger logger = Logger.getLogger( KafkaDataControl.class.toString() );

    private final Predicate< Position > checkPosition = position -> position.getLatitude() > 0
            && position.getLongitude() > 0
            && position.getDeviceTime()
            .after( new Date( 1605006666774L ) );

    private final String KAFKA_BROKER = GpsTrackerApplication
            .context
            .getEnvironment()
            .getProperty( "variables.KAFKA_BROKER" );

    private final String ID = GpsTrackerApplication
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

    private Properties setProperties () {
        this.setProperties( new Properties() );
        this.getProperties().put( AdminClientConfig.CLIENT_ID_CONFIG, this.getID() );
        this.getProperties().put( AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.getKAFKA_BROKER() );
        return getProperties(); }

    private void getNewTopic ( String imei ) {
        try { if ( !this.getClient().listTopics().names().get().contains( imei ) ) {
                this.logger.info( "Topic: " + imei + " was created" );
                this.getClient().createTopics( Collections.singleton(
                        TopicBuilder
                                .name( imei )
                                .partitions( 5 )
                                .replicas( 3 )
                                .build() ) );
        } } catch ( ExecutionException | InterruptedException e ) { throw new RuntimeException(e); } }

    private KafkaTemplate< String, String > kafkaTemplate () {
        Map< String, Object > map = new HashMap<>();
        map.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.KAFKA_BROKER);
        map.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class );
        map.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class );
        return new KafkaTemplate<>( new DefaultKafkaProducerFactory<>( map ) ); }

    private KafkaDataControl () {
        this.kafkaTemplate = this.kafkaTemplate();
        this.logger.info( "KafkaDataControl was created" );
        this.client = KafkaAdminClient.create( this.setProperties() );
        this.getNewTopic( this.getRAW_GPS_LOCATION_TOPIC_PROD() );
        this.getNewTopic( this.getRAW_GPS_LOCATION_TOPIC_DEV() ); }

    public void writeToKafka ( Position position ) {
        Mono.just( position )
                .filter( this.getCheckPosition() )
                .subscribe( position1 -> {
                    this.getKafkaTemplate().send( this.getRAW_GPS_LOCATION_TOPIC_PROD(),
                                    SerDes
                                            .getSerDes()
                                            .serialize( position1 ) )
                            .addCallback( new ListenableFutureCallback<>() {
                                @Override
                                public void onFailure( @NonNull Throwable ex ) {
                                    logger.warning( "Kafka does not work since: receive"
                                            + LocalDateTime.now() ); }

                                @Override
                                public void onSuccess( SendResult< String, String > result ) {
                                    logger.info( "Kafka got: " + position1.getDeviceId() +
                                            " at: " + position1.getDeviceTime() +
                                            " with offset: " + result.getRecordMetadata().offset()
                                            + " for PROD" ); } } );

                    this.getKafkaTemplate().send( this.getRAW_GPS_LOCATION_TOPIC_DEV(),
                                    SerDes
                                            .getSerDes()
                                            .serialize( position1 ) )
                            .addCallback( new ListenableFutureCallback<>() {
                                @Override
                                public void onFailure( @NonNull Throwable ex ) {
                                    logger.warning( "Kafka does not work since: receive"
                                            + LocalDateTime.now() ); }

                                @Override
                                public void onSuccess( SendResult< String, String > result ) {
                                    logger.info( "Kafka got: " + position1.getDeviceId() +
                                            " at: " + position1.getDeviceTime() +
                                            " with offset: " + result.getRecordMetadata().offset()
                                            + " for DEV" ); } } ); } ); }

    public void clear () {
        this.logger.info( "Kafka was closed" );
        this.getKafkaTemplate().destroy();
        this.getKafkaTemplate().flush();
        this.getProperties().clear();
        this.setProperties( null );
        this.getClient().close();
        instance = null; }
}
