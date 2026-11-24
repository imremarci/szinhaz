package com.example.szinhaz.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActorTest {

    @Test
    void defaultConstructorShouldCreateEmptyActor() {
        Actor actor = new Actor();

        assertThat(actor.getId()).isNull();
        assertThat(actor.getName()).isNull();
        assertThat(actor.getRating()).isZero();
        assertThat(actor.getProfileImageUrl()).isNull();
    }

    @Test
    void parameterizedConstructorShouldSetFields() {
        Actor actor = new Actor("Nagy Ervin", 4, "https://example.com/ervin.png");

        assertThat(actor.getName()).isEqualTo("Nagy Ervin");
        assertThat(actor.getRating()).isEqualTo(4.0);
        assertThat(actor.getProfileImageUrl()).isEqualTo("https://example.com/ervin.png");
    }

    @Test
    void settersAndGettersShouldRoundTripEveryField() {
        Actor actor = new Actor();

        actor.setId(7L);
        actor.setName("Onodi Eszter");
        actor.setRating(4.5);
        actor.setProfileImageUrl("https://example.com/eszter.png");

        assertThat(actor.getId()).isEqualTo(7L);
        assertThat(actor.getName()).isEqualTo("Onodi Eszter");
        assertThat(actor.getRating()).isEqualTo(4.5);
        assertThat(actor.getProfileImageUrl()).isEqualTo("https://example.com/eszter.png");
    }
}
