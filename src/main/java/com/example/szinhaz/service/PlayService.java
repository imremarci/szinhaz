package com.example.szinhaz.service;

import com.example.szinhaz.model.Play;
import com.example.szinhaz.repository.PlayRepository;
import org.springframework.stereotype.Service;

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
}