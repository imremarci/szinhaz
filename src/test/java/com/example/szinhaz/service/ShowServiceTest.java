package com.example.szinhaz.service;

import com.example.szinhaz.model.Play;
import com.example.szinhaz.model.Room;
import com.example.szinhaz.model.Show;
import com.example.szinhaz.repository.ShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShowServiceTest {

    private ShowRepository showRepository;
    private ShowService showService;

    @BeforeEach
    void setUp() {
        showRepository = Mockito.mock(ShowRepository.class);
        showService = new ShowService(showRepository);
    }

    private Room room(Long id) {
        Room room = new Room("Terem " + id, 10, 15);
        room.setId(id);
        return room;
    }

    private Play play(Long id, int durationMinutes) {
        Play play = new Play("Darab " + id, "Rendezo", durationMinutes, 2024, "Leiras", "kep.png");
        play.setId(id);
        return play;
    }

    private LocalDateTime tomorrowAt(int hour, int minute) {
        return LocalDate.now().plusDays(1).atTime(hour, minute);
    }

    @Test
    void getAllShowsShouldReturnRepositoryResult() {
        List<Show> shows = List.of(new Show());
        when(showRepository.findAll()).thenReturn(shows);

        assertThat(showService.getAllShows()).isSameAs(shows);
    }

    @Test
    void getShowByIdShouldReturnShowWhenPresent() {
        Show show = new Show();
        when(showRepository.findById(1L)).thenReturn(Optional.of(show));

        assertThat(showService.getShowById(1L)).isSameAs(show);
    }

    @Test
    void getShowByIdShouldReturnNullWhenMissing() {
        when(showRepository.findById(2L)).thenReturn(Optional.empty());

        assertThat(showService.getShowById(2L)).isNull();
    }

    @Test
    void saveShowShouldPersistValidShow() {
        Show show = new Show(tomorrowAt(18, 0), room(1L), play(1L, 120));
        when(showRepository.findAll()).thenReturn(List.of());
        when(showRepository.save(show)).thenReturn(show);

        assertThat(showService.saveShow(show)).isSameAs(show);
        verify(showRepository).save(show);
    }

    @Test
    void saveShowShouldAcceptBoundaryOpeningTime() {
        Show show = new Show(tomorrowAt(10, 0), room(1L), play(1L, 60));
        when(showRepository.findAll()).thenReturn(List.of());
        when(showRepository.save(any(Show.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(showService.saveShow(show)).isSameAs(show);
    }

    @Test
    void saveShowShouldAcceptBoundaryClosingTime() {
        Show show = new Show(tomorrowAt(21, 0), room(1L), play(1L, 60));
        when(showRepository.findAll()).thenReturn(List.of());
        when(showRepository.save(any(Show.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(showService.saveShow(show)).isSameAs(show);
    }

    @Test
    void saveShowShouldRejectMissingPlay() {
        Show show = new Show(tomorrowAt(18, 0), room(1L), null);

        assertThatThrownBy(() -> showService.saveShow(show))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A színdarab, a terem és a kezdési időpont megadása kötelező!");
    }

    @Test
    void saveShowShouldRejectMissingRoom() {
        Show show = new Show(tomorrowAt(18, 0), null, play(1L, 120));

        assertThatThrownBy(() -> showService.saveShow(show))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A színdarab, a terem és a kezdési időpont megadása kötelező!");
    }

    @Test
    void saveShowShouldRejectMissingStartTime() {
        Show show = new Show(null, room(1L), play(1L, 120));

        assertThatThrownBy(() -> showService.saveShow(show))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A színdarab, a terem és a kezdési időpont megadása kötelező!");
    }

    @Test
    void saveShowShouldRejectPastStartTime() {
        Show show = new Show(LocalDateTime.now().minusHours(1), room(1L), play(1L, 120));

        assertThatThrownBy(() -> showService.saveShow(show))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Múltbeli időpontra nem lehet előadást rögzíteni!");
    }

    @Test
    void saveShowShouldRejectStartBeforeOpeningTime() {
        Show show = new Show(tomorrowAt(9, 59), room(1L), play(1L, 60));

        assertThatThrownBy(() -> showService.saveShow(show))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Az előadásnak 10:00 és 22:00 közé kell esnie!");
    }

    @Test
    void saveShowShouldRejectEndAfterClosingTime() {
        Show show = new Show(tomorrowAt(21, 0), room(1L), play(1L, 120));

        assertThatThrownBy(() -> showService.saveShow(show))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Az előadásnak 10:00 és 22:00 közé kell esnie!");
    }

    @Test
    void saveShowShouldRejectShowCrossingMidnight() {
        Show show = new Show(tomorrowAt(22, 0), room(1L), play(1L, 120));

        assertThatThrownBy(() -> showService.saveShow(show))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Az előadásnak 10:00 és 22:00 közé kell esnie!");
    }

    @Test
    void saveShowShouldRejectOverlappingShowInSameRoom() {
        Show existing = new Show(tomorrowAt(18, 0), room(1L), play(1L, 120));
        existing.setId(10L);
        Show newShow = new Show(tomorrowAt(19, 0), room(1L), play(2L, 60));
        when(showRepository.findAll()).thenReturn(List.of(existing));

        assertThatThrownBy(() -> showService.saveShow(newShow))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Ütközés! Ebben a teremben már fut előadás ekkor: Darab 1");
    }

    @Test
    void saveShowShouldAllowOverlapInDifferentRoom() {
        Show existing = new Show(tomorrowAt(18, 0), room(1L), play(1L, 120));
        existing.setId(10L);
        Show newShow = new Show(tomorrowAt(19, 0), room(2L), play(2L, 60));
        when(showRepository.findAll()).thenReturn(List.of(existing));
        when(showRepository.save(any(Show.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(showService.saveShow(newShow)).isSameAs(newShow);
    }

    @Test
    void saveShowShouldAllowShowEndingBeforeExistingStarts() {
        Show existing = new Show(tomorrowAt(18, 0), room(1L), play(1L, 120));
        existing.setId(10L);
        Show newShow = new Show(tomorrowAt(16, 0), room(1L), play(2L, 60));
        when(showRepository.findAll()).thenReturn(List.of(existing));
        when(showRepository.save(any(Show.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(showService.saveShow(newShow)).isSameAs(newShow);
    }

    @Test
    void saveShowShouldAllowShowStartingAfterExistingEnds() {
        Show existing = new Show(tomorrowAt(18, 0), room(1L), play(1L, 120));
        existing.setId(10L);
        Show newShow = new Show(tomorrowAt(20, 30), room(1L), play(2L, 60));
        when(showRepository.findAll()).thenReturn(List.of(existing));
        when(showRepository.save(any(Show.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(showService.saveShow(newShow)).isSameAs(newShow);
    }

    @Test
    void saveShowShouldSkipExistingShowWithSameId() {
        Show existing = new Show(tomorrowAt(18, 0), room(1L), play(1L, 120));
        existing.setId(10L);
        Show newShow = new Show(tomorrowAt(19, 0), room(1L), play(2L, 60));
        newShow.setId(10L);
        when(showRepository.findAll()).thenReturn(List.of(existing));
        when(showRepository.save(any(Show.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(showService.saveShow(newShow)).isSameAs(newShow);
    }

    @Test
    void saveShowShouldCheckRoomWhenIdsAreNull() {
        Show existing = new Show(tomorrowAt(18, 0), room(1L), play(1L, 120));
        Show newShow = new Show(tomorrowAt(19, 0), room(2L), play(2L, 60));
        when(showRepository.findAll()).thenReturn(List.of(existing));
        when(showRepository.save(any(Show.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(showService.saveShow(newShow)).isSameAs(newShow);
    }

    @Test
    void saveShowShouldCheckDifferentExistingId() {
        Show existing = new Show(tomorrowAt(18, 0), room(2L), play(1L, 120));
        existing.setId(10L);
        Show newShow = new Show(tomorrowAt(19, 0), room(1L), play(2L, 60));
        newShow.setId(11L);
        when(showRepository.findAll()).thenReturn(List.of(existing));
        when(showRepository.save(any(Show.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(showService.saveShow(newShow)).isSameAs(newShow);
    }

    @Test
    void deleteShowShouldDelegateToRepository() {
        showService.deleteShow(8L);

        verify(showRepository).deleteById(8L);
    }
}
