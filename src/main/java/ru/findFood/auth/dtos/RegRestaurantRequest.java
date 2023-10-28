package ru.findFood.auth.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegRestaurantRequest {
    private String email;
    private String title;
    private String password;
    private String confirmPassword;
}
