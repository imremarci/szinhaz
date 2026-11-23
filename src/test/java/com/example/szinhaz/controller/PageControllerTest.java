package com.example.szinhaz.controller;

import com.example.szinhaz.model.CartItem;
import com.example.szinhaz.model.Play;
import com.example.szinhaz.model.Room;
import com.example.szinhaz.model.Show;
import com.example.szinhaz.model.User;
import com.example.szinhaz.repository.PlayRepository;
import com.example.szinhaz.repository.RoomRepository;
import com.example.szinhaz.repository.ShowRepository;
import com.example.szinhaz.repository.TicketRepository;
import com.example.szinhaz.service.PlayService;
import com.example.szinhaz.service.RoomService;
import com.example.szinhaz.service.ShowService;
import com.example.szinhaz.service.TicketService;
import com.example.szinhaz.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PageControllerTest {

    @Mock private ShowService showService;
    @Mock private PlayService playService;
    @Mock private RoomService roomService;
    @Mock private TicketService ticketService;
    @Mock private UserService userService;
    @Mock private TicketRepository ticketRepository;
    @Mock private ShowRepository showRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private PlayRepository playRepository;

    @InjectMocks
    private PageController pageController;

    private Model model;
    private RedirectAttributes redirectAttributes;
    private MockHttpSession session;
    private User regularUser;
    private User adminUser;
    private Show show;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
        redirectAttributes = new RedirectAttributesModelMap();
        session = new MockHttpSession();

        regularUser = new User();
        regularUser.setId(1L);
        regularUser.setName("user1");
        regularUser.setRole("USER");

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setName("admin");
        adminUser.setRole("ADMIN");

        show = new Show();
        show.setId(10L);
        show.setStartTime(LocalDateTime.now());
    }

    @Test
    void initializeCart_ReturnsEmptyList() {
        List<CartItem> cart = pageController.initializeCart();
        assertNotNull(cart);
        assertTrue(cart.isEmpty());
    }

    @Test
    void home_PopulatesModelAndSortsRooms() {
        Room r1 = new Room("Kisterem", 4, 6);
        Room r2 = new Room("Nagyterem", 8, 12);
        Room r3 = new Room("B terem", 5, 5);

        when(playService.getAllPlays()).thenReturn(List.of(new Play()));
        when(roomService.getAllRooms()).thenReturn(new ArrayList<>(List.of(r1, r2, r3)));
        when(showService.getAllShows()).thenReturn(List.of(show));

        String view = pageController.home(model);

        assertEquals("fooldal", view);
        @SuppressWarnings("unchecked")
        List<Room> sortedRooms = (List<Room>) model.getAttribute("rooms");
        assertNotNull(sortedRooms);
        assertEquals("Nagyterem", sortedRooms.get(0).getName());
        assertNotNull(model.getAttribute("shows"));
    }

    @Test
    void cart_EmptyAndNonEmpty_CalculatesTotals() {
        List<CartItem> emptyCart = new ArrayList<>();
        assertEquals("kosar", pageController.cart(emptyCart, model));
        assertEquals(0, model.getAttribute("convenienceFee"));
        assertEquals(0, model.getAttribute("totalPrice"));

        List<CartItem> cart = List.of(new CartItem(show, "1. sor, 1. szék;", "1-1", 2, 9000));
        assertEquals("kosar", pageController.cart(cart, model));
        assertEquals(2, model.getAttribute("totalTickets"));
        assertEquals(390, model.getAttribute("convenienceFee"));
        assertEquals(9390, model.getAttribute("totalPrice"));
    }

    @Test
    void addToCart_ShowNotFound_RedirectsToShows() {
        when(showService.getShowById(99L)).thenReturn(null);

        String view = pageController.addToCart(99L, "1-1", new ArrayList<>(), redirectAttributes);

        assertEquals("redirect:/eloadasok", view);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
    }

    @Test
    void addToCart_EmptyOrInvalidSeats_RedirectsToShow() {
        when(showService.getShowById(10L)).thenReturn(show);

        assertEquals("redirect:/eloadas/10", pageController.addToCart(10L, "  ", new ArrayList<>(), redirectAttributes));
        assertEquals("redirect:/eloadas/10", pageController.addToCart(10L, "hibas", new ArrayList<>(), redirectAttributes));
        assertEquals("redirect:/eloadas/10", pageController.addToCart(10L, "0-1", new ArrayList<>(), redirectAttributes));
    }

    @Test
    void addToCart_ValidSeats_AddsItemAndRedirectsToCart() {
        when(showService.getShowById(10L)).thenReturn(show);
        List<CartItem> cart = new ArrayList<>();

        String view = pageController.addToCart(10L, "1-2, 1-3", cart, redirectAttributes);

        assertEquals("redirect:/kosar", view);
        assertEquals(1, cart.size());
        assertEquals(2, cart.get(0).getQuantity());
        assertEquals(9000, cart.get(0).getPrice());
    }

    @Test
    void removeFromCart_ValidAndInvalidIndex() {
        List<CartItem> cart = new ArrayList<>();
        cart.add(new CartItem(show, "1-1", "1-1", 1, 4500));

        assertEquals("redirect:/kosar", pageController.removeFromCart(5, cart));
        assertEquals(1, cart.size());

        assertEquals("redirect:/kosar", pageController.removeFromCart(0, cart));
        assertTrue(cart.isEmpty());
    }

    @Test
    void checkout_NotLoggedIn_RedirectsToLogin() {
        String view = pageController.checkout(new ArrayList<>(), session, redirectAttributes);
        assertEquals("redirect:/bejelentkezes", view);
    }

    @Test
    void checkout_EmptyCart_RedirectsToCartWithError() {
        session.setAttribute("loggedInUser", regularUser);

        String view = pageController.checkout(new ArrayList<>(), session, redirectAttributes);

        assertEquals("redirect:/kosar", view);
        assertNotNull(redirectAttributes.getFlashAttributes().get("error"));
    }

    @Test
    void checkout_Success_ClearsCartAndRedirectsToProfile() {
        session.setAttribute("loggedInUser", regularUser);
        List<CartItem> cart = new ArrayList<>(List.of(new CartItem(show, "1-1", "1-1", 1, 4500)));

        String view = pageController.checkout(cart, session, redirectAttributes);

        assertEquals("redirect:/profil", view);
        assertTrue(cart.isEmpty());
        assertNotNull(redirectAttributes.getFlashAttributes().get("success"));
        verify(ticketService).checkoutCart(any(), eq(regularUser));
    }

    @Test
    void checkout_Exceptions_RedirectToCartWithError() {
        session.setAttribute("loggedInUser", regularUser);
        List<CartItem> cart = new ArrayList<>(List.of(new CartItem(show, "1-1", "1-1", 1, 4500)));

        doThrow(new IllegalStateException("Foglalt")).when(ticketService).checkoutCart(any(), eq(regularUser));
        assertEquals("redirect:/kosar", pageController.checkout(cart, session, redirectAttributes));

        doThrow(new DataIntegrityViolationException("Duplicate")).when(ticketService).checkoutCart(any(), eq(regularUser));
        assertEquals("redirect:/kosar", pageController.checkout(cart, session, redirectAttributes));
    }

    @Test
    void loginAndProcessLogin_AllBranches() {
        assertEquals("bejelentkezes", pageController.login());

        when(userService.login("user1", "pass")).thenReturn(regularUser);
        assertEquals("redirect:/fooldal", pageController.processLogin("user1", "pass", session, model));
        assertEquals(regularUser, session.getAttribute("loggedInUser"));

        when(userService.login("admin", "pass")).thenReturn(adminUser);
        assertEquals("redirect:/admin", pageController.processLogin("admin", "pass", session, model));

        when(userService.login("bad", "bad")).thenThrow(new IllegalArgumentException("Hibás adatok"));
        assertEquals("bejelentkezes", pageController.processLogin("bad", "bad", session, model));
        assertEquals("Hibás adatok", model.getAttribute("error"));
    }

    @Test
    void registerAndProcessRegister_AllBranches() {
        assertEquals("regisztracio", pageController.register());

        assertEquals("redirect:/bejelentkezes?sikeresReg",
                pageController.processRegister("Teszt Elek", "t@e.hu", "teszt", "123456", model));

        doThrow(new IllegalStateException("Foglalt")).when(userService).registerUser(any(User.class));
        assertEquals("regisztracio",
                pageController.processRegister("Teszt Elek", "t@e.hu", "teszt", "123456", model));
        assertEquals("Foglalt", model.getAttribute("error"));
    }

    @Test
    void logout_ClearsSessionAndRedirects() {
        SessionStatus status = mock(SessionStatus.class);
        String view = pageController.logout(session, status);

        assertEquals("redirect:/fooldal", view);
        verify(status).setComplete();
        assertTrue(session.isInvalid());
    }

    @Test
    void userProfile_GuestAndLoggedIn() {
        assertEquals("redirect:/bejelentkezes", pageController.userProfile(session, model));

        session.setAttribute("loggedInUser", regularUser);
        when(ticketService.getTicketsByUser(1L)).thenReturn(Collections.emptyList());

        assertEquals("profil", pageController.userProfile(session, model));
        assertEquals(regularUser, model.getAttribute("user"));
    }

    @Test
    void adminDashboard_AccessControlAndDateFilter() {
        assertEquals("redirect:/bejelentkezes", pageController.adminDashboard(null, session, model));

        session.setAttribute("loggedInUser", regularUser);
        assertEquals("redirect:/fooldal", pageController.adminDashboard(null, session, model));

        session.setAttribute("loggedInUser", adminUser);
        when(roomService.getAllRooms()).thenReturn(new ArrayList<>(List.of(new Room("Kisterem", 4, 6), new Room("Nagyterem", 8, 12))));
        when(playService.getAllPlays()).thenReturn(Collections.emptyList());
        when(showService.getAllShows()).thenReturn(List.of(show));

        LocalDate customDate = LocalDate.now().plusDays(1);
        assertEquals("admin_felulet", pageController.adminDashboard(customDate, session, model));
        assertEquals(customDate, model.getAttribute("selectedDate"));
    }

    @Test
    void addShow_AccessControlSuccessAndError() {
        LocalDateTime time = LocalDateTime.now().plusDays(1);
        assertEquals("redirect:/fooldal", pageController.addShow(1L, 1L, time, session, redirectAttributes));

        session.setAttribute("loggedInUser", adminUser);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(new Room("Nagyterem", 5, 5)));
        when(playRepository.findById(1L)).thenReturn(Optional.of(new Play()));

        assertEquals("redirect:/admin", pageController.addShow(1L, 1L, time, session, redirectAttributes));
        assertNotNull(redirectAttributes.getFlashAttributes().get("success"));

        doThrow(new IllegalStateException("Ütközés")).when(showService).saveShow(any(Show.class));
        assertEquals("redirect:/admin", pageController.addShow(1L, 1L, time, session, redirectAttributes));
        assertEquals("Ütközés", redirectAttributes.getFlashAttributes().get("error"));
    }

    @Test
    void deleteShow_AccessControlAndDelete() {
        assertEquals("redirect:/fooldal", pageController.deleteShow(10L, session));

        session.setAttribute("loggedInUser", adminUser);
        when(ticketRepository.findByShowId(10L)).thenReturn(Collections.emptyList());

        assertEquals("redirect:/admin", pageController.deleteShow(10L, session));
        verify(ticketRepository).deleteAll(any());
        verify(showRepository).deleteById(10L);
    }
}