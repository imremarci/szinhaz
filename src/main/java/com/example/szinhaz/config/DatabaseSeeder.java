package com.example.szinhaz.config;

import com.example.szinhaz.model.Actor;
import com.example.szinhaz.model.Play;
import com.example.szinhaz.model.Room;
import com.example.szinhaz.repository.ActorRepository;
import com.example.szinhaz.repository.PlayRepository;
import com.example.szinhaz.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseSeeder implements CommandLineRunner {

    private final RoomRepository roomRepository;
    private final PlayRepository playRepository;
    private final ActorRepository actorRepository;

    public DatabaseSeeder(RoomRepository roomRepository, PlayRepository playRepository, ActorRepository actorRepository) {
        this.roomRepository = roomRepository;
        this.playRepository = playRepository;
        this.actorRepository = actorRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Termek létrehozása
        if (roomRepository.count() == 0) {
            roomRepository.save(new Room("Nagyterem", 20, 25));
            roomRepository.save(new Room("Kisterem", 10, 15));
            roomRepository.save(new Room("Stúdió", 5, 10));
            System.out.println("Termek betöltve.");
        }

        // 2. Színdarabok létrehozása
        if (playRepository.count() == 0) {
            playRepository.save(new Play("Hamlet", "William Shakespeare", 180));
            playRepository.save(new Play("Az ügynök halála", "Arthur Miller", 150));
            System.out.println("Darabok betöltve.");
        }

        // 3. Színészek létrehozása
        if (actorRepository.count() == 0) {
            actorRepository.save(new Actor("Nagy Ervin", 9));
            actorRepository.save(new Actor("Csányi Sándor", 8));
            System.out.println("Színészek betöltve.");
        }
    }
}