package ru.findFood.auth.validators;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.findFood.auth.dtos.ConfirmDataRequest;
import ru.findFood.auth.exceptions.AppError;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ConfirmDataValidator {

    public void validate(ConfirmDataRequest request) throws AppError {
        if (request.getEmail() == null || request.getEmail().equals("")) {
            throw new AppError(HttpStatus.BAD_REQUEST.value(),
                    "Подтверждаемый email должен иметь значение, текущее значение email: " + request.getEmail());
        }

        if (!Pattern.matches("^[a-zA-Z\\d._%+-]+@[a-zA-Z\\d.-]+\\.[a-zA-Z]{2,}$", request.getEmail())) {
            throw new AppError(HttpStatus.BAD_REQUEST.value(),
                    "E-mail адрес введен некорректно!!!");
        }
        if (request.getPassword() == null || request.getPassword().equals("")) {
            throw new AppError(HttpStatus.BAD_REQUEST.value(),
                    "Подтверждаемый пароль должен иметь значение!!!");
        }
    }

}
