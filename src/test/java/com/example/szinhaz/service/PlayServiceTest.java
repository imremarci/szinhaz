package com.example.szinhaz.service;

import com.example.szinhaz.model.Play;
import com.example.szinhaz.repository.PlayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayServiceTest {

    @Mock
    private PlayRepository playRepository;

    @InjectMocks
    private PlayService playService;

    private Play validPlay;

    @BeforeEach
    void setUp() {
        validPlay = new Play();
        validPlay.setTitle("Hamlet");
        validPlay.setDirector("Alföldi Róbert");
        validPlay.setDurationMinutes(120);
        validPlay.setReleaseYear(2024);
    }

    @Test
    void getAllPlays_ReturnsList() {
        when(playRepository.findAll()).thenReturn(List.of(validPlay));

        List<Play> result = playService.getAllPlays();

        assertEquals(1, result.size());
        verify(playRepository).findAll();
    }

    @Test
    void getPlayById_Found_ReturnsPlay() {
        when(playRepository.findById(1L)).thenReturn(Optional.of(validPlay));

        Play result = playService.getPlayById(1L);

        assertNotNull(result);
        assertEquals("Hamlet", result.getTitle());
    }

    @Test
    void getPlayById_NotFound_ReturnsNull() {
        when(playRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(playService.getPlayById(99L));
    }

    @Test
    void savePlay_ValidData_SavesAndReturnsPlay() {
        when(playRepository.save(any(Play.class))).thenAnswer(i -> i.getArgument(0));

        Play saved = playService.savePlay(validPlay);

        assertNotNull(saved);
        assertEquals("Hamlet", saved.getTitle());
        verify(playRepository, times(1)).save(validPlay);
    }

    @Test
    void savePlay_EmptyOrNullTitle_ThrowsException() {
        validPlay.setTitle(null);
        assertThrows(IllegalArgumentException.class, () -> playService.savePlay(validPlay));

        validPlay.setTitle("   ");
        assertThrows(IllegalArgumentException.class, () -> playService.savePlay(validPlay));
        verify(playRepository, never()).save(any());
    }

    @Test
    void savePlay_EmptyOrNullDirector_ThrowsException() {
        validPlay.setDirector(null);
        assertThrows(IllegalArgumentException.class, () -> playService.savePlay(validPlay));

        validPlay.setDirector("   ");
        assertThrows(IllegalArgumentException.class, () -> playService.savePlay(validPlay));
        verify(playRepository, never()).save(any());
    }

    @Test
    void savePlay_InvalidDuration_ThrowsException() {
        validPlay.setDurationMinutes(null);
        assertThrows(IllegalArgumentException.class, () -> playService.savePlay(validPlay));

        validPlay.setDurationMinutes(10);
        assertThrows(IllegalArgumentException.class, () -> playService.savePlay(validPlay));

        validPlay.setDurationMinutes(400);
        assertThrows(IllegalArgumentException.class, () -> playService.savePlay(validPlay));
        verify(playRepository, never()).save(any());
    }

    @Test
    void savePlay_InvalidReleaseYear_ThrowsException() {
        validPlay.setReleaseYear(1799);
        assertThrows(IllegalArgumentException.class, () -> playService.savePlay(validPlay));

        validPlay.setReleaseYear(LocalDate.now().getYear() + 10);
        assertThrows(IllegalArgumentException.class, () -> playService.savePlay(validPlay));
        verify(playRepository, never()).save(any());
    }

    @Test
    void deletePlay_CallsRepositoryDelete() {
        playService.deletePlay(1L);

        verify(playRepository, times(1)).deleteById(1L);
    }
}