package com.example.szinhaz.service;

import com.example.szinhaz.model.Show;
import com.example.szinhaz.repository.ShowRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service // Ez jelzi a Springnek, hogy ez a fájl végzi a logikai műveleteket
public class ShowService {

    private final ShowRepository showRepository;

    public ShowService(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    // Ide jön a logika, amit a Controllerből kiszerveztünk
    public List<Show> getDailyShows(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        return showRepository.findByStartTimeBetween(startOfDay, endOfDay);
    }
}