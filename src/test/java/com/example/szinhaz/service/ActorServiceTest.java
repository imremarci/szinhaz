package com.example.szinhaz.service;

import com.example.szinhaz.model.Actor;
import com.example.szinhaz.repository.ActorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActorServiceTest {

    @Mock
    private ActorRepository actorRepository;

    @InjectMocks
    private ActorService actorService;

    private Actor validActor;

    @BeforeEach
    void setUp() {
        validActor = new Actor();
        validActor.setName("Mucsi Zoltán");
        validActor.setRating(4.8);
    }

    @Test
    void getAllActors_ReturnsList() {
        when(actorRepository.findAll()).thenReturn(List.of(validActor));

        List<Actor> result = actorService.getAllActors();

        assertEquals(1, result.size());
        verify(actorRepository).findAll();
    }

    @Test
    void getActorById_Found_ReturnsActor() {
        when(actorRepository.findById(1L)).thenReturn(Optional.of(validActor));

        Actor result = actorService.getActorById(1L);

        assertNotNull(result);
        assertEquals("Mucsi Zoltán", result.getName());
    }

    @Test
    void getActorById_NotFound_ReturnsNull() {
        when(actorRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(actorService.getActorById(99L));
    }

    @Test
    void saveActor_ValidData_SavesAndReturnsActor() {
        when(actorRepository.save(any(Actor.class))).thenAnswer(i -> i.getArgument(0));

        Actor saved = actorService.saveActor(validActor);

        assertNotNull(saved);
        assertEquals("Mucsi Zoltán", saved.getName());
        verify(actorRepository, times(1)).save(validActor);
    }

    @Test
    void saveActor_EmptyOrNullName_ThrowsException() {
        validActor.setName(null);
        assertThrows(IllegalArgumentException.class, () -> actorService.saveActor(validActor));

        validActor.setName("  ");
        assertThrows(IllegalArgumentException.class, () -> actorService.saveActor(validActor));
        verify(actorRepository, never()).save(any());
    }

    @Test
    void saveActor_InvalidRating_ThrowsException() {
        validActor.setRating(0.5);
        assertThrows(IllegalArgumentException.class, () -> actorService.saveActor(validActor));

        validActor.setRating(5.5);
        assertThrows(IllegalArgumentException.class, () -> actorService.saveActor(validActor));
        verify(actorRepository, never()).save(any());
    }

    @Test
    void deleteActor_CallsRepositoryDelete() {
        actorService.deleteActor(1L);

        verify(actorRepository, times(1)).deleteById(1L);
    }
}