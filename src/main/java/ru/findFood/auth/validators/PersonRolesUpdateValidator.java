package ru.findFood.auth.validators;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.findFood.auth.converters.RoleConverter;
import ru.findFood.auth.dtos.RoleDto;
import ru.findFood.auth.exceptions.AppError;
import ru.findFood.auth.repositories.RoleRepository;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class PersonRolesUpdateValidator {
    public final RoleRepository roleRepository;
    public final RoleConverter roleConverter;
    RoleDto role;
    public void validate(Collection<RoleDto> roles) throws AppError {
        if (roles.size() == 0){
            throw new AppError(HttpStatus.BAD_REQUEST.value(),
                    "У пользователя должна оставаться роль CLIENT, текущее количество ролей пользователя: " + 0);
        }
        if((roles.contains(roleConverter.entityToDto(roleRepository.findByTitle("CLIENT").get())) ||
                roles.contains(roleConverter.entityToDto(roleRepository.findByTitle("NUTRITIONIST").get()))) &&
                roles.contains(roleConverter.entityToDto(roleRepository.findByTitle("RESTAURANT").get())) &&
                !roles.contains(roleConverter.entityToDto(roleRepository.findByTitle("ADMIN").get()))){
            throw new AppError(HttpStatus.PRECONDITION_FAILED.value(),
                    "Пользователь (диетолог) не может быть одновременно пользователем и рестораном!!!\n" +
                            "Необходимо добавить роль администратора или удалить роль ресторана!!!");

        }
    }
}
