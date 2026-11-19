package com.example.szinhaz.service;

import com.example.szinhaz.model.Show;
import com.example.szinhaz.repository.ShowRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ShowService {

    private final ShowRepository showRepository;

    public ShowService(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    public List<Show> getAllShows() {
        return showRepository.findAll();
    }

    public Show getShowById(Long id) {
        return showRepository.findById(id).orElse(null);
    }

    public Show saveShow(Show newShow) {
        if (newShow.getPlay() == null || newShow.getRoom() == null || newShow.getStartTime() == null) {
            throw new IllegalArgumentException("A színdarab, a terem és a kezdési időpont megadása kötelező!");
        }

        LocalDateTime start = newShow.getStartTime();
        LocalDateTime end = start.plusMinutes(newShow.getPlay().getDurationMinutes());

        if (start.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Múltbeli időpontra nem lehet előadást rögzíteni!");
        }

        LocalTime openTime = LocalTime.of(10, 0);
        LocalTime closeTime = LocalTime.of(22, 0);
        if (start.toLocalTime().isBefore(openTime)
                || end.toLocalTime().isAfter(closeTime)
                || !end.toLocalDate().equals(start.toLocalDate())) {
            throw new IllegalArgumentException("Az előadásnak 10:00 és 22:00 közé kell esnie!");
        }

        for (Show existing : showRepository.findAll()) {
            if (newShow.getId() != null && newShow.getId().equals(existing.getId())) {
                continue;
            }
            if (existing.getRoom().getId().equals(newShow.getRoom().getId())) {
                LocalDateTime exStart = existing.getStartTime();
                LocalDateTime exEnd = exStart.plusMinutes(existing.getPlay().getDurationMinutes());

                if (start.isBefore(exEnd) && end.isAfter(exStart)) {
                    throw new IllegalStateException(
                            "Ütközés! Ebben a teremben már fut előadás ekkor: " + existing.getPlay().getTitle()
                    );
                }
            }
        }

        return showRepository.save(newShow);
    }

    public void deleteShow(Long id) {
        showRepository.deleteById(id);
    }
}