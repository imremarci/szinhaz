package com.example.szinhaz.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoomTest {

    @Test
    void defaultConstructorShouldCreateEmptyRoom() {
        Room room = new Room();

        assertThat(room.getId()).isNull();
        assertThat(room.getName()).isNull();
        assertThat(room.getNumberOfRows()).isZero();
        assertThat(room.getNumberOfSeats()).isZero();
    }

    @Test
    void parameterizedConstructorShouldSetFields() {
        Room room = new Room("Zsiraf Teatrum, Nagyterem", 10, 15);

        assertThat(room.getName()).isEqualTo("Zsiraf Teatrum, Nagyterem");
        assertThat(room.getNumberOfRows()).isEqualTo(10);
        assertThat(room.getNumberOfSeats()).isEqualTo(15);
    }

    @Test
    void settersAndGettersShouldRoundTripEveryField() {
        Room room = new Room();

        room.setId(3L);
        room.setName("Kamaraterem");
        room.setNumberOfRows(8);
        room.setNumberOfSeats(10);

        assertThat(room.getId()).isEqualTo(3L);
        assertThat(room.getName()).isEqualTo("Kamaraterem");
        assertThat(room.getNumberOfRows()).isEqualTo(8);
        assertThat(room.getNumberOfSeats()).isEqualTo(10);
    }
}
