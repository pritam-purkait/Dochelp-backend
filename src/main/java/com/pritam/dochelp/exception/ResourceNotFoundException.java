package com.pritam.dochelp.exception;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message) {
        super(message);
    }
    public ResourceNotFoundException() {
        super("Resource not found:");
    }
    public ResourceNotFoundException(String message ,Throwable cause) {
        super(message, cause);
    }
}
