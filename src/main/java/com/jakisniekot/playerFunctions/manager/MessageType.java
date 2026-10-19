package com.jakisniekot.playerFunctions.manager;

public class MessageType {

    private String message;
    private boolean colorfulMessage;

    public MessageType(
            String message,
            Boolean colorfulMessage
    ) {
        this.message = message;
        this.colorfulMessage = colorfulMessage;
    }

    public boolean isColorfulMessage() {
        return colorfulMessage;
    }

    public String getMessage() {
        return message;
    }
}
