package com.webmanager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.security.PrivateKey;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "managers")
public class Manager {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String email;

    @OneToMany(mappedBy = "manager")
    private List<Employee> employees;
}
