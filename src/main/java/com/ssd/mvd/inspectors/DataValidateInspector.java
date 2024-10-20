package com.ssd.mvd.inspectors;

import com.ssd.mvd.annotations.EntityConstructorAnnotation;
import com.ssd.mvd.GpsTrackerApplication;
import java.util.*;

@com.ssd.mvd.annotations.ImmutableEntityAnnotation
public class DataValidateInspector extends TimeInspector {
    @EntityConstructorAnnotation( permission = LogInspector.class )
    protected <T> DataValidateInspector ( @lombok.NonNull final Class<T> instance ) {
        super( DataValidateInspector.class );

        AnnotationInspector.checkCallerPermission( instance, DataValidateInspector.class );
        AnnotationInspector.checkAnnotationIsImmutable( DataValidateInspector.class );
    }

    @SuppressWarnings(
            value = """
                    получает в параметрах название параметра из файла application.yaml
                    проверят что context внутри main класса GpsTrackerApplication  инициализирован
                    и среди параметров сервиса существует переданный параметр
                    """
    )
    @lombok.Synchronized
    @org.jetbrains.annotations.Contract( value = "_, _ -> _" )
    protected static synchronized int checkContextOrReturnDefaultValue (
            @lombok.NonNull final String paramName,
            final int defaultValue
    ) {
        return Objects.nonNull( GpsTrackerApplication.context )
                && Objects.nonNull(
                        GpsTrackerApplication
                                .context
                                .getEnvironment()
                                .getProperty( paramName )
                )
                ? Integer.parseInt(
                        GpsTrackerApplication
                                .context
                                .getEnvironment()
                                .getProperty( paramName )
                )
                : defaultValue;
    }

    @lombok.Synchronized
    @org.jetbrains.annotations.Contract( value = "_, _ -> _" )
    protected static synchronized String checkContextOrReturnDefaultValue (
            @lombok.NonNull final String paramName,
            @lombok.NonNull final String defaultValue
    ) {
        return Objects.nonNull( GpsTrackerApplication.context )
                && Objects.nonNull(
                        GpsTrackerApplication
                                .context
                                .getEnvironment()
                                .getProperty( paramName )
                )
                ? GpsTrackerApplication
                .context
                .getEnvironment()
                .getProperty( paramName )
                : defaultValue;
    }
}