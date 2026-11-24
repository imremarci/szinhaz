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

    public Actor getActorById(Long id) {
        return actorRepository.findById(id).orElse(null);
    }

    public Actor saveActor(Actor actor) {
        if (actor.getName() == null || actor.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("A színész neve nem lehet üres!");
        }
        actor.setName(actor.getName().trim());
        if (actor.getName().length() > 255) {
            throw new IllegalArgumentException("A színész neve legfeljebb 255 karakter lehet!");
        }
        if (actor.getRating() < 1.0 || actor.getRating() > 5.0) {
            throw new IllegalArgumentException("Az értékelésnek 1.0 és 5.0 közé kell esnie!");
        }
        return actorRepository.save(actor);
    }

    public void deleteActor(Long id) {
        actorRepository.deleteById(id);
    }
}