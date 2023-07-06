package ru.findFood.auth.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "auth_user")
@Getter
@Setter
@RequiredArgsConstructor
public class AuthUser  extends BaseEntity {

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;
}