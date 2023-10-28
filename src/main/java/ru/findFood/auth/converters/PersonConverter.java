package ru.findFood.auth.converters;



import org.springframework.stereotype.Component;
import ru.findFood.auth.dtos.PersonDto;
import ru.findFood.auth.entities.AuthUser;

import java.util.stream.Collectors;

@Component
public class PersonConverter {
    private final RoleConverter roleConverter;

    public PersonConverter(RoleConverter roleConverter) {
        this.roleConverter = roleConverter;
    }

    public AuthUser dtoToEntity(PersonDto personDto) {
        return new AuthUser(personDto.getEmail(), personDto.getPassword(), personDto.getRoles().stream().map(roleConverter::dtoToEntity).collect(Collectors.toList()));
    }

    public PersonDto entityToDto(AuthUser authUser) {
        return new PersonDto(authUser.getId(), authUser.getEmail(), "PROTECTED", authUser.getRoles().stream().map(roleConverter::entityToDto).collect(Collectors.toList()));
    }
}
