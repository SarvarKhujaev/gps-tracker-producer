package com.ssd.mvd.inspectors;

import com.ssd.mvd.kafka.kafkaConfigs.KafkaOptionsAndParams;
import com.ssd.mvd.annotations.EntityConstructorAnnotation;
import com.ssd.mvd.annotations.ImmutableEntityAnnotation;
import com.ssd.mvd.annotations.ServiceParametrAnnotation;
import com.ssd.mvd.constants.Errors;

import org.apache.commons.lang3.Validate;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

import java.util.stream.Stream;
import java.util.Collections;

public class AnnotationInspector extends LogInspector {
    @EntityConstructorAnnotation( permission = KafkaOptionsAndParams.class )
    protected <T extends CollectionsInspector> AnnotationInspector ( @lombok.NonNull final Class<T> instance ) {
        super( AnnotationInspector.class );

        AnnotationInspector.checkCallerPermission( instance, KafkaOptionsAndParams.class );
        AnnotationInspector.checkAnnotationIsImmutable( KafkaOptionsAndParams.class );
    }

    @lombok.NonNull
    @lombok.Synchronized
    @org.jetbrains.annotations.Contract( value = "_, _ -> !null" )
    public static synchronized String getVariable (
            @lombok.NonNull final Class< ? > object,
            @lombok.NonNull final String paramName
    ) {
        Validate.isTrue(
                object.isAnnotationPresent( ServiceParametrAnnotation.class ),
                Errors.WRONG_TYPE_IN_ANNOTATION.translate( "ru", object.getName() )
        );

        return checkContextOrReturnDefaultValue(
                String.join(
                        DOT,
                        object.getAnnotation( ServiceParametrAnnotation.class ).mainGroupName(),
                        object.getAnnotation( ServiceParametrAnnotation.class ).propertyGroupName(),
                        paramName
                ),
                Errors.DATA_NOT_FOUND.translate(
                        "ru",
                        String.join(
                                DOT,
                                object.getAnnotation( ServiceParametrAnnotation.class ).mainGroupName(),
                                object.getAnnotation( ServiceParametrAnnotation.class ).propertyGroupName(),
                                paramName
                        )
                )
        );
    }

    @SuppressWarnings(
            value = """
                    Принимает класс и возвращает его экземпляры классов,
                    у которых есть доступ к конструктору вызванного объекта

                    Проверяет что у метода есть нужная аннотация
                    В случае ошибки вызывает Exception с подходящим сообщением
                    """
    )
    @lombok.Synchronized
    @org.jetbrains.annotations.Contract( value = "_, _ -> fail" )
    public static synchronized <T, U> void checkCallerPermission (
            // класс который обращается
            @lombok.NonNull final Class<T> callerInstance,
            // класс к которому обращаются
            @lombok.NonNull final Class<U> calledInstance
    ) {
        try {
            final Constructor<U> declaredConstructor = calledInstance.getDeclaredConstructor( Class.class );
            org.springframework.util.ReflectionUtils.makeAccessible( declaredConstructor );
            declaredConstructor.setAccessible( true );

            Validate.isTrue(
                    (
                            declaredConstructor.isAnnotationPresent( EntityConstructorAnnotation.class )
                                    && declaredConstructor.getParameters().length == 1
                                    && Collections.frequency(
                                    CollectionsInspector.convertArrayToList(
                                            declaredConstructor
                                                    .getAnnotation( EntityConstructorAnnotation.class )
                                                    .permission()
                                    ),
                                    callerInstance
                            ) > 0
                    ),
                    Errors.OBJECT_IS_OUT_OF_INSTANCE_PERMISSION.translate(
                            callerInstance.getName(),
                            calledInstance.getName()
                    )
            );
        } catch ( final NoSuchMethodException e ) {
            throw new RuntimeException(e);
        }
    }

    @lombok.Synchronized
    @org.jetbrains.annotations.Contract( value = "_ -> fail" )
    protected static synchronized < T > void checkAnnotationIsImmutable (
            @lombok.NonNull final Class<T> object
    ) {
        Validate.isTrue(
                object.isAnnotationPresent( ImmutableEntityAnnotation.class ),
                Errors.OBJECT_IS_IMMUTABLE.translate( object.getName() )
        );
    }

    @SuppressWarnings(
            value = """
                    Принимает экземпляр класса и возвращает список всех его параметров
                    """
    )
    @lombok.NonNull
    @lombok.Synchronized
    @org.jetbrains.annotations.Contract( value = "_ -> !null" )
    protected static synchronized Stream<Field> getFields (
            @lombok.NonNull final Class< ? > object
    ) {
        return convertArrayToList( object.getDeclaredFields() ).stream();
    }

    @SuppressWarnings(
            value = """
                    Принимает экземпляр класса и возвращает список всех его методов
                    """
    )
    @lombok.NonNull
    @lombok.Synchronized
    @org.jetbrains.annotations.Contract( value = "_ -> !null" )
    protected static synchronized Stream<Method> getMethods (
            @lombok.NonNull final Class< ? > object
    ) {
        return convertArrayToList( object.getDeclaredMethods() ).stream();
    }
}
