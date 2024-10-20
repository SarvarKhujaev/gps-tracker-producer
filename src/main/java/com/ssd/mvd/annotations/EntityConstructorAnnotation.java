package com.ssd.mvd.annotations;

import java.lang.annotation.*;

@Target( value = ElementType.CONSTRUCTOR )
@Retention( value = RetentionPolicy.RUNTIME )
@Documented
public @interface EntityConstructorAnnotation {
    Class<?>[] permission();
}
