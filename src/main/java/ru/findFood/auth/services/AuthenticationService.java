package ru.findFood.auth.services;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.findFood.auth.dtos.AuthenticationRequest;
import ru.findFood.auth.dtos.AuthenticationResponse;
import ru.findFood.auth.entities.AuthUser;
import ru.findFood.auth.entities.Role;
import ru.findFood.auth.exceptions.EmailAlreadyExists;
import ru.findFood.auth.repositories.RoleRepository;
import ru.findFood.auth.utils.JwtUtils;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtUtils jwtUtils;
    private final PersonService personService;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder encoder;

    @Value("${security_params.clientRole}")
    private String clientRole;

    @Value("${security_params.restaurantRole}")
    private String restaurantRole;

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException{
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String personEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        personEmail = jwtUtils.extractEmail(refreshToken);
        if (personEmail != null) {
            var person = personService.findPersonByEmail(personEmail);
            if (jwtUtils.isTokenValid(refreshToken, person)) {
                var accessToken = jwtUtils.generateJwt(person);
                var authResponse = new AuthenticationResponse(accessToken, refreshToken);
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        var person = personService.findPersonByEmail(request.email());
        return createResponse(person);
    }

    public AuthenticationResponse register(AuthenticationRequest request){
        return register(request, clientRole);

    }

    public void registerRestaurant(AuthenticationRequest request){
        register(request, restaurantRole);
    }

    private AuthenticationResponse register(AuthenticationRequest request, String role) {
        Role roleClient = roleRepository.findByTitle(role)
                .orElse(new Role(role));
        roleRepository.save(roleClient);

        if (personService.isExistByEmail(request.email())) {
            throw new EmailAlreadyExists(request.email());
        }
        AuthUser person = AuthUser.builder()
                .email(request.email())
                .password(encoder.encode(request.password()))
                .roles(List.of(roleClient))
                .build();
        personService.save(person);
        return createResponse(person);
    }

    private AuthenticationResponse createResponse(AuthUser person) {
        var jwt = jwtUtils.generateJwt(person);
        var refreshToken = jwtUtils.generateRefreshToken(person);
        return new AuthenticationResponse(jwt, refreshToken);
    }
}
