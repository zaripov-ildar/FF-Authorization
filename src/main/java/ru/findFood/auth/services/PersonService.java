package ru.findFood.auth.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.findFood.auth.entities.AuthUser;
import ru.findFood.auth.repositories.AuthUserRepository;


@Service
@RequiredArgsConstructor
public class PersonService {

    private final AuthUserRepository repository;

    public AuthUser findPersonByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Can't find the email: " + email));
    }

    public void save(AuthUser person) {
        repository.save(person);
    }

    public boolean isExistByEmail(String email) {
        return repository.existsByEmail(email);
    }

}
