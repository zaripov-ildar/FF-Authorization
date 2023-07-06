package ru.findFood.auth.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import ru.findFood.auth.dtos.AuthenticationRequest;
import ru.findFood.auth.dtos.AuthenticationResponse;

@Service
public class AuthenticationService {
    
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {

    }

   
    public AuthenticationResponse register(AuthenticationRequest request) {
        return null;
    }

   
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        return null;
    }

    public void registerRestaurant(AuthenticationRequest request) {
    }
}
