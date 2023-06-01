package org.featurehouse.mcmod.symlinkcheck;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

@Target({ElementType.TYPE_USE, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotNull
public @interface Dotted {
    boolean force() default true;
}
