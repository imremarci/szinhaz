package com.example.szinhaz.service;

import com.example.szinhaz.model.Play;
import com.example.szinhaz.repository.PlayRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PlayService {

    private final PlayRepository playRepository;

    public PlayService(PlayRepository playRepository) {
        this.playRepository = playRepository;
    }

    public List<Play> getAllPlays() {
        return playRepository.findAll();
    }

    public Play getPlayById(Long id) {
        return playRepository.findById(id).orElse(null);
    }

    public Play savePlay(Play play) {
        if (play.getTitle() == null || play.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("A színdarab címe nem lehet üres!");
        }
        if (play.getDirector() == null || play.getDirector().trim().isEmpty()) {
            throw new IllegalArgumentException("A rendező neve nem lehet üres!");
        }
        if (play.getDurationMinutes() < 15 || play.getDurationMinutes() > 360) {
            throw new IllegalArgumentException("A színdarab hossza 15 és 360 perc között lehet!");
        }
        if (play.getReleaseYear() < 1800 || play.getReleaseYear() > LocalDate.now().getYear() + 5) {
            throw new IllegalArgumentException("Érvénytelen bemutató év!");
        }
        return playRepository.save(play);
    }
    public void deletePlay(Long id) {
        playRepository.deleteById(id);
    }
}