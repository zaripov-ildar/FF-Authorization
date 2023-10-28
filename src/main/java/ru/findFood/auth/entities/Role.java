package ru.findFood.auth.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "_role")
@Getter
@Setter
@NoArgsConstructor
public class Role extends BaseEntity {

    @Column(name = "title")
    @JsonBackReference
    private String title;

    @ManyToMany(mappedBy = "roles")
    public List<AuthUser> users;

    public Role(String title) {
        this.title = title;
    }


    @Override
    public String toString() {
        return "Role{" +
                "title='" + title + '\'' +
                ", users=" + users +
                ", id=" + getId() +
                '}';
    }
}
