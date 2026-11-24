package com.example.szinhaz.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoomControllerTest {

    private RoomController roomController;

    @BeforeEach
    void setUp() {
        roomController = new RoomController();
    }

    @Test
    void listRoomsShouldRedirectToHome() {
        assertThat(roomController.listRooms()).isEqualTo("redirect:/fooldal");
    }
}