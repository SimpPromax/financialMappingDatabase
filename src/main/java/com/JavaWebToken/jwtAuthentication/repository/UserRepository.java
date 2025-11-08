package com.JavaWebToken.jwtAuthentication.repository;

import com.JavaWebToken.jwtAuthentication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Spring Data JPA repository for User entity
public interface UserRepository extends JpaRepository<User, Long> {
    // Finds a user by username (returns Optional to handle null cases)
    Optional<User> findByUsername(String username);

    // Checks if a username already exists in the database
    boolean existsByUsername(String username);

}