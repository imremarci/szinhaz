package com.example.szinhaz.controller;

import com.example.szinhaz.model.Actor;
import com.example.szinhaz.model.Play;
import com.example.szinhaz.model.Room;
import com.example.szinhaz.model.Show;
import com.example.szinhaz.model.User;
import com.example.szinhaz.repository.ActorRepository;
import com.example.szinhaz.repository.PlayRepository;
import com.example.szinhaz.repository.ShowRepository;
import com.example.szinhaz.repository.TicketRepository;
import com.example.szinhaz.service.PlayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlayControllerTest {

    private PlayService playService;
    private ShowRepository showRepository;
    private TicketRepository ticketRepository;
    private PlayRepository playRepository;
    private ActorRepository actorRepository;
    private PlayController playController;
    private Model model;
    private MockHttpSession session;
    private RedirectAttributesModelMap redirectAttributes;

    @BeforeEach
    void setUp() {
        playService = Mockito.mock(PlayService.class);
        showRepository = Mockito.mock(ShowRepository.class);
        ticketRepository = Mockito.mock(TicketRepository.class);
        playRepository = Mockito.mock(PlayRepository.class);
        actorRepository = Mockito.mock(ActorRepository.class);
        playController = new PlayController(playService, showRepository, ticketRepository, playRepository, actorRepository);
        model = new ExtendedModelMap();
        session = new MockHttpSession();
        redirectAttributes = new RedirectAttributesModelMap();
    }

    private User loggedInUser(String role) {
        User user = new User("teszt", "jelszo", "Teszt Elek", "teszt@example.com");
        user.setRole(role);
        return user;
    }

    private Play play(Long id, String title) {
        Play play = new Play(title, "Rendezo", 120, 2024, "Leiras", "kep.png");
        play.setId(id);
        return play;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> flashAttributes() {
        return (Map<String, Object>) redirectAttributes.getFlashAttributes();
    }

    @Test
    void listPlaysUserShouldExposePlays() {
        List<Play> plays = List.of(play(1L, "Hamlet"));
        when(playService.getAllPlays()).thenReturn(plays);

        String view = playController.listPlaysUser(model);

        assertThat(view).isEqualTo("szindarabok_user");
        assertThat(model.getAttribute("plays")).isSameAs(plays);
    }

    @Test
    void viewPlayDetailsShouldExposeSelectedPlay() {
        Play play = play(1L, "Hamlet");
        when(playService.getPlayById(1L)).thenReturn(play);

        String view = playController.viewPlayDetails(1L, model);

        assertThat(view).isEqualTo("szindarab");
        assertThat(model.getAttribute("play")).isSameAs(play);
    }

    @Test
    void viewPlayDetailsShouldReturnNotFoundWhenPlayIsMissing() {
        when(playService.getPlayById(99L)).thenReturn(null);

        assertThatThrownBy(() -> playController.viewPlayDetails(99L, model))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("A színdarab nem található.");
    }

    @Test
    void listPlaysAdminShouldRedirectToLoginWithoutUser() {
        assertThat(playController.listPlaysAdmin(session, model)).isEqualTo("redirect:/bejelentkezes");
    }

    @Test
    void listPlaysAdminShouldRedirectNonAdminUser() {
        session.setAttribute("loggedInUser", loggedInUser("USER"));

        assertThat(playController.listPlaysAdmin(session, model)).isEqualTo("redirect:/fooldal");
    }

    @Test
    void listPlaysAdminShouldExposePlaysFormAndActorsForAdmin() {
        session.setAttribute("loggedInUser", loggedInUser("ADMIN"));
        List<Play> plays = List.of(play(1L, "Hamlet"));
        List<Actor> actors = List.of(new Actor("Anna", 5, "anna.png"));
        when(playService.getAllPlays()).thenReturn(plays);
        when(actorRepository.findAll()).thenReturn(actors);

        String view = playController.listPlaysAdmin(session, model);

        assertThat(view).isEqualTo("szindarabok_admin");
        assertThat(model.getAttribute("plays")).isSameAs(plays);
        assertThat(model.getAttribute("newPlay")).isInstanceOf(Play.class);
        assertThat(model.getAttribute("actors")).isSameAs(actors);
    }

    @Test
    void savePlayShouldRedirectWithoutUser() {
        Play play = play(null, "Hamlet");

        assertThat(playController.savePlay(play, null, session, redirectAttributes))
                .isEqualTo("redirect:/fooldal");
        verify(playService, never()).savePlay(any());
    }

    @Test
    void savePlayShouldRedirectNonAdminUser() {
        session.setAttribute("loggedInUser", loggedInUser("USER"));
        Play play = play(null, "Hamlet");

        assertThat(playController.savePlay(play, null, session, redirectAttributes))
                .isEqualTo("redirect:/fooldal");
        verify(playService, never()).savePlay(any());
    }

    @Test
    void savePlayShouldSaveWithoutActorsWhenActorIdsAreNull() {
        session.setAttribute("loggedInUser", loggedInUser("ADMIN"));
        Play play = play(null, "Hamlet");
        when(playService.savePlay(play)).thenReturn(play);

        String view = playController.savePlay(play, null, session, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/darabok");
        assertThat(play.getActors()).isEmpty();
        assertThat(flashAttributes())
                .containsEntry("success", "Színdarab sikeresen mentve!");
        verify(actorRepository, never()).findAllById(any());
    }

    @Test
    void savePlayShouldSaveWithoutActorsWhenActorIdsAreEmpty() {
        session.setAttribute("loggedInUser", loggedInUser("ADMIN"));
        Play play = play(null, "Hamlet");
        when(playService.savePlay(play)).thenReturn(play);

        playController.savePlay(play, List.of(), session, redirectAttributes);

        assertThat(play.getActors()).isEmpty();
        verify(actorRepository, never()).findAllById(any());
    }

    @Test
    void savePlayShouldAttachSelectedActors() {
        session.setAttribute("loggedInUser", loggedInUser("ADMIN"));
        Play play = play(null, "Hamlet");
        List<Long> actorIds = List.of(1L, 2L);
        List<Actor> actors = List.of(new Actor("Anna", 5, "anna.png"), new Actor("Béla", 4, "bela.png"));
        when(actorRepository.findAllById(actorIds)).thenReturn(actors);
        when(playService.savePlay(play)).thenReturn(play);

        playController.savePlay(play, actorIds, session, redirectAttributes);

        assertThat(play.getActors()).isSameAs(actors);
        verify(actorRepository).findAllById(actorIds);
    }

    @Test
    void savePlayShouldClearActorsWhenEditingWithoutActorIds() {
        session.setAttribute("loggedInUser", loggedInUser("ADMIN"));
        Play play = play(5L, "Hamlet");
        when(playService.savePlay(play)).thenReturn(play);

        playController.savePlay(play, null, session, redirectAttributes);

        assertThat(play.getActors()).isEmpty();
        verify(actorRepository, never()).findAllById(any());
    }

    @Test
    void savePlayShouldReplaceActorsWhenEditingWithActorIds() {
        session.setAttribute("loggedInUser", loggedInUser("ADMIN"));
        Play play = play(5L, "Hamlet");
        List<Long> actorIds = List.of(1L, 2L);
        List<Actor> actors = List.of(
                new Actor("Anna", 5, "anna.png"),
                new Actor("Béla", 4, "bela.png")
        );
        when(actorRepository.findAllById(actorIds)).thenReturn(actors);
        when(playService.savePlay(play)).thenReturn(play);

        playController.savePlay(play, actorIds, session, redirectAttributes);

        assertThat(play.getActors()).isSameAs(actors);
        verify(actorRepository).findAllById(actorIds);
    }

    @Test
    void savePlayShouldReportValidationError() {
        session.setAttribute("loggedInUser", loggedInUser("ADMIN"));
        Play play = play(null, "");
        when(playService.savePlay(play)).thenThrow(new IllegalArgumentException("Hibás darab"));

        String view = playController.savePlay(play, null, session, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/darabok");
        assertThat(flashAttributes()).containsEntry("error", "Hibás darab");
    }

    @Test
    void deletePlayShouldRedirectWithoutUser() {
        assertThat(playController.deletePlay(1L, session)).isEqualTo("redirect:/fooldal");
        verify(showRepository, never()).deleteAll(any());
    }

    @Test
    void deletePlayShouldRedirectNonAdminUser() {
        session.setAttribute("loggedInUser", loggedInUser("USER"));

        assertThat(playController.deletePlay(1L, session)).isEqualTo("redirect:/fooldal");
        verify(showRepository, never()).deleteAll(any());
    }

    @Test
    void deletePlayShouldRemoveRelatedShowsAndTickets() {
        session.setAttribute("loggedInUser", loggedInUser("ADMIN"));
        Play requestedPlay = play(2L, "Hamlet");
        Play otherPlay = play(3L, "Dzsungel");
        Show showWithoutPlay = new Show(LocalDateTime.now(), new Room("Terem", 5, 5), null);
        Show unrelatedShow = new Show(LocalDateTime.now(), new Room("Terem", 5, 5), otherPlay);
        Show relatedShow = new Show(LocalDateTime.now(), new Room("Terem", 5, 5), requestedPlay);
        relatedShow.setId(9L);
        List<Show> allShows = List.of(showWithoutPlay, unrelatedShow, relatedShow);
        List<com.example.szinhaz.model.Ticket> tickets = List.of(new com.example.szinhaz.model.Ticket());
        when(showRepository.findAll()).thenReturn(allShows);
        when(ticketRepository.findByShowId(9L)).thenReturn(tickets);

        String view = playController.deletePlay(2L, session);

        assertThat(view).isEqualTo("redirect:/admin/darabok");
        verify(ticketRepository).deleteAll(tickets);

        ArgumentCaptor<List<Show>> showsCaptor = ArgumentCaptor.forClass(List.class);
        verify(showRepository).deleteAll(showsCaptor.capture());
        assertThat(showsCaptor.getValue()).containsExactly(relatedShow);
        verify(playRepository).deleteById(2L);
    }

    @Test
    void deletePlayShouldHandlePlayWithoutRelatedShows() {
        session.setAttribute("loggedInUser", loggedInUser("ADMIN"));
        when(showRepository.findAll()).thenReturn(List.of());

        String view = playController.deletePlay(4L, session);

        assertThat(view).isEqualTo("redirect:/admin/darabok");
        verify(ticketRepository, never()).deleteAll(any());
        verify(showRepository).deleteAll(List.of());
        verify(playRepository).deleteById(4L);
    }
}
