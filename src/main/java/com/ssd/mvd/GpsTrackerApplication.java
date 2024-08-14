package com.ssd.mvd;

import com.ssd.mvd.kafka.KafkaDataControl;
import com.ssd.mvd.netty.Position;
import com.ssd.mvd.netty.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class GpsTrackerApplication {
    public static ApplicationContext context;

    public static void main( String[] args ) {
//        context = SpringApplication.run( GpsTrackerApplication.class, args );
//        new Server().initServers();
        while ( true ) {
            for ( int i = 0; i < 10; i++ ) {
                final Position position = new Position();
                position.setDeviceId( "123456789" );
                position.setDeviceTime( new Date() );

                position.setLatitude( 65.6 );
                position.setLongitude( 65.6 );

                KafkaDataControl
                        .getInstance()
                        .getWriteToKafka()
                        .accept( position );
            }

            try {
                Thread.sleep( 5000 );
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
