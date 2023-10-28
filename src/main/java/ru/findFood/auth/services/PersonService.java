package ru.findFood.auth.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.findFood.auth.converters.RoleConverter;
import ru.findFood.auth.dtos.*;
import ru.findFood.auth.entities.AuthUser;
import ru.findFood.auth.exceptions.AppError;
import ru.findFood.auth.exceptions.ResourceNotFoundException;
import ru.findFood.auth.exceptions.WebClientRequestException;
import ru.findFood.auth.integrations.PersonAreaServiceIntegration;
import ru.findFood.auth.integrations.RestaurantsServiceIntegration;
import ru.findFood.auth.repositories.AuthUserRepository;
import ru.findFood.auth.services.specifications.AuthUserSpecifications;
import ru.findFood.auth.validators.DelRequestValidator;
import ru.findFood.auth.validators.NewEmailValidator;
import ru.findFood.auth.validators.NewPasswordValidator;
import ru.findFood.auth.validators.PersonRolesUpdateValidator;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PersonService {

    private final AuthUserRepository repository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final DelRequestValidator delRequestValidator;
    private final NewEmailValidator newEmailValidator;
    private final NewPasswordValidator newPasswordValidator;
    private final PersonRolesUpdateValidator roleUpdateValidator;
    private final RestaurantsServiceIntegration restServiceIntegration;
    private final PersonAreaServiceIntegration personAreaServiceIntegration;
    private final RoleConverter roleConverter;


    public AuthUser findPersonByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Can't find the email: " + email));
    }

    public List<AuthUser> findAllEmployees(String byRole, String partEmail) {

        Integer numberRoles = 1;
        Specification<AuthUser> spec = Specification.where(null);

        spec = spec.and(AuthUserSpecifications.getByRolesWhereSizeGreaterThan(numberRoles));

        if (byRole != null) {
            spec = spec.and(AuthUserSpecifications.getByRolesTitleEqualsTo(byRole));
        }
        if (partEmail != null) {
            spec = spec.and(AuthUserSpecifications.emailLike(partEmail));
        }

        return repository.findAll(spec);
    }

    public Boolean confirmData(String email, String password) throws AppError {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (BadCredentialsException e) {
            throw new AppError(HttpStatus.BAD_REQUEST.value(), "Имя пользователя или пароль не найдены");
        }
        return true;
    }

    public Boolean changePassword(NewPasswordRequest request) throws AppError {
        newPasswordValidator.validate(request);
        if(confirmData(request.getEmail(), request.getPassword())){
            AuthUser person = findPersonByEmail(request.getEmail());
            person.setPassword(encoder.encode(request.getNewPassword()));
            repository.save(person);
            return true;
        }
        throw new AppError(HttpStatus.BAD_REQUEST.value(), "Операция замены пароля завершилась с ошибкой");
    }

    @Transactional
    public Boolean changeUserOrRestaurantEmail(NewEmailRequest request, String integrationDefine) throws AppError {
        newEmailValidator.validate(request);
        String message = "Операция замены email завершилась с ошибкой!";
        String msg;
        ResponseEntity<?> responseService = null;
        if(confirmData(request.getEmail(), request.getPassword())){
            AuthUser person = findPersonByEmail(request.getEmail());
            person.setEmail(request.getNewEmail());
            repository.save(person);
            EmailToNewEmail emailToNewEmail = new EmailToNewEmail(request.getEmail(), request.getNewEmail());
            try {
                if (integrationDefine.equals("user")) {
                    responseService = personAreaServiceIntegration.changePersonEmail(emailToNewEmail);
                }else if(integrationDefine.equals("rest")){
                    responseService = restServiceIntegration.changeRestaurantEmail(emailToNewEmail);
                }
                assert responseService != null;
                if (responseService.getStatusCode() == HttpStatus.OK) {
                    return true;
                }
            } catch (WebClientRequestException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                msg = DefineSourceOfMessage(message, e.getMessage());
                throw new AppError(e.getStatusCode(), msg);
            } catch (WebClientResponseException ex) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                msg = DefineSourceOfMessage(message, ex.getMessage());
                throw new AppError(ex.getStatusCode().value(), msg);
            }
        }
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        throw new AppError(HttpStatus.BAD_REQUEST.value(), message);
    }

    @Transactional
    public AuthUser updateRole(PersonDto personDto) throws AppError {

        AuthUser person = repository.findByEmail(personDto.getEmail())
                .orElseThrow(()-> new ResourceNotFoundException("Пользователь отсутствует в списке, id: " + personDto.getId()));
        roleUpdateValidator.validate(personDto.getRoles());
        person.setRoles(personDto.getRoles().stream().map(roleConverter::dtoToEntity).collect(Collectors.toList()));
        return repository.save(person);
    }

    public void save(AuthUser person) {
        repository.save(person);
    }

    public boolean isExistByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Transactional
    public boolean deleteUserOrRestaurantByEmail(DelRequest delRequest, String integrationDefine) throws AppError {
        delRequestValidator.validate(delRequest);
        String message;
        String msg;
        ResponseEntity<?> responseService = null;

        message = getMessageValue(delRequest, integrationDefine, "ID");

        repository.deleteByEmail(delRequest.getEmail());
        if (!isExistByEmail(delRequest.getEmail()) && !delRequest.getEmail().equals("") && delRequest.getEmail() != null) {
            try {
                if (integrationDefine.equals("user")) {
                    responseService = personAreaServiceIntegration.removePersonById(delRequest.getId());
                }else if(integrationDefine.equals("rest")){
                    responseService = restServiceIntegration.deleteRestaurantById(delRequest.getId());
                }
                assert responseService != null;
                if (responseService.getStatusCode() == HttpStatus.OK) {
                    return true;
                }
            } catch (WebClientRequestException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                msg = DefineSourceOfMessage(message, e.getMessage());
                throw new AppError(e.getStatusCode(), msg);
            } catch (WebClientResponseException ex) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                msg = DefineSourceOfMessage(message, ex.getMessage());
                throw new AppError(ex.getStatusCode().value(), msg);
            }
        }
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        message = getMessageValue(delRequest, integrationDefine, "email");
        throw new AppError(HttpStatus.BAD_REQUEST.value(), message);
    }

    private static String getMessageValue(DelRequest delRequest, String integrationDefine, String idOrEmail) {
        String message = "";

        if (integrationDefine.equals("user")) {
            message = "Операция удаления пользователя с " + idOrEmail + ": " + delRequest.getId() + " завершилась с ошибкой!";
        } else if (integrationDefine.equals("rest")) {
            message = "Операция удаления ресторана с " + idOrEmail + ": " + delRequest.getId() + " завершилась с ошибкой!";
        }
        return message;
    }

    private static String DefineSourceOfMessage(String message, String e_message) {
        String msg;
        if (e_message!= null) {
            msg = e_message;
        }else {
            msg = message;
        }
        return msg;
    }
}
