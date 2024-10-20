package com.ssd.mvd.annotations;

import org.apache.avro.Schema;
import java.lang.annotation.*;

@Target( value = ElementType.FIELD )
@Retention( value = RetentionPolicy.RUNTIME )
@Documented
@SuppressWarnings(
        value = """
                отвечает за параметры классов, которые используются для сериализации
                и отправки в Кафку
                """
)
public @interface AvroFieldAnnotation {
    String name();
    String description() default com.ssd.mvd.inspectors.StringOperations.EMPTY;

    byte chosenEnum() default 0;

    boolean isEnum() default false;
    boolean isDate() default false;
    boolean isEntity() default false;

    Schema.Type schemaType() default Schema.Type.STRING;
}
