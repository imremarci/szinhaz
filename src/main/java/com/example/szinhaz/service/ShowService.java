package com.example.szinhaz.service;

import com.example.szinhaz.model.Show;
import com.example.szinhaz.repository.ShowRepository;
import org.springframework.stereotype.Service;
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

    public void saveShow(Show Show) {
        showRepository.save(Show);
    }

    public void deleteShow(Long id) {
        showRepository.deleteById(id);
    }
}