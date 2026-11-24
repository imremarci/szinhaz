package com.example.szinhaz.config;

import com.example.szinhaz.model.Actor;
import com.example.szinhaz.model.Play;
import com.example.szinhaz.model.Room;
import com.example.szinhaz.model.Show;
import com.example.szinhaz.model.User;
import com.example.szinhaz.repository.ActorRepository;
import com.example.szinhaz.repository.PlayRepository;
import com.example.szinhaz.repository.RoomRepository;
import com.example.szinhaz.repository.ShowRepository;
import com.example.szinhaz.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatabaseSeederTest {

    private PlayRepository playRepository;
    private ActorRepository actorRepository;
    private RoomRepository roomRepository;
    private ShowRepository showRepository;
    private UserRepository userRepository;
    private DatabaseSeeder seeder;

    @BeforeEach
    void setUp() {
        playRepository = org.mockito.Mockito.mock(PlayRepository.class);
        actorRepository = org.mockito.Mockito.mock(ActorRepository.class);
        roomRepository = org.mockito.Mockito.mock(RoomRepository.class);
        showRepository = org.mockito.Mockito.mock(ShowRepository.class);
        userRepository = org.mockito.Mockito.mock(UserRepository.class);
        seeder = new DatabaseSeeder(playRepository, actorRepository, roomRepository, showRepository, userRepository);
    }

    @Test
    void runShouldSeedEverythingWhenDatabaseIsEmpty() {
        when(playRepository.count()).thenReturn(0L);
        when(userRepository.findByName("admin")).thenReturn(Optional.empty());

        seeder.run();

        verify(roomRepository, times(3)).save(any(Room.class));
        verify(playRepository, times(2)).save(any(Play.class));
        verify(actorRepository, times(2)).save(any(Actor.class));
        verify(showRepository, times(3)).save(any(Show.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void runShouldCreateAdminWithExpectedCredentials() {
        when(playRepository.count()).thenReturn(0L);
        when(userRepository.findByName("admin")).thenReturn(Optional.empty());

        seeder.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User admin = captor.getValue();
        assertThat(admin.getName()).isEqualTo("admin");
        assertThat(admin.getPassword()).isEqualTo("admin123");
        assertThat(admin.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void runShouldSeedRoomsWithExpectedLayout() {
        when(playRepository.count()).thenReturn(0L);
        when(userRepository.findByName("admin")).thenReturn(Optional.empty());

        seeder.run();

        ArgumentCaptor<Room> captor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository, times(3)).save(captor.capture());

        List<Room> rooms = captor.getAllValues();
        assertThat(rooms)
                .extracting(Room::getName)
                .containsExactly(
                        "Zsiráf Fórum, Kamaraterem",
                        "Zsiráf Fórum, Stúdióterem",
                        "Zsiráf Teátrum, Nagyterem"
                );
        assertThat(rooms)
                .extracting(Room::getNumberOfRows, Room::getNumberOfSeats)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(8, 10),
                        org.assertj.core.groups.Tuple.tuple(7, 8),
                        org.assertj.core.groups.Tuple.tuple(10, 15)
                );
    }

    @Test
    void runShouldSeedPlaysAndActors() {
        when(playRepository.count()).thenReturn(0L);
        when(userRepository.findByName("admin")).thenReturn(Optional.empty());

        seeder.run();

        ArgumentCaptor<Play> playCaptor = ArgumentCaptor.forClass(Play.class);
        verify(playRepository, times(2)).save(playCaptor.capture());
        assertThat(playCaptor.getAllValues())
                .extracting(Play::getTitle)
                .containsExactly("Hamlet", "A dzsungel könyve");

        ArgumentCaptor<Actor> actorCaptor = ArgumentCaptor.forClass(Actor.class);
        verify(actorRepository, times(2)).save(actorCaptor.capture());
        assertThat(actorCaptor.getAllValues())
                .extracting(Actor::getName)
                .containsExactly("Nagy Ervin", "Ónodi Eszter");
    }

    @Test
    void runShouldNotCreateAdminWhenItAlreadyExists() {
        when(playRepository.count()).thenReturn(0L);
        when(userRepository.findByName("admin")).thenReturn(Optional.of(new User()));

        seeder.run();

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void runShouldDoNothingWhenDatabaseIsNotEmpty() {
        when(playRepository.count()).thenReturn(5L);

        seeder.run();

        verify(roomRepository, never()).save(any(Room.class));
        verify(playRepository, never()).save(any(Play.class));
        verify(actorRepository, never()).save(any(Actor.class));
        verify(showRepository, never()).save(any(Show.class));
        verify(userRepository, never()).findByName(any());
        verify(userRepository, never()).save(any(User.class));
    }
}
