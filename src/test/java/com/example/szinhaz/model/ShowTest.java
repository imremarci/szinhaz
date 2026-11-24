package com.example.szinhaz.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ShowTest {

    @Test
    void defaultConstructorShouldCreateEmptyShow() {
        Show show = new Show();

        assertThat(show.getId()).isNull();
        assertThat(show.getStartTime()).isNull();
        assertThat(show.getRoom()).isNull();
        assertThat(show.getPlay()).isNull();
    }

    @Test
    void parameterizedConstructorShouldSetFields() {
        LocalDateTime start = LocalDateTime.of(2030, 5, 1, 19, 0);
        Room room = new Room("Nagyterem", 10, 15);
        Play play = new Play("Hamlet", "Shakespeare", 160, 2024, "Leiras", "kep.png");

        Show show = new Show(start, room, play);

        assertThat(show.getStartTime()).isEqualTo(start);
        assertThat(show.getRoom()).isSameAs(room);
        assertThat(show.getPlay()).isSameAs(play);
    }

    @Test
    void settersAndGettersShouldRoundTripEveryField() {
        Show show = new Show();
        LocalDateTime start = LocalDateTime.of(2031, 6, 2, 18, 30);
        Room room = new Room("Kamaraterem", 8, 10);
        Play play = new Play("Dzsungel", "Des", 145, 2025, "Leiras", "kep.png");

        show.setId(11L);
        show.setStartTime(start);
        show.setRoom(room);
        show.setPlay(play);

        assertThat(show.getId()).isEqualTo(11L);
        assertThat(show.getStartTime()).isEqualTo(start);
        assertThat(show.getRoom()).isSameAs(room);
        assertThat(show.getPlay()).isSameAs(play);
    }
}
