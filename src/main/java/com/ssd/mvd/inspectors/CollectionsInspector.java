package com.ssd.mvd.inspectors;

import com.ssd.mvd.annotations.EntityConstructorAnnotation;

import java.util.WeakHashMap;
import java.util.Arrays;
import java.util.List;

@com.ssd.mvd.annotations.ImmutableEntityAnnotation
public class CollectionsInspector {
    protected CollectionsInspector() {}

    @lombok.NonNull
    @lombok.Synchronized
    protected final synchronized <T, V> WeakHashMap<T, V> newMap () {
        return new WeakHashMap<>( 1 );
    }

    @EntityConstructorAnnotation( permission = StringOperations.class )
    protected <T> CollectionsInspector( @lombok.NonNull final Class<T> instance ) {
        AnnotationInspector.checkCallerPermission( instance, CollectionsInspector.class );
        AnnotationInspector.checkAnnotationIsImmutable( CollectionsInspector.class );
    }

    @lombok.NonNull
    @lombok.Synchronized
    @org.jetbrains.annotations.Contract( value = "_ -> _" )
    protected static synchronized <T> List<T> convertArrayToList (
            @lombok.NonNull final T[] objects
    ) {
        return Arrays.asList( objects );
    }
}
