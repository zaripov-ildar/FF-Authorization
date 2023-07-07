package ru.findFood.auth.exceptions;

public class EmailAlreadyExists extends RuntimeException {
    public EmailAlreadyExists(String email) {
        super(email);
    }
}
