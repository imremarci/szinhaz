package com.example.szinhaz.service;

import com.example.szinhaz.model.Room;
import com.example.szinhaz.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoomServiceTest {

    private RoomRepository roomRepository;
    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomRepository = Mockito.mock(RoomRepository.class);
        roomService = new RoomService(roomRepository);
    }

    @Test
    void getAllRoomsShouldReturnRepositoryResult() {
        List<Room> rooms = List.of(new Room("Nagyterem", 10, 15));
        when(roomRepository.findAll()).thenReturn(rooms);

        assertThat(roomService.getAllRooms()).isSameAs(rooms);
    }

    @Test
    void getRoomByIdShouldReturnRoomWhenPresent() {
        Room room = new Room("Nagyterem", 10, 15);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThat(roomService.getRoomById(1L)).isSameAs(room);
    }

    @Test
    void getRoomByIdShouldReturnNullWhenMissing() {
        when(roomRepository.findById(2L)).thenReturn(Optional.empty());

        assertThat(roomService.getRoomById(2L)).isNull();
    }

    @Test
    void saveRoomShouldDelegateToRepository() {
        Room room = new Room("Kamaraterem", 8, 10);

        roomService.saveRoom(room);

        verify(roomRepository).save(room);
    }

    @Test
    void deleteRoomShouldDelegateToRepository() {
        roomService.deleteRoom(6L);

        verify(roomRepository).deleteById(6L);
    }
}
