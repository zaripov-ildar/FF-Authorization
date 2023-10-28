package ru.findFood.auth.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewPasswordRequest {
    private String email;
    private String password;
    private String newPassword;
    private String confirmPassword;

}
