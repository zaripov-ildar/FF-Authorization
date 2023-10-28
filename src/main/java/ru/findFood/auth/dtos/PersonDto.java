package ru.findFood.auth.dtos;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.findFood.auth.entities.Role;

import java.util.Collection;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonDto {
    private Long id;
    private String email;
    private String password;
    private Collection<RoleDto> roles;

}
