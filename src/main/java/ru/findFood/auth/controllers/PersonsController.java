package ru.findFood.auth.controllers;


import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.findFood.auth.converters.PersonConverter;
import ru.findFood.auth.dtos.*;
import ru.findFood.auth.entities.AuthUser;
import ru.findFood.auth.exceptions.AppError;
import ru.findFood.auth.services.PersonService;
import ru.findFood.auth.validators.ConfirmDataValidator;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api/v1/persons")
@RequiredArgsConstructor
public class PersonsController {

    private final PersonService personService;
    private final ConfirmDataValidator confirmDataValidator;
    @Autowired
    private PersonConverter personConverter;

//    private final UserValidator personValidator;


    // GET http://localhost:8189/market-auth/api/v1/persons



    @GetMapping("/employees/all")
    public List<PersonDto> getAllEmployees(
            @RequestParam(name = "by_role", required = false) String byRole,
            @RequestParam(name = "part_email", required = false) String partEmail
    ) {
        List<AuthUser> personsList = personService.findAllEmployees(byRole, partEmail);
        List<PersonDto> personDtoList = new ArrayList<>();
        for (AuthUser p: personsList) {
            PersonDto person = personConverter.entityToDto(p);
            personDtoList.add(person);
        }
        return personDtoList;
    }

    @GetMapping("/{email}")
    public PersonDto getUserByEmail(@PathVariable  String email) {
        AuthUser authUser = personService.findPersonByEmail(email);
        PersonDto personDto = personConverter.entityToDto(authUser);

        personDto.setPassword("PROTECTED");
        return personDto;
    }


    @PostMapping("/confirmation")
    public  ResponseEntity<?> confirmData(@RequestBody ConfirmDataRequest request) {
        try {
            confirmDataValidator.validate(request);
            return ResponseEntity.ok(personService.confirmData(request.getEmail(), request.getPassword()));
        } catch (AppError e) {
            return new ResponseEntity<>(new AppError(e.getStatus(), e.getMessage()), HttpStatusCode.valueOf(e.getStatus()));
        }

    }

    @PutMapping("/password")
    public  ResponseEntity<?> changePassword(@RequestBody NewPasswordRequest request) {
        try {
            return ResponseEntity.ok(personService.changePassword(request));
        } catch (AppError e) {
            return new ResponseEntity<>(new AppError(e.getStatus(), e.getMessage()), HttpStatusCode.valueOf(e.getStatus()));
        }

    }

    @PutMapping("/user/email")
    public  ResponseEntity<?> changeUserEmail(@RequestBody NewEmailRequest request){
        try {
            return ResponseEntity.ok(personService.changeUserOrRestaurantEmail(request, "user"));
        } catch (AppError e) {
            return new ResponseEntity<>(new AppError(e.getStatus(), e.getMessage()), HttpStatusCode.valueOf(e.getStatus()));
        }
    }

    @PutMapping("/restaurant/email")
    public  ResponseEntity<?> changeRestaurantEmail(@RequestBody NewEmailRequest request){
        try {
            return ResponseEntity.ok(personService.changeUserOrRestaurantEmail(request, "rest"));
        } catch (AppError e) {
            return new ResponseEntity<>(new AppError(e.getStatus(), e.getMessage()), HttpStatusCode.valueOf(e.getStatus()));
        }
    }

    @PutMapping("/role")
    public ResponseEntity<?> updateRole(@RequestBody PersonDto personDto) {
        try {
            AuthUser authUser = personService.updateRole(personDto);
            return ResponseEntity.ok(personConverter.entityToDto(authUser));
        }catch (AppError e) {
            return new  ResponseEntity<>(new AppError(e.getStatus(), e.getMessage()), HttpStatusCode.valueOf(e.getStatus()));
        }
    }


    @DeleteMapping ("/restaurant")
    public ResponseEntity<?> deleteRestaurantByEmail(@RequestBody DelRequest delRequest) {
        try {
            return ResponseEntity.ok(personService.deleteUserOrRestaurantByEmail(delRequest, "rest"));
        } catch (AppError e) {
            return new ResponseEntity<>(new AppError(e.getStatus(), e.getMessage()), HttpStatusCode.valueOf(e.getStatus()));
        }
    }

    @DeleteMapping ("/user")
    public ResponseEntity<?> deleteUserByEmail(@RequestBody DelRequest delRequest) {
        try {
            return ResponseEntity.ok(personService.deleteUserOrRestaurantByEmail(delRequest, "user"));
        } catch (AppError e){
            return new ResponseEntity<>(new AppError(e.getStatus(), e.getMessage()), HttpStatusCode.valueOf(e.getStatus()));
        }
    }



}
