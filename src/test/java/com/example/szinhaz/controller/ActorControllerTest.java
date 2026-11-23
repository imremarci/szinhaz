package com.example.szinhaz.controller;

import com.example.szinhaz.model.Actor;
import com.example.szinhaz.model.User;
import com.example.szinhaz.service.ActorService;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActorControllerTest {

    @Mock
    private ActorService actorService;

    @InjectMocks
    private ActorController actorController;

    private Model model;
    private RedirectAttributes redirectAttributes;
    private MockHttpSession session;
    private User regularUser;
    private User adminUser;
    private Actor actorA;
    private Actor actorB;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
        redirectAttributes = new RedirectAttributesModelMap();
        session = new MockHttpSession();

        regularUser = new User();
        regularUser.setRole("USER");

        adminUser = new User();
        adminUser.setRole("ADMIN");

        actorA = new Actor();
        actorA.setName("Ábel Anita");

        actorB = new Actor();
        actorB.setName("Zoltán");
    }

    @Test
    void listActorsUser_SortsHungarianAlphabetically() {
        when(actorService.getAllActors()).thenReturn(List.of(actorB, actorA));

        String view = actorController.listActorsUser(model);

        assertEquals("szineszek_user", view);
        @SuppressWarnings("unchecked")
        List<Actor> actors = (List<Actor>) model.getAttribute("actors");
        assertNotNull(actors);
        assertEquals("Ábel Anita", actors.get(0).getName());
    }

    @Test
    void listActorsAdmin_AccessControlAndSuccess() {
        assertEquals("redirect:/bejelentkezes", actorController.listActorsAdmin(session, model));

        session.setAttribute("loggedInUser", regularUser);
        assertEquals("redirect:/fooldal", actorController.listActorsAdmin(session, model));

        session.setAttribute("loggedInUser", adminUser);
        when(actorService.getAllActors()).thenReturn(List.of(actorB, actorA));

        String view = actorController.listActorsAdmin(session, model);
        assertEquals("szineszek_admin", view);
        assertNotNull(model.getAttribute("actors"));
        assertNotNull(model.getAttribute("newActor"));
    }

    @Test
    void saveActor_AccessControlSuccessAndError() {
        assertEquals("redirect:/fooldal", actorController.saveActor(actorA, session, redirectAttributes));

        session.setAttribute("loggedInUser", regularUser);
        assertEquals("redirect:/fooldal", actorController.saveActor(actorA, session, redirectAttributes));

        session.setAttribute("loggedInUser", adminUser);
        assertEquals("redirect:/admin/szineszek", actorController.saveActor(actorA, session, redirectAttributes));
        assertNotNull(redirectAttributes.getFlashAttributes().get("success"));
        verify(actorService).saveActor(actorA);

        doThrow(new IllegalArgumentException("Hibás név")).when(actorService).saveActor(any(Actor.class));
        assertEquals("redirect:/admin/szineszek", actorController.saveActor(actorA, session, redirectAttributes));
        assertEquals("Hibás név", redirectAttributes.getFlashAttributes().get("error"));
    }
}