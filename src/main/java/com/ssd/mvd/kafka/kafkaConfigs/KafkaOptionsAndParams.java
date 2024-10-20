package com.ssd.mvd.kafka.kafkaConfigs;

import com.ssd.mvd.annotations.EntityConstructorAnnotation;
import com.ssd.mvd.inspectors.CollectionsInspector;
import com.ssd.mvd.inspectors.AnnotationInspector;
import com.ssd.mvd.kafka.KafkaDataControl;

@com.ssd.mvd.annotations.ServiceParametrAnnotation( propertyGroupName = "KAFKA_VARIABLES" )
public class KafkaOptionsAndParams extends AnnotationInspector {
    @EntityConstructorAnnotation( permission = KafkaDataControl.class )
    protected <T extends CollectionsInspector> KafkaOptionsAndParams ( @lombok.NonNull final Class<T> instance ) {
        super( KafkaOptionsAndParams.class );

        AnnotationInspector.checkCallerPermission( instance, KafkaOptionsAndParams.class );
        AnnotationInspector.checkAnnotationIsImmutable( KafkaOptionsAndParams.class );
    }

    protected final static String KAFKA_ACKS_CONFIG = checkContextOrReturnDefaultValue(
            AnnotationInspector.getVariable(
                    KafkaOptionsAndParams.class,
                    KafkaOptionsAndParams.class.getDeclaredFields()[0].getName()
            ),
            "all"
    );

    protected final static String GROUP_ID_FOR_KAFKA = checkContextOrReturnDefaultValue(
            AnnotationInspector.getVariable(
                    KafkaOptionsAndParams.class,
                    KafkaOptionsAndParams.class.getDeclaredFields()[1].getName()
            ),
            com.ssd.mvd.kafka.KafkaDataControl.class.getName()
    );

    protected final static String KAFKA_BROKER = checkContextOrReturnDefaultValue(
            AnnotationInspector.getVariable(
                    KafkaOptionsAndParams.class,
                    KafkaOptionsAndParams.class.getDeclaredFields()[2].getName()
            ),
            "localhost:9092"
    );

    protected final static int KAFKA_SENDER_MAX_IN_FLIGHT = checkContextOrReturnDefaultValue(
            AnnotationInspector.getVariable(
                    KafkaOptionsAndParams.class,
                    KafkaOptionsAndParams.class.getDeclaredFields()[3].getName()
            ),
            1024
    );

    protected final static int RETRIES_CONFIG = checkContextOrReturnDefaultValue(
            AnnotationInspector.getVariable(
                    KafkaOptionsAndParams.class,
                    KafkaOptionsAndParams.class.getDeclaredFields()[4].getName()
            ),
            3
    );

    protected final static int LINGER_MS_CONFIG = checkContextOrReturnDefaultValue(
            AnnotationInspector.getVariable(
                    KafkaOptionsAndParams.class,
                    KafkaOptionsAndParams.class.getDeclaredFields()[5].getName()
            ),
            DAY_IN_SECOND / 10000
    );

    protected final static int BATCH_SIZE_CONFIG = checkContextOrReturnDefaultValue(
            AnnotationInspector.getVariable(
                    KafkaOptionsAndParams.class,
                    KafkaOptionsAndParams.class.getDeclaredFields()[6].getName()
            ),
            DAY_IN_SECOND / 4 // 21 Kb
    );

    @SuppressWarnings(
            value = "The buffer.memory property sets the total memory allocated for the producer buffer."
    )
    protected final static int BUFFER_MEMORY_CONFIG = checkContextOrReturnDefaultValue(
            AnnotationInspector.getVariable(
                    KafkaOptionsAndParams.class,
                    KafkaOptionsAndParams.class.getDeclaredFields()[7].getName()
            ),
            DAY_IN_SECOND * 40 // 32MB buffer size
    );

    protected final static int REQUEST_TIMEOUT_MS_CONFIG = checkContextOrReturnDefaultValue(
            AnnotationInspector.getVariable(
                    KafkaOptionsAndParams.class,
                    KafkaOptionsAndParams.class.getDeclaredFields()[8].getName()
            ),
            DAY_IN_SECOND / 100
    );

    protected final static int MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION = checkContextOrReturnDefaultValue(
            AnnotationInspector.getVariable(
                    KafkaOptionsAndParams.class,
                    KafkaOptionsAndParams.class.getDeclaredFields()[9].getName()
            ),
            3
    );

    protected final static int METADATA_MAX_AGE_CONFIG = checkContextOrReturnDefaultValue(
            AnnotationInspector.getVariable(
                    KafkaOptionsAndParams.class,
                    KafkaOptionsAndParams.class.getDeclaredFields()[10].getName()
            ),
            DAY_IN_SECOND
    );
}
