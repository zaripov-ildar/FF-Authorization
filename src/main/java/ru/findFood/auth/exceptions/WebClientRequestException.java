package ru.findFood.auth.exceptions;


import lombok.Getter;

@Getter
public class WebClientRequestException extends RuntimeException {
    private final int statusCode;
    public WebClientRequestException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
