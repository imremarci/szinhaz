package com.example.szinhaz.service;

import com.example.szinhaz.model.Room;
import com.example.szinhaz.repository.RoomRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id).orElse(null);
    }

    public void saveRoom(Room Room) {
        roomRepository.save(Room);
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }
}