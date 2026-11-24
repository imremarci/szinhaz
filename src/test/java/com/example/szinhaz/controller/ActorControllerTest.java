package com.example.szinhaz.controller;

import com.example.szinhaz.model.Actor;
import com.example.szinhaz.model.User;
import com.example.szinhaz.service.ActorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActorControllerTest {

    private ActorService actorService;
    private ActorController actorController;
    private Model model;
    private MockHttpSession session;
    private RedirectAttributesModelMap redirectAttributes;

    @BeforeEach
    void setUp() {
        actorService = Mockito.mock(ActorService.class);
        actorController = new ActorController(actorService);
        model = new ExtendedModelMap();
        session = new MockHttpSession();
        redirectAttributes = new RedirectAttributesModelMap();
    }

    private User loggedInUser(String role) {
        User user = new User("teszt", "jelszo", "Teszt Elek", "teszt@example.com");
        user.setRole(role);
        return user;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> flashAttributes() {
        return (Map<String, Object>) redirectAttributes.getFlashAttributes();
    }

    @Test
    void listActorsUserShouldExposeActorsSortedByName() {
        Actor bela = new Actor("Béla", 4, "bela.png");
        Actor anna = new Actor("Anna", 5, "anna.png");
        when(actorService.getAllActors()).thenReturn(List.of(bela, anna));

        String view = actorController.listActorsUser(model);

        assertThat(view).isEqualTo("szineszek_user");
        assertThat((List<Actor>) model.getAttribute("actors"))
                .extracting(Actor::getName)
                .containsExactly("Anna", "Béla");
    }

    @Test
    void listActorsAdminShouldRedirectToLoginWithoutUser() {
        assertThat(actorController.listActorsAdmin(session, model)).isEqualTo("redirect:/bejelentkezes");
    }

    @Test
    void listActorsAdminShouldRedirectNonAdminUser() {
        session.setAttribute("loggedInUser", loggedInUser("USER"));

        assertThat(actorController.listActorsAdmin(session, model)).isEqualTo("redirect:/fooldal");
    }

    @Test
    void listActorsAdminShouldExposeActorsAndEmptyFormForAdmin() {
        session.setAttribute("loggedInUser", loggedInUser("ADMIN"));
        when(actorService.getAllActors()).thenReturn(List.of(new Actor("Anna", 5, "anna.png")));

        String view = actorController.listActorsAdmin(session, model);

        assertThat(view).isEqualTo("szineszek_admin");
        assertThat((List<Actor>) model.getAttribute("actors")).hasSize(1);
        assertThat(model.getAttribute("newActor")).isInstanceOf(Actor.class);
    }

    @Test
    void saveActorShouldRedirectWithoutUser() {
        Actor actor = new Actor("Anna", 5, "anna.png");

        assertThat(actorController.saveActor(actor, session, redirectAttributes))
                .isEqualTo("redirect:/fooldal");
        verify(actorService, never()).saveActor(any());
    }

    @Test
    void saveActorShouldRedirectNonAdminUser() {
        session.setAttribute("loggedInUser", loggedInUser("USER"));
        Actor actor = new Actor("Anna", 5, "anna.png");

        assertThat(actorController.saveActor(actor, session, redirectAttributes))
                .isEqualTo("redirect:/fooldal");
        verify(actorService, never()).saveActor(any());
    }

    @Test
    void saveActorShouldSaveAndReportSuccessForAdmin() {
        session.setAttribute("loggedInUser", loggedInUser("ADMIN"));
        Actor actor = new Actor("Anna", 5, "anna.png");
        when(actorService.saveActor(actor)).thenReturn(actor);

        String view = actorController.saveActor(actor, session, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/szineszek");
        assertThat(flashAttributes())
                .containsEntry("success", "Színész sikeresen mentve!");
        verify(actorService).saveActor(actor);
    }

    @Test
    void saveActorShouldReportValidationErrorForAdmin() {
        session.setAttribute("loggedInUser", loggedInUser("ADMIN"));
        Actor actor = new Actor("", 5, "anna.png");
        when(actorService.saveActor(actor)).thenThrow(new IllegalArgumentException("Hibás színész"));

        String view = actorController.saveActor(actor, session, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/szineszek");
        assertThat(flashAttributes()).containsEntry("error", "Hibás színész");
    }
}
