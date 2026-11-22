package com.example.szinhaz.service;

import com.example.szinhaz.model.Room;
import com.example.szinhaz.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room("Nagyterem", 5, 10);
    }

    @Test
    void getAllRooms_ReturnsList() {
        when(roomRepository.findAll()).thenReturn(List.of(room));

        List<Room> result = roomService.getAllRooms();

        assertEquals(1, result.size());
        verify(roomRepository).findAll();
    }

    @Test
    void getRoomById_Found_ReturnsRoom() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        Room result = roomService.getRoomById(1L);

        assertNotNull(result);
        assertEquals("Nagyterem", result.getName());
    }

    @Test
    void getRoomById_NotFound_ReturnsNull() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(roomService.getRoomById(99L));
    }

    @Test
    void saveRoom_CallsRepositorySave() {
        roomService.saveRoom(room);

        verify(roomRepository, times(1)).save(room);
    }

    @Test
    void deleteRoom_CallsRepositoryDelete() {
        roomService.deleteRoom(1L);

        verify(roomRepository, times(1)).deleteById(1L);
    }
}