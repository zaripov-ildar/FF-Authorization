package ru.findFood.auth.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewRestaurantAddedToDb {
    private String title;
    private String email;
}
