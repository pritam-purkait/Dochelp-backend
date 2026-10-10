package com.pritam.dochelp.exception;

public class DocumentProcessingException extends RuntimeException {
    public DocumentProcessingException(String message) {
        super(message);
    }

    public DocumentProcessingException() {
        super("Error processing document:");
    }
    public DocumentProcessingException(String message ,Throwable cause) {
        super(message, cause);
    }
}
