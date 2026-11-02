package com.example.szinhaz.service;

import com.example.szinhaz.model.Actor;
import com.example.szinhaz.repository.ActorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActorService {

    private final ActorRepository actorRepository;

    public ActorService(ActorRepository actorRepository) {
        this.actorRepository = actorRepository;
    }

    public List<Actor> getAllActors() {
        return actorRepository.findAll();
    }

    public void saveActor(Actor actor) {
        actorRepository.save(actor);
    }
}