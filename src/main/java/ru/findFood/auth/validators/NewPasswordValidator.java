package ru.findFood.auth.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.findFood.auth.dtos.NewPasswordRequest;
import ru.findFood.auth.exceptions.AppError;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class NewPasswordValidator {
    public void validate(NewPasswordRequest request) throws AppError {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppError(HttpStatus.BAD_REQUEST.value(),
                    "Введенные новые пароли не совпадают!!!");
        }

        if (request.getEmail() == null || request.getEmail().equals("")) {
            throw new AppError(HttpStatus.BAD_REQUEST.value(),
                    "Email должен иметь значение, текущее значение email: " + request.getEmail());
        }

        if (!Pattern.matches("^[a-zA-Z\\d._%+-]+@[a-zA-Z\\d.-]+\\.[a-zA-Z]{2,}$", request.getEmail())) {
            throw new AppError(HttpStatus.BAD_REQUEST.value(),
                    "E-mail адрес введен некорректно!!!");
        }

        if (request.getPassword() == null || request.getPassword().equals("")) {
            throw new AppError(HttpStatus.BAD_REQUEST.value(),
                    "Пароль должен иметь значение!!!");
        }
    }
}
