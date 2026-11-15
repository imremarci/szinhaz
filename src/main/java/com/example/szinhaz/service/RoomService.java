package com.example.szinhaz.service;

import com.example.szinhaz.model.Show;
import com.example.szinhaz.repository.ShowRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RoomService {

    private final ShowRepository showRepository;

    public RoomService(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    public List<Show> getAllRooms() {
        return showRepository.findAll();
    }

    public Show getRoomById(Long id) {
        return showRepository.findById(id).orElse(null);
    }
}