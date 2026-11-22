package com.example.szinhaz.service;

import com.example.szinhaz.model.User;
import com.example.szinhaz.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = new User();
        validUser.setFullName("Kovács János");
        validUser.setEmail("kovacs.janos@email.hu");
        validUser.setName("kovacsj");
        validUser.setPassword("titok123");
    }

    @Test
    void registerUser_ValidData_SavesAndReturnsUser() {
        when(userRepository.findByName("kovacsj")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("kovacs.janos@email.hu")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User saved = userService.registerUser(validUser);

        assertNotNull(saved);
        assertEquals("USER", saved.getRole());
        assertEquals("kovacs.janos@email.hu", saved.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_EmptyFullName_ThrowsException() {
        validUser.setFullName("   ");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(validUser));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_InvalidEmail_ThrowsException() {
        validUser.setEmail("hibas-email");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(validUser));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_ShortUsername_ThrowsException() {
        validUser.setName("ab");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(validUser));
    }

    @Test
    void registerUser_ShortPassword_ThrowsException() {
        validUser.setPassword("123");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(validUser));
    }

    @Test
    void registerUser_DuplicateUsername_ThrowsException() {
        when(userRepository.findByName("kovacsj")).thenReturn(Optional.of(new User()));
        assertThrows(IllegalStateException.class, () -> userService.registerUser(validUser));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_DuplicateEmail_ThrowsException() {
        when(userRepository.findByName("kovacsj")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("kovacs.janos@email.hu")).thenReturn(Optional.of(new User()));
        assertThrows(IllegalStateException.class, () -> userService.registerUser(validUser));
        verify(userRepository, never()).save(any());
    }
}