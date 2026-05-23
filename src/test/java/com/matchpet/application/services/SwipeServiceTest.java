package com.matchpet.application.services;

import com.matchpet.application.ports.input.dto.SwipeCommand;
import com.matchpet.application.ports.input.dto.SwipeResult;
import com.matchpet.application.ports.output.PetPersistencePort;
import com.matchpet.application.ports.output.SwipePersistencePort;
import com.matchpet.application.ports.output.UserPersistencePort;
import com.matchpet.domain.exception.EntityNotFoundException;
import com.matchpet.domain.model.Pet;
import com.matchpet.domain.model.Shelter;
import com.matchpet.domain.model.SwipeAction;
import com.matchpet.domain.model.SwipeEvent;
import com.matchpet.domain.model.Trait;
import com.matchpet.domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SwipeServiceTest {

    private final UserPersistencePort userPersistencePort = mock(UserPersistencePort.class);
    private final PetPersistencePort petPersistencePort = mock(PetPersistencePort.class);
    private final SwipePersistencePort swipePersistencePort = mock(SwipePersistencePort.class);
    private final SwipeService service = new SwipeService(userPersistencePort, petPersistencePort, swipePersistencePort);

    @Test
    void shouldPersistLikeSwipe() {
        User user = new User("u1", "Martin", List.of(new Trait("t1", "Friendly")));
        Pet pet = new Pet("p1", "Luna", new Shelter("s1", "Refugio Norte", "CABA"), List.of(new Trait("t2", "Calm")));

        when(userPersistencePort.findById("u1")).thenReturn(Optional.of(user));
        when(petPersistencePort.findById("p1")).thenReturn(Optional.of(pet));
        when(swipePersistencePort.save(any(SwipeEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SwipeResult result = service.execute(new SwipeCommand("u1", "p1", SwipeAction.LIKE));

        assertEquals("u1", result.userId());
        assertEquals("p1", result.petId());
        assertEquals(SwipeAction.LIKE, result.action());
    }

    @Test
    void shouldPersistDislikeSwipe() {
        User user = new User("u1", "Martin", List.of(new Trait("t1", "Friendly")));
        Pet pet = new Pet("p1", "Luna", new Shelter("s1", "Refugio Norte", "CABA"), List.of(new Trait("t2", "Calm")));

        when(userPersistencePort.findById("u1")).thenReturn(Optional.of(user));
        when(petPersistencePort.findById("p1")).thenReturn(Optional.of(pet));
        when(swipePersistencePort.save(any(SwipeEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SwipeResult result = service.execute(new SwipeCommand("u1", "p1", SwipeAction.DISLIKE));

        assertEquals(SwipeAction.DISLIKE, result.action());
    }

    @Test
    void shouldFailWhenUserDoesNotExist() {
        when(userPersistencePort.findById("u-404")).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.execute(new SwipeCommand("u-404", "p1", SwipeAction.LIKE)));

        assertEquals("User not found: u-404", ex.getMessage());
    }

    @Test
    void shouldRejectLikeSwipeWhenBehaviorIsImpulsive() {
        User user = new User("u1", "Martin", List.of());
        Pet pet = new Pet("p1", "Luna", new Shelter("s1", "Refugio Norte", "CABA"), List.of());

        when(userPersistencePort.findById("u1")).thenReturn(Optional.of(user));
        when(petPersistencePort.findById("p1")).thenReturn(Optional.of(pet));
        when(swipePersistencePort.countLikesSince(eq("u1"), any())).thenReturn(10L);

        assertThrows(com.matchpet.domain.exception.ImpulsiveBehaviorException.class,
                () -> service.execute(new SwipeCommand("u1", "p1", SwipeAction.LIKE)));
    }

    @Test
    void shouldAllowDislikeSwipeEvenIfLikesAreImpulsive() {
        User user = new User("u1", "Martin", List.of());
        Pet pet = new Pet("p1", "Luna", new Shelter("s1", "Refugio Norte", "CABA"), List.of());

        when(userPersistencePort.findById("u1")).thenReturn(Optional.of(user));
        when(petPersistencePort.findById("p1")).thenReturn(Optional.of(pet));
        when(swipePersistencePort.countLikesSince(eq("u1"), any())).thenReturn(10L);
        when(swipePersistencePort.save(any(SwipeEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SwipeResult result = service.execute(new SwipeCommand("u1", "p1", SwipeAction.DISLIKE));

        assertEquals(SwipeAction.DISLIKE, result.action());
    }
}
