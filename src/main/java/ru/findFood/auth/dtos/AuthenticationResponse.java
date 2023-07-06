package ru.findFood.auth.dtos;

public record AuthenticationResponse(String jwt, String refreshToken) {
}
