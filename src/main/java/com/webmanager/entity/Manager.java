package com.webmanager.entity;

import com.webmanager.enums.Role;
import com.webmanager.utils.CpfConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false, unique = true, length = 11)
    @Convert(converter = CpfConverter.class)
    private String cpf;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    private String address;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    @OneToMany(mappedBy = "manager")
    private List<Employee> employees;
}
