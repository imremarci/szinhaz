package com.example.szinhaz.service;

import com.example.szinhaz.model.Show;
import com.example.szinhaz.repository.ShowRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ActorService {

    private final ShowRepository showRepository;

    public ActorService(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    public List<Show> getAllActor() {
        return showRepository.findAll();
    }

    public Show getActorById(Long id) {
        return showRepository.findById(id).orElse(null);
    }
}