package ru.findFood.auth.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.findFood.auth.dtos.RegMessage;
import ru.findFood.auth.dtos.RegRestaurantRequest;
import ru.findFood.auth.integrations.RestaurantsServiceIntegration;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class RestaurantInputValidator {
    public final RestaurantsServiceIntegration restServiceIntegration;

    public ResponseEntity<RegMessage> validate(RegRestaurantRequest reg_request) {
        if (!reg_request.getPassword().equals(reg_request.getConfirmPassword())) {
            return new ResponseEntity<>(new RegMessage(HttpStatus.PRECONDITION_FAILED,
                    "Введенные пароли не совпадают!!!"), HttpStatus.PRECONDITION_FAILED);
        }

        if (!restServiceIntegration.isRestTitleFree(reg_request.getTitle())) {
            return new ResponseEntity<>(new RegMessage(HttpStatus.PRECONDITION_FAILED,
                    "Ресторан с таким названием уже существует!!!"), HttpStatus.PRECONDITION_FAILED);
        }

        if (!Pattern.matches("^[a-zA-Z\\d._%+-]+@[a-zA-Z\\d.-]+\\.[a-zA-Z]{2,}$", reg_request.getEmail())) {
            return new ResponseEntity<>(new RegMessage(HttpStatus.BAD_REQUEST,
                    "E-mail адрес введен некорректно!!!"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }
}
