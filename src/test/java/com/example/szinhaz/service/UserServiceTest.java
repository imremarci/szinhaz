package com.example.szinhaz.service;

import com.example.szinhaz.model.User;
import com.example.szinhaz.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    private User validUser() {
        return new User("anna", "Jelszo123", "Kiss Anna", "anna@example.com");
    }

    private void stubUniqueCredentials() {
        when(userRepository.findByName(any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
    }

    @Test
    void registerUserShouldPersistValidUser() {
        User user = validUser();
        stubUniqueCredentials();
        when(userRepository.save(user)).thenReturn(user);

        assertThat(userService.registerUser(user)).isSameAs(user);
        verify(userRepository).save(user);
    }

    @Test
    void registerUserShouldTrimFieldsAndLowercaseEmail() {
        User user = new User("  anna  ", "Jelszo123", "  Kiss Anna  ", "  Anna@Example.COM  ");
        stubUniqueCredentials();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.registerUser(user);

        assertThat(saved.getName()).isEqualTo("anna");
        assertThat(saved.getFullName()).isEqualTo("Kiss Anna");
        assertThat(saved.getEmail()).isEqualTo("anna@example.com");
        assertThat(saved.getRole()).isEqualTo("USER");
    }

    @Test
    void registerUserShouldKeepExistingRole() {
        User user = validUser();
        user.setRole("ADMIN");
        stubUniqueCredentials();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(userService.registerUser(user).getRole()).isEqualTo("ADMIN");
    }

    @Test
    void registerUserShouldDefaultNullRoleToUser() {
        User user = validUser();
        user.setRole(null);
        stubUniqueCredentials();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(userService.registerUser(user).getRole()).isEqualTo("USER");
    }

    @Test
    void registerUserShouldDefaultEmptyRoleToUser() {
        User user = validUser();
        user.setRole("");
        stubUniqueCredentials();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(userService.registerUser(user).getRole()).isEqualTo("USER");
    }

    @Test
    void registerUserShouldRejectNullFullName() {
        User user = validUser();
        user.setFullName(null);

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A teljes név megadása kötelező!");
    }

    @Test
    void registerUserShouldRejectBlankFullName() {
        User user = validUser();
        user.setFullName("   ");

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A teljes név megadása kötelező!");
    }

    @Test
    void registerUserShouldRejectNullEmail() {
        User user = validUser();
        user.setEmail(null);

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Érvényes e-mail cím megadása kötelező!");
    }

    @Test
    void registerUserShouldRejectBlankEmail() {
        User user = validUser();
        user.setEmail("   ");

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Érvényes e-mail cím megadása kötelező!");
    }

    @Test
    void registerUserShouldRejectEmailWithoutAtSign() {
        User user = validUser();
        user.setEmail("anna.example.com");

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Érvényes e-mail cím megadása kötelező!");
    }

    @Test
    void registerUserShouldRejectEmailWithoutDot() {
        User user = validUser();
        user.setEmail("anna@example");

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Érvényes e-mail cím megadása kötelező!");
    }

    @Test
    void registerUserShouldRejectNullName() {
        User user = validUser();
        user.setName(null);

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A felhasználónév megadása kötelező!");
    }

    @Test
    void registerUserShouldRejectBlankName() {
        User user = validUser();
        user.setName("   ");

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A felhasználónév megadása kötelező!");
    }

    @Test
    void registerUserShouldRejectTooShortName() {
        User user = validUser();
        user.setName("ab");

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A felhasználónév legalább 3 karakter legyen!");
    }

    @Test
    void registerUserShouldRejectNullPassword() {
        User user = validUser();
        user.setPassword(null);

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A jelszó legalább 8 karakter hosszú legyen!");
    }

    @Test
    void registerUserShouldRejectTooShortPassword() {
        User user = validUser();
        user.setPassword("123");

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A jelszó legalább 8 karakter hosszú legyen!");
    }

    @Test
    void registerUserShouldRejectTakenName() {
        User user = validUser();
        when(userRepository.findByName("anna")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Ez a felhasználónév már foglalt!");
    }

    @Test
    void registerUserShouldRejectTakenEmail() {
        User user = validUser();
        when(userRepository.findByName("anna")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("anna@example.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Ezzel az e-mail címmel már regisztráltak!");
    }

    @Test
    void registerUserShouldNotSaveInvalidUser() {
        User user = validUser();
        user.setPassword("1");

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginShouldRejectNullName() {
        assertThatThrownBy(() -> userService.login(null, "jelszo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A felhasználónév és a jelszó megadása kötelező!");
    }

    @Test
    void loginShouldRejectNullPassword() {
        assertThatThrownBy(() -> userService.login("anna", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A felhasználónév és a jelszó megadása kötelező!");
    }

    @Test
    void loginShouldRejectUnknownUser() {
        when(userRepository.findByName("anna")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login("anna", "jelszo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Hibás felhasználónév vagy jelszó!");
    }

    @Test
    void loginShouldRejectWrongPassword() {
        User user = validUser();
        when(userRepository.findByName("anna")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.login("anna", "rossz"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Hibás felhasználónév vagy jelszó!");
    }

    @Test
    void loginShouldReturnUserForCorrectCredentials() {
        User user = validUser();
        when(userRepository.findByName("anna")).thenReturn(Optional.of(user));

        assertThat(userService.login("  anna  ", "Jelszo123")).isSameAs(user);
    }

    @Test
    void registerUserShouldRejectPasswordWithoutLowercase() {
        User user = validUser();
        user.setPassword("JELSZO123");

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A jelszónak tartalmaznia kell kisbetűt, nagybetűt és számot!");
    }

    @Test
    void registerUserShouldRejectPasswordWithoutUppercase() {
        User user = validUser();
        user.setPassword("jelszo123");

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A jelszónak tartalmaznia kell kisbetűt, nagybetűt és számot!");
    }

    @Test
    void registerUserShouldRejectPasswordWithoutDigit() {
        User user = validUser();
        user.setPassword("JelszoABC");

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A jelszónak tartalmaznia kell kisbetűt, nagybetűt és számot!");
    }

    @Test
    void registerUserShouldAcceptPasswordWithPunctuation() {
        User user = validUser();
        user.setPassword("Jelszo123!");
        stubUniqueCredentials();
        when(userRepository.save(user)).thenReturn(user);

        assertThat(userService.registerUser(user)).isSameAs(user);
    }

    @Test
    void registerUserShouldRejectTooLongFullName() {
        User user = validUser();
        user.setFullName("N".repeat(256));

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A teljes név legfeljebb 255 karakter lehet!");
    }

    @Test
    void registerUserShouldRejectTooLongEmail() {
        User user = validUser();
        user.setEmail("a".repeat(250) + "@example.com");

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Az e-mail cím legfeljebb 255 karakter lehet!");
    }

    @Test
    void registerUserShouldRejectTooLongName() {
        User user = validUser();
        user.setName("n".repeat(256));

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A felhasználónév legfeljebb 255 karakter lehet!");
    }

    @Test
    void registerUserShouldRejectTooLongPassword() {
        User user = validUser();
        user.setPassword("Jelszo123" + "x".repeat(250));

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A jelszó legfeljebb 255 karakter lehet!");
    }
}
