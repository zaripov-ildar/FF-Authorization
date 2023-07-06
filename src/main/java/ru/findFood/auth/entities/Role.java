package ru.findFood.auth.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "_role")
@Getter
@Setter
@RequiredArgsConstructor
public class Role extends BaseEntity {

    @Column(name = "title")
    private String title;
}
