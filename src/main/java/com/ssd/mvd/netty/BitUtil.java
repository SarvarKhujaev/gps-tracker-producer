package com.ssd.mvd.netty;

public final class BitUtil {
    public static boolean check( long number, int index ) { return ( number & ( 1 << index ) ) != 0; }

    public static long between(long number, int from, int to) {
        return (number >> from) & ((1L << to - from) - 1L);
    }
}
