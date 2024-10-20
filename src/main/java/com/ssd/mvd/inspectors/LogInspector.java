package com.ssd.mvd.inspectors;

import com.ssd.mvd.kafka.kafkaConfigs.KafkaProducerInterceptor;
import com.ssd.mvd.annotations.EntityConstructorAnnotation;

import org.springframework.scheduling.annotation.Async;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@com.ssd.mvd.annotations.ImmutableEntityAnnotation
@com.ssd.mvd.annotations.ServiceParametrAnnotation( propertyGroupName = "LOGGER_WITH_JSON_LAYOUT" )
public class LogInspector extends DataValidateInspector {
    @EntityConstructorAnnotation(
            permission = {
                    AnnotationInspector.class,
                    KafkaProducerInterceptor.class
            }
    )
    protected <T> LogInspector ( @lombok.NonNull final Class<T> instance ) {
        super( LogInspector.class );

        AnnotationInspector.checkCallerPermission( instance, LogInspector.class );
        AnnotationInspector.checkAnnotationIsImmutable( LogInspector.class );
    }

    private final static Logger LOGGER = LogManager.getLogger( "LOGGER_WITH_JSON_LAYOUT" );

    @Async( value = "LogInspector" )
    protected void logging ( @lombok.NonNull final Object o ) {
        LOGGER.info(
                String.join(
                        SPACE_WITH_DOUBLE_DOTS,
                        o.getClass().getName(),
                        "was closed successfully at",
                        super.newDate().toString()
                )
        );
    }

    @Async( value = "LogInspector" )
    protected void logging ( @lombok.NonNull final String message ) {
        LOGGER.info( message );
    }

    @Async( value = "LogInspector" )
    protected void logging ( @lombok.NonNull final Class<? extends CollectionsInspector> clazz ) {
        LOGGER.info(
                String.join(
                        SPACE_WITH_DOUBLE_DOTS,
                        clazz.getName(),
                        "was created at",
                        super.newDate().toString()
                )
        );
    }
}
