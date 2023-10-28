package ru.findFood.auth.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.findFood.auth.dtos.NewEmailRequest;
import ru.findFood.auth.dtos.NewPasswordRequest;
import ru.findFood.auth.dtos.RegMessage;
import ru.findFood.auth.exceptions.AppError;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class NewEmailValidator {
    public void validate(NewEmailRequest request) throws AppError {

        if (request.getPassword() == null || request.getPassword().equals("")) {
            throw new AppError(HttpStatus.BAD_REQUEST.value(),
                    "Пароль должен иметь значение!!!");
        }

        if (request.getEmail() == null || request.getEmail().equals("")) {
            throw new AppError(HttpStatus.BAD_REQUEST.value(),
                    "Действующий email должен иметь значение, текущее значение email: " + request.getEmail());
        }

        if (!Pattern.matches("^[a-zA-Z\\d._%+-]+@[a-zA-Z\\d.-]+\\.[a-zA-Z]{2,}$", request.getEmail())) {
            throw new AppError(HttpStatus.BAD_REQUEST.value(),
                    "Действующий email адрес введен некорректно!!!");
        }

        if (request.getNewEmail() == null || request.getNewEmail().equals("")) {
            throw new AppError(HttpStatus.BAD_REQUEST.value(),
                    "Новый email должен иметь значение, текущее значение email: " + request.getNewEmail());
        }

        if (!Pattern.matches("^[a-zA-Z\\d._%+-]+@[a-zA-Z\\d.-]+\\.[a-zA-Z]{2,}$", request.getNewEmail())) {
            throw new AppError(HttpStatus.BAD_REQUEST.value(),
                    "Новый email адрес введен некорректно!!!");
        }

        if (request.getEmail().equals(request.getNewEmail())) {
            throw new AppError(HttpStatus.PRECONDITION_FAILED.value(),
                    "Введенные адреса электронной почты совпадают!!!");
        }
    }
}
