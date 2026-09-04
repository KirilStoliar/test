package com.serviceorders.common.core.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Object resourceId) {
        super("%s with id '%s' was not found".formatted(resourceName, resourceId));
    }
}