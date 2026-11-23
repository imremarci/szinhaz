package com.example.szinhaz.controller;

import com.example.szinhaz.model.Actor;
import com.example.szinhaz.model.Play;
import com.example.szinhaz.model.Show;
import com.example.szinhaz.model.User;
import com.example.szinhaz.repository.ActorRepository;
import com.example.szinhaz.repository.PlayRepository;
import com.example.szinhaz.repository.ShowRepository;
import com.example.szinhaz.repository.TicketRepository;
import com.example.szinhaz.service.PlayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayControllerTest {

    @Mock private PlayService playService;
    @Mock private ShowRepository showRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private PlayRepository playRepository;
    @Mock private ActorRepository actorRepository;

    @InjectMocks
    private PlayController playController;

    private Model model;
    private RedirectAttributes redirectAttributes;
    private MockHttpSession session;
    private User regularUser;
    private User adminUser;
    private Play play;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
        redirectAttributes = new RedirectAttributesModelMap();
        session = new MockHttpSession();

        regularUser = new User();
        regularUser.setRole("USER");

        adminUser = new User();
        adminUser.setRole("ADMIN");

        play = new Play();
        play.setId(1L);
        play.setTitle("Hamlet");
    }

    @Test
    void listPlaysUser_ReturnsViewWithPlays() {
        when(playService.getAllPlays()).thenReturn(List.of(play));

        assertEquals("szindarabok_user", playController.listPlaysUser(model));
        assertNotNull(model.getAttribute("plays"));
    }

    @Test
    void viewPlayDetails_ReturnsViewWithPlay() {
        when(playService.getPlayById(1L)).thenReturn(play);

        assertEquals("szindarab", playController.viewPlayDetails(1L, model));
        assertEquals(play, model.getAttribute("play"));
    }

    @Test
    void listPlaysAdmin_AccessControlAndSuccess() {
        assertEquals("redirect:/bejelentkezes", playController.listPlaysAdmin(session, model));

        session.setAttribute("loggedInUser", regularUser);
        assertEquals("redirect:/fooldal", playController.listPlaysAdmin(session, model));

        session.setAttribute("loggedInUser", adminUser);
        when(playService.getAllPlays()).thenReturn(List.of(play));
        when(actorRepository.findAll()).thenReturn(Collections.emptyList());

        assertEquals("szindarabok_admin", playController.listPlaysAdmin(session, model));
        assertNotNull(model.getAttribute("plays"));
        assertNotNull(model.getAttribute("newPlay"));
        assertNotNull(model.getAttribute("actors"));
    }

    @Test
    void savePlay_AccessControlActorsSuccessAndError() {
        assertEquals("redirect:/fooldal", playController.savePlay(play, null, session, redirectAttributes));

        session.setAttribute("loggedInUser", adminUser);
        Actor actor = new Actor();
        when(actorRepository.findAllById(List.of(1L))).thenReturn(List.of(actor));

        assertEquals("redirect:/admin/darabok", playController.savePlay(play, List.of(1L), session, redirectAttributes));
        assertNotNull(redirectAttributes.getFlashAttributes().get("success"));
        verify(playService).savePlay(play);

        doThrow(new IllegalArgumentException("Hibás cím")).when(playService).savePlay(any(Play.class));
        assertEquals("redirect:/admin/darabok", playController.savePlay(play, Collections.emptyList(), session, redirectAttributes));
        assertEquals("Hibás cím", redirectAttributes.getFlashAttributes().get("error"));
    }

    @Test
    void deletePlay_AccessControlAndCascadeDelete() {
        assertEquals("redirect:/fooldal", playController.deletePlay(1L, session));

        session.setAttribute("loggedInUser", adminUser);

        Show relatedShow = new Show();
        relatedShow.setId(10L);
        relatedShow.setPlay(play);

        Play otherPlay = new Play();
        otherPlay.setId(2L);
        Show unrelatedShow = new Show();
        unrelatedShow.setId(11L);
        unrelatedShow.setPlay(otherPlay);

        when(showRepository.findAll()).thenReturn(List.of(relatedShow, unrelatedShow));
        when(ticketRepository.findByShowId(10L)).thenReturn(Collections.emptyList());

        assertEquals("redirect:/admin/darabok", playController.deletePlay(1L, session));
        verify(ticketRepository).deleteAll(any());
        verify(showRepository).deleteAll(List.of(relatedShow));
        verify(playRepository).deleteById(1L);
    }
}