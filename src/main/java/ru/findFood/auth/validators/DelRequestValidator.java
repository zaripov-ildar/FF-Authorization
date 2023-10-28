package ru.findFood.auth.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.findFood.auth.dtos.DelRequest;
import ru.findFood.auth.exceptions.AppError;
import ru.findFood.auth.integrations.RestaurantsServiceIntegration;

@Component
@RequiredArgsConstructor
public class DelRequestValidator {
    public final RestaurantsServiceIntegration restServiceIntegration;

    public void validate(DelRequest delRequest) throws AppError {
        if (delRequest.getId() == null) {
            throw new AppError(HttpStatus.BAD_REQUEST.value(),
                    "ID удаляемого объекта должен иметь значение, текущее значение ID: " + delRequest.getId());
        }
        if (delRequest.getEmail() == null || delRequest.getEmail().equals("")) {
            throw new AppError(HttpStatus.BAD_REQUEST.value(),
                    "Email  удаляемого объекта  должен иметь значение, текущее значение email: " + delRequest.getEmail());
        }
    }
}
