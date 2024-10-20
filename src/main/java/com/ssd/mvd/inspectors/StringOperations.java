package com.ssd.mvd.inspectors;

import com.ssd.mvd.annotations.EntityConstructorAnnotation;

@com.ssd.mvd.annotations.ImmutableEntityAnnotation
public class StringOperations extends CollectionsInspector {
    @EntityConstructorAnnotation( permission = TimeInspector.class )
    protected <T> StringOperations( @lombok.NonNull final Class<T> instance ) {
        super( StringOperations.class );

        AnnotationInspector.checkCallerPermission( instance, StringOperations.class );
        AnnotationInspector.checkAnnotationIsImmutable( StringOperations.class );
    }

    public final static String DOT = ".";
    public final static String EMPTY = "";
    public final static String SPACE = " ";
    protected final static String SPACE_WITH_DOUBLE_DOTS = " : ";

    public final static String AVRO_DATE_PATTERN = "^[0-9]{4}-[0-9]{2}-[0-9]{2}$";
}
