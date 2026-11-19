package com.example.szinhaz.config;

import com.example.szinhaz.model.*;
import com.example.szinhaz.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final PlayRepository playRepository;
    private final ActorRepository actorRepository;
    private final RoomRepository roomRepository;
    private final ShowRepository showRepository;
    private final UserRepository userRepository;

    public DatabaseSeeder(PlayRepository playRepository, ActorRepository actorRepository,
                          RoomRepository roomRepository, ShowRepository showRepository, UserRepository userRepository) {
        this.playRepository = playRepository;
        this.actorRepository = actorRepository;
        this.roomRepository = roomRepository;
        this.showRepository = showRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (playRepository.count() == 0) {
            Room nagyterem = new Room("Zsiráf Teátrum, Nagyterem", 10, 15);
            Room kamaraterem = new Room("Zsiráf Fórum, Kamaraterem", 8, 10);
            Room studioterem = new Room("Zsiráf Fórum, Stúdióterem", 7, 8);

            roomRepository.save(kamaraterem);
            roomRepository.save(studioterem);
            roomRepository.save(nagyterem);

            Play hamlet = new Play(
                    "Hamlet", "William Shakespeare", 160, 2024,
                    "Shakespeare klasszikus tragédiája a bosszúról és az őrületről.",
                    "https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?w=400"
            );
            Play dzsungel = new Play(
                    "A dzsungel könyve", "Dés László - Geszti Péter", 145, 2025,
                    "Maugli és a farkasok története lenyűgöző zenés darabban.",
                    "https://images.unsplash.com/photo-1514306191717-452ec28c7814?w=400"
            );
            playRepository.save(hamlet);
            playRepository.save(dzsungel);

            Actor ervin = new Actor("Nagy Ervin", 4,"https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400");
            Actor eszter = new Actor("Ónodi Eszter", 5, "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400");
            actorRepository.save(ervin);
            actorRepository.save(eszter);

            Show eloadas1 = new Show(LocalDateTime.of(2026, 11, 20, 19, 0), nagyterem, hamlet);
            Show eloadas2 = new Show(LocalDateTime.of(2026, 11, 22, 18, 0), nagyterem, dzsungel);
            Show eloadas3 = new Show(LocalDateTime.of(2026, 11, 18, 18, 0), nagyterem, dzsungel);
            showRepository.save(eloadas1);
            showRepository.save(eloadas2);
            showRepository.save(eloadas3);

            if (userRepository.findByName("admin").isEmpty()) {
                User admin = new User();
                admin.setName("admin");
                admin.setPassword("admin123"); // nyugodtan írd át másra
                admin.setRole("ADMIN");
                userRepository.save(admin);
            }
        }
    }
}