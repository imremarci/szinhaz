package com.example.szinhaz.repository;

import com.example.szinhaz.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String Name);

    Optional<User> findByEmail(String email);
}