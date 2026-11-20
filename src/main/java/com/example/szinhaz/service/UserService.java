package com.example.szinhaz.service;

import com.example.szinhaz.model.User;
import com.example.szinhaz.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(User user) {
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("A teljes név megadása kötelező!");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()
                || !user.getEmail().contains("@") || !user.getEmail().contains(".")) {
            throw new IllegalArgumentException("Érvényes e-mail cím megadása kötelező!");
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("A felhasználónév megadása kötelező!");
        }
        if (user.getName().trim().length() < 3) {
            throw new IllegalArgumentException("A felhasználónév legalább 3 karakter legyen!");
        }
        if (user.getPassword() == null || user.getPassword().length() < 4) {
            throw new IllegalArgumentException("A jelszó legalább 4 karakter hosszú legyen!");
        }

        String cleanName = user.getName().trim();
        String cleanEmail = user.getEmail().trim().toLowerCase();

        if (userRepository.findByName(cleanName).isPresent()) {
            throw new IllegalStateException("Ez a felhasználónév már foglalt!");
        }
        if (userRepository.findByEmail(cleanEmail).isPresent()) {
            throw new IllegalStateException("Ezzel az e-mail címmel már regisztráltak!");
        }

        user.setFullName(user.getFullName().trim());
        user.setEmail(cleanEmail);
        user.setName(cleanName);

        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }

        return userRepository.save(user);
    }

    public User login(String name, String password) {
        if (name == null || password == null) {
            throw new IllegalArgumentException("A felhasználónév és a jelszó megadása kötelező!");
        }

        Optional<User> found = userRepository.findByName(name.trim());
        if (found.isEmpty() || !found.get().getPassword().equals(password)) {
            throw new IllegalArgumentException("Hibás felhasználónév vagy jelszó!");
        }

        return found.get();
    }
}