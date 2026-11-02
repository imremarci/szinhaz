package com.example.szinhaz.repository;

import com.example.szinhaz.model.Actor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActorRepository extends JpaRepository<Actor, Long> {
    // Alapértelmezett CRUD műveletek (mentés, lekérdezés, törlés) automatikusan adottak
}