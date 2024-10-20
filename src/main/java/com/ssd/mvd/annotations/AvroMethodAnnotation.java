package com.ssd.mvd.annotations;

import java.lang.annotation.*;

@Target( value = ElementType.METHOD )
@Retention( value = RetentionPolicy.RUNTIME )
@Documented
@SuppressWarnings(
        value = """
                отвечает за методы классов, которые используются для сериализации
                и отправки в Кафку
                """
)
public @interface AvroMethodAnnotation {
    String name();
}
