package ru.findFood.auth.services;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.findFood.auth.dtos.*;
import ru.findFood.auth.entities.AuthUser;
import ru.findFood.auth.entities.Role;
import ru.findFood.auth.exceptions.AppError;
import ru.findFood.auth.exceptions.WebClientRequestException;
import ru.findFood.auth.integrations.PersonAreaServiceIntegration;
import ru.findFood.auth.integrations.RestaurantsServiceIntegration;
import ru.findFood.auth.repositories.RoleRepository;
import ru.findFood.auth.utils.JwtUtils;
import ru.findFood.auth.validators.RestaurantInputValidator;
import ru.findFood.auth.validators.UserInputValidator;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final JwtUtils jwtUtils;
    private final PersonService personService;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder encoder;
    private final RestaurantsServiceIntegration restServiceIntegration;
    private final PersonAreaServiceIntegration personAreaServiceIntegration;
    private final RestaurantInputValidator restaurantInputValidator;
    private final UserInputValidator userInputValidator;

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

    @Transactional
    public ResponseEntity<?> register(RegUserRequest request) {

        ResponseEntity<RegMessage> responseUserInputValidation = userInputValidator.validate(request);

        if (responseUserInputValidation.getStatusCode() != HttpStatus.OK) return responseUserInputValidation;

        ResponseEntity<?> response = register(new AuthenticationRequest(request.getEmail(), request.getPassword()), clientRole);

        if (response.getStatusCode() == HttpStatus.PRECONDITION_FAILED) {
            return response;
        }

        if (response.getStatusCode() == HttpStatus.OK) {

            try {

                ResponseEntity<?> responsePersonService = personAreaServiceIntegration.createNewPerson(
                        new NewUserAddedToDb(request.getName(), request.getEmail()
                        ));

                if (responsePersonService.getStatusCode() == HttpStatus.CREATED) {
                    return new ResponseEntity<>(new RegMessage(HttpStatus.CREATED,
                            "Регистрация прошла успешно!!!"), HttpStatus.CREATED);
                }

            } catch (WebClientRequestException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new ResponseEntity<>(new RegMessage(HttpStatus.resolve(e.getStatusCode()), e.getMessage()), HttpStatus.resolve(e.getStatusCode()));

            } catch (WebClientResponseException ex){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new ResponseEntity<>(new RegMessage(HttpStatus.resolve(ex.getStatusCode().value()), ex.getMessage()), HttpStatus.resolve(ex.getStatusCode().value()));
            }
        }
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return new ResponseEntity<>(new RegMessage(HttpStatus.UNAUTHORIZED,
                "Регистрация закончилась неудачно!!!"), HttpStatus.UNAUTHORIZED);
    }



    @Transactional
    public ResponseEntity<?> registerRestaurant(RegRestaurantRequest reg_request) throws AppError {

        ResponseEntity<RegMessage> responseRestaurantInputValidation = restaurantInputValidator.validate(reg_request);

        if (responseRestaurantInputValidation.getStatusCode() != HttpStatus.OK) return responseRestaurantInputValidation;

        ResponseEntity<?> response = register(new AuthenticationRequest(reg_request.getEmail(), reg_request.getPassword()), restaurantRole);


        if (response.getStatusCode() == HttpStatus.PRECONDITION_FAILED) {
            return response;
        }

        if (response.getStatusCode() == HttpStatus.OK) {

            try {
                ResponseEntity<?> responseRestService = restServiceIntegration.createNewRestaurant(
                        new NewRestaurantAddedToDb(reg_request.getTitle(), reg_request.getEmail())
                );


                if (responseRestService.getStatusCode() == HttpStatus.CREATED) {
                    return new ResponseEntity<>(new RegMessage(HttpStatus.CREATED,
                            "Регистрация прошла успешно!!!"), HttpStatus.CREATED);
                }

            } catch (WebClientRequestException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new ResponseEntity<>(new RegMessage(HttpStatus.resolve(e.getStatusCode()),
                        e.getMessage()), Objects.requireNonNull(HttpStatus.resolve(e.getStatusCode())));

            } catch (WebClientResponseException ex){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new ResponseEntity<>(new RegMessage(HttpStatus.resolve(ex.getStatusCode().value()),
                        ex.getMessage()), Objects.requireNonNull(HttpStatus.resolve(ex.getStatusCode().value())));
            }
        }
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return new ResponseEntity<>(new RegMessage(HttpStatus.UNAUTHORIZED,
                "Регистрация закончилась неудачно!!!"), HttpStatus.UNAUTHORIZED);
    }




    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<?>  register(AuthenticationRequest request, String role) {
        Role roleClient = roleRepository.findByTitle(role)
                .orElseThrow(()->new RuntimeException(role + " role not exist!"));

        if (personService.isExistByEmail(request.email())) {
            return new ResponseEntity<>(new RegMessage(HttpStatus.PRECONDITION_FAILED,
                    "Адрес электронной почты " + request.email() + " уже используется!!!"), HttpStatus.PRECONDITION_FAILED);
        }
        AuthUser person = AuthUser.builder()
                .email(request.email())
                .password(encoder.encode(request.password()))
                .roles(List.of(roleClient))
                .build();
        personService.save(person);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    private AuthenticationResponse createResponse(AuthUser person) {
        var jwt = jwtUtils.generateJwt(person);
        var refreshToken = jwtUtils.generateRefreshToken(person);
        return new AuthenticationResponse(jwt, refreshToken);
    }
}
