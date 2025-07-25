package com.example.demo.repositories;

import com.example.demo.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users,Long> {
    Optional<Users> findByEmail(String email);
    Optional<Users> findByCuid(String cuid);
}
