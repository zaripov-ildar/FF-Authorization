package ru.findFood.auth.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.findFood.auth.dtos.*;
import ru.findFood.auth.exceptions.AppError;
import ru.findFood.auth.services.AuthenticationService;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthenticationService service;

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/register/user")
    public ResponseEntity<?> registerUser(@RequestBody RegUserRequest request) {
        return service.register(request);
    }

    @PostMapping("/register/restaurant")
    public ResponseEntity<?> registerRestaurant(@RequestBody RegRestaurantRequest request) {
        try {
            return service.registerRestaurant(request);
        } catch (AppError e){
            return new ResponseEntity<>(new AppError(e.getStatus(), e.getMessage()), HttpStatusCode.valueOf(e.getStatus()));
        }
    }

    @PostMapping("/refreshJWT")
    public void refreshJWT(HttpServletRequest request, HttpServletResponse response) throws IOException {
        service.refreshToken(request, response);
    }
}
