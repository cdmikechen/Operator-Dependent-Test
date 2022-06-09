package com.pesco.operator.common.exception;

public class ConfigmapException extends RuntimeException {

    public ConfigmapException(String message) {
        super(message);
    }

    public ConfigmapException(String message, Throwable cause) {
        super(message, cause);
    }
}
