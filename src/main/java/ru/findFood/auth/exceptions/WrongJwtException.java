package ru.findFood.auth.exceptions;

public class WrongJwtException extends RuntimeException{
    public WrongJwtException(String message) {
        super(message);
    }
}
