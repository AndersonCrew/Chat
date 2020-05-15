package com.dazone.crewchatoff.socket;

import java.lang.Exception;

public class BitConverter {

    public static byte[] getBytes(int x) {
        return new byte[] { (byte) x, (byte) (x >>> 8),
                (byte) (x >>> 16), (byte) (x >>> 24) };
    }

    public static byte[] getBytes(long x) {
        return new byte[] {
                (byte) x,
                (byte) (x >>> 8),
                (byte) (x >>> 16),
                (byte) (x >>> 24),
                (byte) (x >>> 32),
                (byte) (x >>> 40),
                (byte) (x >>> 48),
                (byte) (x >>> 56)
        };
    }

    public static int toInt32(byte[] bytes, int index) throws Exception {
        if (bytes.length != 4)
            throw new Exception(
                    "The length of the byte array must be at least 4 bytes long.");
        return (0xff & bytes[index]) << 56
                | (0xff & bytes[index + 1]) << 48
                | (0xff & bytes[index + 2]) << 40 | (0xff & bytes[index + 3]) << 32;
    }


    public static String toString(byte[] bytes) throws Exception {
        if (bytes == null)
            throw new Exception("The byte array must have at least 1 byte.");
        return new String(bytes);
    }
}
