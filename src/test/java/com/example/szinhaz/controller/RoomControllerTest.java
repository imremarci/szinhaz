package com.example.szinhaz.controller;

import com.example.szinhaz.model.Room;
import com.example.szinhaz.service.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomController roomController;

    @Test
    void listRooms_ReturnsViewWithRooms() {
        Model model = new ExtendedModelMap();
        when(roomService.getAllRooms()).thenReturn(List.of(new Room("Nagyterem", 5, 10)));

        String view = roomController.listRooms(model);

        assertEquals("termek", view);
        assertNotNull(model.getAttribute("rooms"));
        verify(roomService).getAllRooms();
    }
}