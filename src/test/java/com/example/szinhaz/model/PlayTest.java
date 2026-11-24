package com.example.szinhaz.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlayTest {

    @Test
    void defaultConstructorShouldCreateEmptyPlayWithEmptyActorList() {
        Play play = new Play();

        assertThat(play.getId()).isNull();
        assertThat(play.getTitle()).isNull();
        assertThat(play.getDirector()).isNull();
        assertThat(play.getDurationMinutes()).isNull();
        assertThat(play.getReleaseYear()).isNull();
        assertThat(play.getDescription()).isNull();
        assertThat(play.getCoverImageUrl()).isNull();
        assertThat(play.getActors()).isEmpty();
    }

    @Test
    void parameterizedConstructorShouldSetEveryField() {
        Play play = new Play("Hamlet", "William Shakespeare", 160, 2024, "Tragedia", "hamlet.png");

        assertThat(play.getTitle()).isEqualTo("Hamlet");
        assertThat(play.getDirector()).isEqualTo("William Shakespeare");
        assertThat(play.getDurationMinutes()).isEqualTo(160);
        assertThat(play.getReleaseYear()).isEqualTo(2024);
        assertThat(play.getDescription()).isEqualTo("Tragedia");
        assertThat(play.getCoverImageUrl()).isEqualTo("hamlet.png");
    }

    @Test
    void settersAndGettersShouldRoundTripEveryField() {
        Play play = new Play();
        List<Actor> actors = List.of(new Actor("Nagy Ervin", 4, "kep.png"));

        play.setId(1L);
        play.setTitle("A dzsungel konyve");
        play.setDirector("Des Laszlo");
        play.setDurationMinutes(145);
        play.setReleaseYear(2025);
        play.setDescription("Zenés darab");
        play.setCoverImageUrl("dzsungel.png");
        play.setActors(actors);

        assertThat(play.getId()).isEqualTo(1L);
        assertThat(play.getTitle()).isEqualTo("A dzsungel konyve");
        assertThat(play.getDirector()).isEqualTo("Des Laszlo");
        assertThat(play.getDurationMinutes()).isEqualTo(145);
        assertThat(play.getReleaseYear()).isEqualTo(2025);
        assertThat(play.getDescription()).isEqualTo("Zenés darab");
        assertThat(play.getCoverImageUrl()).isEqualTo("dzsungel.png");
        assertThat(play.getActors()).isSameAs(actors);
    }
}
