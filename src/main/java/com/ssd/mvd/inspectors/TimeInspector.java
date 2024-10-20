package com.ssd.mvd.inspectors;

import com.ssd.mvd.annotations.EntityConstructorAnnotation;
import java.util.Date;

@com.ssd.mvd.annotations.ImmutableEntityAnnotation
public class TimeInspector extends StringOperations {
    @EntityConstructorAnnotation( permission = DataValidateInspector.class )
    protected <T> TimeInspector( @lombok.NonNull final Class<T> instance ) {
        super( TimeInspector.class );

        AnnotationInspector.checkCallerPermission( instance, TimeInspector.class );
        AnnotationInspector.checkAnnotationIsImmutable( TimeInspector.class );
    }

    protected final static int DAY_IN_SECOND = 86400;

    @lombok.NonNull
    @lombok.Synchronized
    protected final synchronized Date newDate () {
        return new Date();
    }
}
