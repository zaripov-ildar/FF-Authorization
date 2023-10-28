package ru.findFood.auth.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegUserRequest {
    private String email;
    private String name;
    private String password;
    private String confirmPassword;
}
