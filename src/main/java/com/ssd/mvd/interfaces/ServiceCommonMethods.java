package com.ssd.mvd.interfaces;

public interface ServiceCommonMethods {
    default void close() {}

    default void clean() {
        System.gc();
    }

    default void close( @lombok.NonNull final Throwable throwable ) {}
}
