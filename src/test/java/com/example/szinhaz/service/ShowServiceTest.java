package com.example.szinhaz.service;

import com.example.szinhaz.model.Play;
import com.example.szinhaz.model.Room;
import com.example.szinhaz.model.Show;
import com.example.szinhaz.repository.ShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShowServiceTest {

    @Mock
    private ShowRepository showRepository;

    @InjectMocks
    private ShowService showService;

    private Play play;
    private Room room1;
    private Room room2;
    private Show validShow;
    private LocalDate tomorrow;

    @BeforeEach
    void setUp() {
        tomorrow = LocalDate.now().plusDays(1);

        play = new Play();
        play.setTitle("Hamlet");
        play.setDurationMinutes(120);

        room1 = new Room("Nagyterem", 5, 10);
        room1.setId(1L);

        room2 = new Room("Kisterem", 4, 8);
        room2.setId(2L);

        validShow = new Show();
        validShow.setPlay(play);
        validShow.setRoom(room1);
        validShow.setStartTime(tomorrow.atTime(18, 0));
    }

    @Test
    void getAllShows_ReturnsList() {
        when(showRepository.findAll()).thenReturn(List.of(validShow));

        List<Show> result = showService.getAllShows();

        assertEquals(1, result.size());
        verify(showRepository).findAll();
    }

    @Test
    void getShowById_Found_ReturnsShow() {
        when(showRepository.findById(1L)).thenReturn(Optional.of(validShow));

        Show result = showService.getShowById(1L);

        assertNotNull(result);
        assertEquals("Hamlet", result.getPlay().getTitle());
    }

    @Test
    void getShowById_NotFound_ReturnsNull() {
        when(showRepository.findById(99L)).thenReturn(Optional.empty());

        Show result = showService.getShowById(99L);

        assertNull(result);
    }

    @Test
    void saveShow_ValidData_SavesAndReturnsShow() {
        when(showRepository.findAll()).thenReturn(Collections.emptyList());
        when(showRepository.save(any(Show.class))).thenAnswer(i -> i.getArgument(0));

        Show saved = showService.saveShow(validShow);

        assertNotNull(saved);
        verify(showRepository, times(1)).save(validShow);
    }

    @Test
    void saveShow_MissingRequiredFields_ThrowsException() {
        Show noPlay = new Show();
        noPlay.setRoom(room1);
        noPlay.setStartTime(tomorrow.atTime(18, 0));

        Show noRoom = new Show();
        noRoom.setPlay(play);
        noRoom.setStartTime(tomorrow.atTime(18, 0));

        Show noTime = new Show();
        noTime.setPlay(play);
        noTime.setRoom(room1);

        assertThrows(IllegalArgumentException.class, () -> showService.saveShow(noPlay));
        assertThrows(IllegalArgumentException.class, () -> showService.saveShow(noRoom));
        assertThrows(IllegalArgumentException.class, () -> showService.saveShow(noTime));
        verify(showRepository, never()).save(any());
    }

    @Test
    void saveShow_PastStartTime_ThrowsException() {
        validShow.setStartTime(LocalDateTime.now().minusHours(2));

        assertThrows(IllegalArgumentException.class, () -> showService.saveShow(validShow));
        verify(showRepository, never()).save(any());
    }

    @Test
    void saveShow_OutsideOpeningHours_ThrowsException() {
        // 10:00 előtt kezdődik
        validShow.setStartTime(tomorrow.atTime(9, 30));
        assertThrows(IllegalArgumentException.class, () -> showService.saveShow(validShow));

        // 22:00 után ér véget (21:00 + 120 perc = 23:00)
        validShow.setStartTime(tomorrow.atTime(21, 0));
        assertThrows(IllegalArgumentException.class, () -> showService.saveShow(validShow));

        // Átnyúlik másnapra (23:00 + 120 perc = 01:00)
        validShow.setStartTime(tomorrow.atTime(23, 0));
        assertThrows(IllegalArgumentException.class, () -> showService.saveShow(validShow));

        verify(showRepository, never()).save(any());
    }

    @Test
    void saveShow_OverlappingInSameRoom_ThrowsException() {
        Show existing = new Show();
        existing.setId(10L);
        existing.setPlay(play);
        existing.setRoom(room1);
        existing.setStartTime(tomorrow.atTime(17, 0)); // 17:00 - 19:00

        when(showRepository.findAll()).thenReturn(List.of(existing));

        // validShow: 18:00 - 20:00 ugyanabban a teremben -> ütközés
        assertThrows(IllegalStateException.class, () -> showService.saveShow(validShow));
        verify(showRepository, never()).save(any());
    }

    @Test
    void saveShow_SameTimeDifferentRoom_SavesSuccessfully() {
        Show existing = new Show();
        existing.setId(10L);
        existing.setPlay(play);
        existing.setRoom(room2); // másik terem
        existing.setStartTime(tomorrow.atTime(18, 0));

        when(showRepository.findAll()).thenReturn(List.of(existing));
        when(showRepository.save(any(Show.class))).thenAnswer(i -> i.getArgument(0));

        Show saved = showService.saveShow(validShow);

        assertNotNull(saved);
        verify(showRepository, times(1)).save(validShow);
    }

    @Test
    void saveShow_NonOverlappingSameRoom_SavesSuccessfully() {
        Show existing = new Show();
        existing.setId(10L);
        existing.setPlay(play);
        existing.setRoom(room1);
        existing.setStartTime(tomorrow.atTime(14, 0)); // 14:00 - 16:00

        when(showRepository.findAll()).thenReturn(List.of(existing));
        when(showRepository.save(any(Show.class))).thenAnswer(i -> i.getArgument(0));

        Show saved = showService.saveShow(validShow); // 18:00 - 20:00

        assertNotNull(saved);
        verify(showRepository, times(1)).save(validShow);
    }

    @Test
    void saveShow_UpdatingSameShow_IgnoresSelfOverlapAndSaves() {
        validShow.setId(10L);

        Show existingSame = new Show();
        existingSame.setId(10L);
        existingSame.setPlay(play);
        existingSame.setRoom(room1);
        existingSame.setStartTime(tomorrow.atTime(18, 0));

        when(showRepository.findAll()).thenReturn(List.of(existingSame));
        when(showRepository.save(any(Show.class))).thenAnswer(i -> i.getArgument(0));

        Show saved = showService.saveShow(validShow);

        assertNotNull(saved);
        verify(showRepository, times(1)).save(validShow);
    }

    @Test
    void deleteShow_CallsRepositoryDelete() {
        showService.deleteShow(1L);

        verify(showRepository, times(1)).deleteById(1L);
    }
}