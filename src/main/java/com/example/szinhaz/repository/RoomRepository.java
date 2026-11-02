package com.example.szinhaz.repository;

import com.example.szinhaz.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    // Ide nem kell egyedi metódus, a findAll() alapból benne van
}