package com.example.szinhaz.service;

import com.example.szinhaz.model.Play;
import com.example.szinhaz.repository.PlayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayServiceTest {

    private PlayRepository playRepository;
    private PlayService playService;

    @BeforeEach
    void setUp() {
        playRepository = Mockito.mock(PlayRepository.class);
        playService = new PlayService(playRepository);
    }

    private Play validPlay() {
        return new Play("Hamlet", "William Shakespeare", 160, 2024, "Leiras", "kep.png");
    }

    @Test
    void getAllPlaysShouldReturnRepositoryResult() {
        List<Play> plays = List.of(validPlay());
        when(playRepository.findAll()).thenReturn(plays);

        assertThat(playService.getAllPlays()).isSameAs(plays);
    }

    @Test
    void getPlayByIdShouldReturnPlayWhenPresent() {
        Play play = validPlay();
        when(playRepository.findById(1L)).thenReturn(Optional.of(play));

        assertThat(playService.getPlayById(1L)).isSameAs(play);
    }

    @Test
    void getPlayByIdShouldReturnNullWhenMissing() {
        when(playRepository.findById(2L)).thenReturn(Optional.empty());

        assertThat(playService.getPlayById(2L)).isNull();
    }

    @Test
    void savePlayShouldPersistValidPlay() {
        Play play = validPlay();
        when(playRepository.save(play)).thenReturn(play);

        assertThat(playService.savePlay(play)).isSameAs(play);
        verify(playRepository).save(play);
    }

    @Test
    void savePlayShouldAcceptBoundaryDuration() {
        Play shortest = validPlay();
        shortest.setDurationMinutes(15);
        Play longest = validPlay();
        longest.setDurationMinutes(360);
        when(playRepository.save(any(Play.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(playService.savePlay(shortest).getDurationMinutes()).isEqualTo(15);
        assertThat(playService.savePlay(longest).getDurationMinutes()).isEqualTo(360);
    }

    @Test
    void savePlayShouldRejectNullTitle() {
        Play play = validPlay();
        play.setTitle(null);

        assertThatThrownBy(() -> playService.savePlay(play))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A színdarab címe nem lehet üres!");
    }

    @Test
    void savePlayShouldRejectBlankTitle() {
        Play play = validPlay();
        play.setTitle("  ");

        assertThatThrownBy(() -> playService.savePlay(play))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A színdarab címe nem lehet üres!");
    }

    @Test
    void savePlayShouldRejectNullDirector() {
        Play play = validPlay();
        play.setDirector(null);

        assertThatThrownBy(() -> playService.savePlay(play))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A rendező neve nem lehet üres!");
    }

    @Test
    void savePlayShouldRejectBlankDirector() {
        Play play = validPlay();
        play.setDirector("   ");

        assertThatThrownBy(() -> playService.savePlay(play))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A rendező neve nem lehet üres!");
    }

    @Test
    void savePlayShouldRejectNullDuration() {
        Play play = validPlay();
        play.setDurationMinutes(null);

        assertThatThrownBy(() -> playService.savePlay(play))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A színdarab hossza kötelező, és 15 és 360 perc között lehet!");
    }

    @Test
    void savePlayShouldRejectTooShortDuration() {
        Play play = validPlay();
        play.setDurationMinutes(14);

        assertThatThrownBy(() -> playService.savePlay(play))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A színdarab hossza kötelező, és 15 és 360 perc között lehet!");
    }

    @Test
    void savePlayShouldRejectTooLongDuration() {
        Play play = validPlay();
        play.setDurationMinutes(361);

        assertThatThrownBy(() -> playService.savePlay(play))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A színdarab hossza kötelező, és 15 és 360 perc között lehet!");
    }

    @Test
    void savePlayShouldRejectTooEarlyReleaseYear() {
        Play play = validPlay();
        play.setReleaseYear(1799);

        assertThatThrownBy(() -> playService.savePlay(play))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Érvénytelen bemutató év!");
    }

    @Test
    void savePlayShouldRejectTooLateReleaseYear() {
        Play play = validPlay();
        play.setReleaseYear(LocalDate.now().getYear() + 6);

        assertThatThrownBy(() -> playService.savePlay(play))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Érvénytelen bemutató év!");
    }

    @Test
    void savePlayShouldAcceptBoundaryReleaseYears() {
        Play earliest = validPlay();
        earliest.setReleaseYear(1800);
        Play latest = validPlay();
        latest.setReleaseYear(LocalDate.now().getYear() + 5);
        when(playRepository.save(any(Play.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(playService.savePlay(earliest).getReleaseYear()).isEqualTo(1800);
        assertThat(playService.savePlay(latest).getReleaseYear()).isEqualTo(LocalDate.now().getYear() + 5);
    }

    @Test
    void deletePlayShouldDelegateToRepository() {
        playService.deletePlay(4L);

        verify(playRepository).deleteById(4L);
    }

    @Test
    void savePlayShouldRejectTooLongTitle() {
        Play play = validPlay();
        play.setTitle("T".repeat(256));

        assertThatThrownBy(() -> playService.savePlay(play))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A színdarab címe legfeljebb 255 karakter lehet!");
    }

    @Test
    void savePlayShouldRejectTooLongDirector() {
        Play play = validPlay();
        play.setDirector("D".repeat(256));

        assertThatThrownBy(() -> playService.savePlay(play))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A rendező neve legfeljebb 255 karakter lehet!");
    }

    @Test
    void savePlayShouldRejectNullReleaseYear() {
        Play play = validPlay();
        play.setReleaseYear(null);

        assertThatThrownBy(() -> playService.savePlay(play))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Érvénytelen bemutató év!");
    }
}
