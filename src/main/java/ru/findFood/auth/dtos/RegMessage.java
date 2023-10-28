package ru.findFood.auth.dtos;

import org.springframework.http.HttpStatus;

public record RegMessage (HttpStatus code, String message){}
