package com.studyhub.chat_service.exception;

public class MessageNotFoundException extends RuntimeException {
    public MessageNotFoundException(String message) {
        super(message);
    }
    
    public MessageNotFoundException(Long messageId) {
        super("Message not found with id: " + messageId);
    }
}
