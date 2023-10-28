package ru.findFood.auth.converters;


import org.springframework.stereotype.Component;
import ru.findFood.auth.dtos.RoleDto;
import ru.findFood.auth.entities.Role;
import ru.findFood.auth.exceptions.ResourceNotFoundException;
import ru.findFood.auth.repositories.RoleRepository;

@Component
public class RoleConverter {
    private final RoleRepository roleRepository;

    public RoleConverter(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role dtoToEntity(RoleDto roleDto) {
        return roleRepository.findByTitle(roleDto.getTitle())
                .orElseThrow(()-> new ResourceNotFoundException("Роль "  + roleDto.getTitle() + " ролей!!!"));
    }

    public RoleDto entityToDto(Role role) {
        return new RoleDto(role.getId(), role.getTitle());
    }
}
