package com.webmanager.repository;

import com.webmanager.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ManagerRepository  extends JpaRepository<Manager, UUID> {

    Optional<Manager> findByName(String name);
}
