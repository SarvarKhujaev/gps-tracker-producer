package com.ssd.mvd.annotations;

import java.lang.annotation.*;

@Target( value = ElementType.TYPE )
@Retention( value = RetentionPolicy.RUNTIME )
@Documented
@SuppressWarnings(
        value = """
                используется на уровне сервисов для взаимодействия с appliation.yml,
                хранит основное название переменных
                """
)
public @interface ServiceParametrAnnotation {
    /*
    название группы для самого сервиса где храняться все переменные этого сервиса
    */
    String propertyGroupName() default "JWT_VARIABLES";
    /*
    название основной группы где храняться все переменные
    */
    String mainGroupName() default "variables";
}
