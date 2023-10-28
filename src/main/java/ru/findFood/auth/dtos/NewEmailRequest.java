package ru.findFood.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewEmailRequest {
    private String password;
    private String email;
    private String newEmail;
}
