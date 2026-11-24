package com.example.szinhaz.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void defaultConstructorShouldCreateEmptyUserWithUserRole() {
        User user = new User();

        assertThat(user.getId()).isNull();
        assertThat(user.getName()).isNull();
        assertThat(user.getPassword()).isNull();
        assertThat(user.getFullName()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getRole()).isEqualTo("USER");
    }

    @Test
    void parameterizedConstructorShouldSetFields() {
        User user = new User("admin", "admin123", "Adminisztrator", "admin@example.com");

        assertThat(user.getName()).isEqualTo("admin");
        assertThat(user.getPassword()).isEqualTo("admin123");
        assertThat(user.getFullName()).isEqualTo("Adminisztrator");
        assertThat(user.getEmail()).isEqualTo("admin@example.com");
        assertThat(user.getRole()).isEqualTo("USER");
    }

    @Test
    void settersAndGettersShouldRoundTripEveryField() {
        User user = new User();

        user.setId(5L);
        user.setName("teszt");
        user.setPassword("jelszo");
        user.setFullName("Teszt Elek");
        user.setEmail("teszt@example.com");
        user.setRole("ADMIN");

        assertThat(user.getId()).isEqualTo(5L);
        assertThat(user.getName()).isEqualTo("teszt");
        assertThat(user.getPassword()).isEqualTo("jelszo");
        assertThat(user.getFullName()).isEqualTo("Teszt Elek");
        assertThat(user.getEmail()).isEqualTo("teszt@example.com");
        assertThat(user.getRole()).isEqualTo("ADMIN");
    }
}
