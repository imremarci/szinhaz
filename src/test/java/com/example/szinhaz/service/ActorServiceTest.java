package com.example.szinhaz.service;

import com.example.szinhaz.model.Actor;
import com.example.szinhaz.repository.ActorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActorServiceTest {

    private ActorRepository actorRepository;
    private ActorService actorService;

    @BeforeEach
    void setUp() {
        actorRepository = Mockito.mock(ActorRepository.class);
        actorService = new ActorService(actorRepository);
    }

    @Test
    void getAllActorsShouldReturnRepositoryResult() {
        List<Actor> actors = List.of(new Actor("Nagy Ervin", 4, "kep.png"));
        when(actorRepository.findAll()).thenReturn(actors);

        assertThat(actorService.getAllActors()).isSameAs(actors);
    }

    @Test
    void getActorByIdShouldReturnActorWhenPresent() {
        Actor actor = new Actor("Nagy Ervin", 4, "kep.png");
        when(actorRepository.findById(1L)).thenReturn(Optional.of(actor));

        assertThat(actorService.getActorById(1L)).isSameAs(actor);
    }

    @Test
    void getActorByIdShouldReturnNullWhenMissing() {
        when(actorRepository.findById(2L)).thenReturn(Optional.empty());

        assertThat(actorService.getActorById(2L)).isNull();
    }

    @Test
    void saveActorShouldPersistValidActor() {
        Actor actor = new Actor("Nagy Ervin", 4, "kep.png");
        when(actorRepository.save(actor)).thenReturn(actor);

        assertThat(actorService.saveActor(actor)).isSameAs(actor);
        verify(actorRepository).save(actor);
    }

    @Test
    void saveActorShouldAcceptBoundaryRatings() {
        Actor lowest = new Actor("Also hatar", 1, "kep.png");
        Actor highest = new Actor("Felso hatar", 5, "kep.png");
        when(actorRepository.save(any(Actor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(actorService.saveActor(lowest).getRating()).isEqualTo(1.0);
        assertThat(actorService.saveActor(highest).getRating()).isEqualTo(5.0);
    }

    @Test
    void saveActorShouldRejectNullName() {
        Actor actor = new Actor(null, 4, "kep.png");

        assertThatThrownBy(() -> actorService.saveActor(actor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A színész neve nem lehet üres!");
    }

    @Test
    void saveActorShouldRejectBlankName() {
        Actor actor = new Actor("   ", 4, "kep.png");

        assertThatThrownBy(() -> actorService.saveActor(actor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A színész neve nem lehet üres!");
    }

    @Test
    void saveActorShouldRejectRatingBelowOne() {
        Actor actor = new Actor("Nagy Ervin", 4, "kep.png");
        actor.setRating(0.99);

        assertThatThrownBy(() -> actorService.saveActor(actor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Az értékelésnek 1.0 és 5.0 közé kell esnie!");
    }

    @Test
    void saveActorShouldRejectRatingAboveFive() {
        Actor actor = new Actor("Nagy Ervin", 4, "kep.png");
        actor.setRating(5.01);

        assertThatThrownBy(() -> actorService.saveActor(actor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Az értékelésnek 1.0 és 5.0 közé kell esnie!");
    }

    @Test
    void deleteActorShouldDelegateToRepository() {
        actorService.deleteActor(3L);

        verify(actorRepository).deleteById(3L);
    }

    @Test
    void saveActorShouldRejectTooLongName() {
        Actor actor = new Actor("N".repeat(256), 4, "kep.png");

        assertThatThrownBy(() -> actorService.saveActor(actor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A színész neve legfeljebb 255 karakter lehet!");
    }
}
