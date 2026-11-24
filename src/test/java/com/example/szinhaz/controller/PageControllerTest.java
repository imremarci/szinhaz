package com.example.szinhaz.controller;

import com.example.szinhaz.model.CartItem;
import com.example.szinhaz.model.Play;
import com.example.szinhaz.model.Room;
import com.example.szinhaz.model.Show;
import com.example.szinhaz.model.Ticket;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PageControllerTest {

    private ShowService showService;
    private PlayService playService;
    private RoomService roomService;
    private TicketService ticketService;
    private UserService userService;
    private TicketRepository ticketRepository;
    private ShowRepository showRepository;
    private RoomRepository roomRepository;
    private PlayRepository playRepository;

    private PageController pageController;
    private Model model;
    private MockHttpSession session;
    private RedirectAttributesModelMap redirectAttributes;

    @BeforeEach
    void setUp() {
        showService = Mockito.mock(ShowService.class);
        playService = Mockito.mock(PlayService.class);
        roomService = Mockito.mock(RoomService.class);
        ticketService = Mockito.mock(TicketService.class);
        userService = Mockito.mock(UserService.class);
        ticketRepository = Mockito.mock(TicketRepository.class);
        showRepository = Mockito.mock(ShowRepository.class);
        roomRepository = Mockito.mock(RoomRepository.class);
        playRepository = Mockito.mock(PlayRepository.class);

        pageController = new PageController(showService, playService, roomService, ticketService, userService,
                ticketRepository, showRepository, roomRepository, playRepository);

        model = new ExtendedModelMap();
        session = new MockHttpSession();
        redirectAttributes = new RedirectAttributesModelMap();
    }

    private User loggedInUser(String role) {
        User user = new User("teszt", "jelszo", "Teszt Elek", "teszt@example.com");
        user.setId(1L);
        user.setRole(role);
        return user;
    }

    private Room room(String name) {
        return new Room(name, 10, 15);
    }

    private Play play(String title) {
        return new Play(title, "Rendezo", 120, 2024, "Leiras", "kep.png");
    }

    private Show show(Long id, LocalDateTime startTime) {
        Show show = new Show(startTime, room("Nagyterem"), play("Hamlet"));
        show.setId(id);
        return show;
    }

    private CartItem cartItem(Show show, int quantity, int price) {
        return new CartItem(show, "info", "1-1", quantity, price);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> flashAttributes() {
        return (Map<String, Object>) redirectAttributes.getFlashAttributes();
    }

    @Test
    void initializeCartShouldReturnEmptyMutableList() {
        List<CartItem> cart = pageController.initializeCart();

        assertThat(cart).isEmpty();
        cart.add(cartItem(null, 1, 4500));
        assertThat(cart).hasSize(1);
    }

    @Test
    void homeShouldExposePlaysRoomsAndTodaysShows() {
        List<Play> plays = List.of(play("Hamlet"));
        Room kamaraterem = room("Kamaraterem");
        Room studioterem = room("Stúdióterem");
        Room nagyterem = room("Nagyterem");
        Show withoutStart = show(1L, null);
        Show today = show(2L, LocalDate.now().atTime(19, 0));
        Show tomorrow = show(3L, LocalDate.now().plusDays(1).atTime(19, 0));

        when(playService.getAllPlays()).thenReturn(plays);
        when(roomService.getAllRooms()).thenReturn(new ArrayList<>(List.of(kamaraterem, studioterem, nagyterem)));
        when(showService.getAllShows()).thenReturn(List.of(withoutStart, today, tomorrow));

        String view = pageController.home(model);

        assertThat(view).isEqualTo("fooldal");
        assertThat(model.getAttribute("plays")).isSameAs(plays);
        assertThat((List<Room>) model.getAttribute("rooms"))
                .extracting(Room::getName)
                .containsExactly("Nagyterem", "Kamaraterem", "Stúdióterem");
        assertThat((List<Show>) model.getAttribute("shows")).containsExactly(today);
    }

    @Test
    void homeShouldKeepNagyteremFirstWhenItIsAlreadyFirst() {
        Room nagyterem = room("Nagyterem");
        Room kamaraterem = room("Kamaraterem");
        Room studioterem = room("Stúdióterem");

        when(playService.getAllPlays()).thenReturn(List.of());
        when(roomService.getAllRooms()).thenReturn(new ArrayList<>(List.of(nagyterem, kamaraterem, studioterem)));
        when(showService.getAllShows()).thenReturn(List.of());

        pageController.home(model);

        assertThat((List<Room>) model.getAttribute("rooms"))
                .extracting(Room::getName)
                .containsExactly("Nagyterem", "Kamaraterem", "Stúdióterem");
    }

    @Test
    void cartShouldCalculateEmptyCart() {
        String view = pageController.cart(new ArrayList<>(), model);

        assertThat(view).isEqualTo("kosar");
        assertThat(model.getAttribute("totalTickets")).isEqualTo(0);
        assertThat(model.getAttribute("ticketsPrice")).isEqualTo(0);
        assertThat(model.getAttribute("convenienceFee")).isEqualTo(0);
        assertThat(model.getAttribute("totalPrice")).isEqualTo(0);
    }

    @Test
    void cartShouldCalculateTotalsAndConvenienceFee() {
        List<CartItem> cart = List.of(
                cartItem(show(1L, LocalDateTime.now().plusDays(1)), 2, 9000),
                cartItem(show(2L, LocalDateTime.now().plusDays(2)), 1, 4500)
        );

        pageController.cart(cart, model);

        assertThat(model.getAttribute("totalTickets")).isEqualTo(3);
        assertThat(model.getAttribute("ticketsPrice")).isEqualTo(13500);
        assertThat(model.getAttribute("convenienceFee")).isEqualTo(390);
        assertThat(model.getAttribute("totalPrice")).isEqualTo(13890);
    }

    @Test
    void addToCartShouldRejectUnknownShow() {
        when(showService.getShowById(1L)).thenReturn(null);

        String view = pageController.addToCart(1L, "1-2", new ArrayList<>(), redirectAttributes);

        assertThat(view).isEqualTo("redirect:/eloadasok");
        assertThat(flashAttributes())
                .containsEntry("error", "A kiválasztott előadás nem található!");
    }

    @Test
    void addToCartShouldRejectNullSeatSelection() {
        when(showService.getShowById(1L)).thenReturn(show(1L, LocalDateTime.now().plusDays(1)));

        String view = pageController.addToCart(1L, null, new ArrayList<>(), redirectAttributes);

        assertThat(view).isEqualTo("redirect:/eloadas/1");
        assertThat(flashAttributes())
                .containsEntry("error", "Nem választottál ki egyetlen széket sem!");
    }

    @Test
    void addToCartShouldRejectBlankSeatSelection() {
        when(showService.getShowById(1L)).thenReturn(show(1L, LocalDateTime.now().plusDays(1)));

        String view = pageController.addToCart(1L, "   ", new ArrayList<>(), redirectAttributes);

        assertThat(view).isEqualTo("redirect:/eloadas/1");
        assertThat(flashAttributes())
                .containsEntry("error", "Nem választottál ki egyetlen széket sem!");
    }

    @Test
    void addToCartShouldAddEverySelectedSeat() {
        Show show = show(1L, LocalDateTime.now().plusDays(1));
        when(showService.getShowById(1L)).thenReturn(show);
        List<CartItem> cart = new ArrayList<>();

        String view = pageController.addToCart(1L, " 1-2 ,3-4 ", cart, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/kosar");
        assertThat(cart).hasSize(1);
        CartItem item = cart.get(0);
        assertThat(item.getShow()).isSameAs(show);
        assertThat(item.getRawSeats()).isEqualTo(" 1-2 ,3-4 ");
        assertThat(item.getSeatInfo()).isEqualTo("1. sor, 2. szék; 3. sor, 4. szék; ");
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getPrice()).isEqualTo(9000);
        assertThat(flashAttributes()).isEmpty();
    }

    @Test
    void addToCartShouldRejectSeatWithoutSeparator() {
        when(showService.getShowById(1L)).thenReturn(show(1L, LocalDateTime.now().plusDays(1)));

        String view = pageController.addToCart(1L, "12", new ArrayList<>(), redirectAttributes);

        assertThat(view).isEqualTo("redirect:/eloadas/1");
        assertThat(flashAttributes())
                .containsEntry("error", "Érvénytelen székválasztás!");
    }

    @Test
    void addToCartShouldRejectSeatWithTooManyParts() {
        when(showService.getShowById(1L)).thenReturn(show(1L, LocalDateTime.now().plusDays(1)));

        pageController.addToCart(1L, "1-2-3", new ArrayList<>(), redirectAttributes);

        assertThat(flashAttributes())
                .containsEntry("error", "Érvénytelen székválasztás!");
    }

    @Test
    void addToCartShouldRejectNonPositiveRow() {
        when(showService.getShowById(1L)).thenReturn(show(1L, LocalDateTime.now().plusDays(1)));

        pageController.addToCart(1L, "0-2", new ArrayList<>(), redirectAttributes);

        assertThat(flashAttributes())
                .containsEntry("error", "Érvénytelen székválasztás!");
    }

    @Test
    void addToCartShouldRejectNonPositiveSeat() {
        when(showService.getShowById(1L)).thenReturn(show(1L, LocalDateTime.now().plusDays(1)));

        pageController.addToCart(1L, "1-0", new ArrayList<>(), redirectAttributes);

        assertThat(flashAttributes())
                .containsEntry("error", "Érvénytelen székválasztás!");
    }

    @Test
    void addToCartShouldRejectNonNumericSeat() {
        when(showService.getShowById(1L)).thenReturn(show(1L, LocalDateTime.now().plusDays(1)));

        pageController.addToCart(1L, "a-b", new ArrayList<>(), redirectAttributes);

        assertThat(flashAttributes())
                .containsEntry("error", "Érvénytelen székválasztás!");
    }

    @Test
    void removeFromCartShouldRemoveItemAtValidIndex() {
        CartItem first = cartItem(null, 1, 4500);
        CartItem second = cartItem(null, 1, 4500);
        CartItem third = cartItem(null, 1, 4500);
        List<CartItem> cart = new ArrayList<>(List.of(first, second, third));

        String view = pageController.removeFromCart(1, cart);

        assertThat(view).isEqualTo("redirect:/kosar");
        assertThat(cart).containsExactly(first, third);
    }

    @Test
    void removeFromCartShouldIgnoreNegativeIndex() {
        CartItem item = cartItem(null, 1, 4500);
        List<CartItem> cart = new ArrayList<>(List.of(item));

        pageController.removeFromCart(-1, cart);

        assertThat(cart).containsExactly(item);
    }

    @Test
    void removeFromCartShouldIgnoreIndexAtSize() {
        CartItem item = cartItem(null, 1, 4500);
        List<CartItem> cart = new ArrayList<>(List.of(item));

        pageController.removeFromCart(1, cart);

        assertThat(cart).containsExactly(item);
    }

    @Test
    void removeFromCartShouldIgnoreEmptyCart() {
        List<CartItem> cart = new ArrayList<>();

        pageController.removeFromCart(0, cart);

        assertThat(cart).isEmpty();
    }

    @Test
    void checkoutShouldRedirectToLoginWithoutUser() {
        List<CartItem> cart = new ArrayList<>(List.of(cartItem(show(1L, LocalDateTime.now().plusDays(1)), 1, 4500)));

        String view = pageController.checkout(cart, session, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/bejelentkezes");
        verify(ticketService, never()).checkoutCart(any(), any());
    }

    @Test
    void checkoutShouldRejectNullCart() {
        session.setAttribute("loggedInUser", loggedInUser("USER"));

        String view = pageController.checkout(null, session, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/kosar");
        assertThat(flashAttributes())
                .containsEntry("error", "A kosarad üres! Előbb válassz ki legalább egy jegyet.");
    }

    @Test
    void checkoutShouldRejectEmptyCart() {
        session.setAttribute("loggedInUser", loggedInUser("USER"));

        String view = pageController.checkout(new ArrayList<>(), session, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/kosar");
        assertThat(flashAttributes())
                .containsEntry("error", "A kosarad üres! Előbb válassz ki legalább egy jegyet.");
    }

    @Test
    void checkoutShouldCompletePurchaseAndClearCart() {
        User user = loggedInUser("USER");
        session.setAttribute("loggedInUser", user);
        List<CartItem> cart = new ArrayList<>(List.of(cartItem(show(1L, LocalDateTime.now().plusDays(1)), 1, 4500)));
        doNothing().when(ticketService).checkoutCart(cart, user);

        String view = pageController.checkout(cart, session, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/profil");
        assertThat(cart).isEmpty();
        assertThat(flashAttributes())
                .containsEntry("success", "Sikeres jegyvásárlás!");
        verify(ticketService).checkoutCart(cart, user);
    }

    @Test
    void checkoutShouldReportIllegalArgumentError() {
        User user = loggedInUser("USER");
        session.setAttribute("loggedInUser", user);
        List<CartItem> cart = new ArrayList<>(List.of(cartItem(show(1L, LocalDateTime.now().plusDays(1)), 1, 4500)));
        doThrow(new IllegalArgumentException("Hibás kosár")).when(ticketService).checkoutCart(cart, user);

        String view = pageController.checkout(cart, session, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/kosar");
        assertThat(flashAttributes()).containsEntry("error", "Hibás kosár");
        assertThat(cart).hasSize(1);
    }

    @Test
    void checkoutShouldReportIllegalStateError() {
        User user = loggedInUser("USER");
        session.setAttribute("loggedInUser", user);
        List<CartItem> cart = new ArrayList<>(List.of(cartItem(show(1L, LocalDateTime.now().plusDays(1)), 1, 4500)));
        doThrow(new IllegalStateException("Foglalt szék")).when(ticketService).checkoutCart(cart, user);

        String view = pageController.checkout(cart, session, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/kosar");
        assertThat(flashAttributes()).containsEntry("error", "Foglalt szék");
    }

    @Test
    void checkoutShouldReportConcurrentReservation() {
        User user = loggedInUser("USER");
        session.setAttribute("loggedInUser", user);
        List<CartItem> cart = new ArrayList<>(List.of(cartItem(show(1L, LocalDateTime.now().plusDays(1)), 1, 4500)));
        doThrow(new DataIntegrityViolationException("constraint")).when(ticketService).checkoutCart(cart, user);

        String view = pageController.checkout(cart, session, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/kosar");
        assertThat(flashAttributes())
                .containsEntry("error", "Az egyik kiválasztott széket időközben már lefoglalták! Kérjük, válassz másik helyet.");
    }

    @Test
    void loginShouldRenderLoginPage() {
        assertThat(pageController.login()).isEqualTo("bejelentkezes");
    }

    @Test
    void processLoginShouldStoreUserAndRedirectToHome() {
        User user = loggedInUser("USER");
        when(userService.login("teszt", "jelszo")).thenReturn(user);

        String view = pageController.processLogin("teszt", "jelszo", session, model);

        assertThat(view).isEqualTo("redirect:/fooldal");
        assertThat(session.getAttribute("loggedInUser")).isSameAs(user);
    }

    @Test
    void processLoginShouldRedirectAdminToAdminPage() {
        User user = loggedInUser("ADMIN");
        when(userService.login("teszt", "jelszo")).thenReturn(user);

        String view = pageController.processLogin("teszt", "jelszo", session, model);

        assertThat(view).isEqualTo("redirect:/admin");
        assertThat(session.getAttribute("loggedInUser")).isSameAs(user);
    }

    @Test
    void processLoginShouldExposeErrorOnFailure() {
        when(userService.login("teszt", "rossz")).thenThrow(new IllegalArgumentException("Hibás adatok"));

        String view = pageController.processLogin("teszt", "rossz", session, model);

        assertThat(view).isEqualTo("bejelentkezes");
        assertThat(model.getAttribute("error")).isEqualTo("Hibás adatok");
        assertThat(session.getAttribute("loggedInUser")).isNull();
    }

    @Test
    void registerShouldRenderRegistrationPage() {
        assertThat(pageController.register()).isEqualTo("regisztracio");
    }

    @Test
    void processRegisterShouldCreateUserAndRedirect() {
        when(userService.registerUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String view = pageController.processRegister("Teszt Elek", "teszt@example.com", "teszt", "jelszo", model);

        assertThat(view).isEqualTo("redirect:/bejelentkezes?sikeresReg");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).registerUser(captor.capture());
        User created = captor.getValue();
        assertThat(created.getFullName()).isEqualTo("Teszt Elek");
        assertThat(created.getEmail()).isEqualTo("teszt@example.com");
        assertThat(created.getName()).isEqualTo("teszt");
        assertThat(created.getPassword()).isEqualTo("jelszo");
    }

    @Test
    void processRegisterShouldExposeIllegalArgumentError() {
        when(userService.registerUser(any(User.class))).thenThrow(new IllegalArgumentException("Hibás regisztráció"));

        String view = pageController.processRegister("Teszt Elek", "teszt@example.com", "teszt", "jelszo", model);

        assertThat(view).isEqualTo("regisztracio");
        assertThat(model.getAttribute("error")).isEqualTo("Hibás regisztráció");
    }

    @Test
    void processRegisterShouldExposeIllegalStateError() {
        when(userService.registerUser(any(User.class))).thenThrow(new IllegalStateException("Foglalt név"));

        String view = pageController.processRegister("Teszt Elek", "teszt@example.com", "teszt", "jelszo", model);

        assertThat(view).isEqualTo("regisztracio");
        assertThat(model.getAttribute("error")).isEqualTo("Foglalt név");
    }

    @Test
    void logoutShouldCompleteSessionAndInvalidateIt() {
        SessionStatus sessionStatus = Mockito.mock(SessionStatus.class);
        session.setAttribute("loggedInUser", loggedInUser("USER"));

        String view = pageController.logout(session, sessionStatus);

        assertThat(view).isEqualTo("redirect:/fooldal");
        verify(sessionStatus).setComplete();
        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void userProfileShouldRedirectToLoginWithoutUser() {
        assertThat(pageController.userProfile(session, model)).isEqualTo("redirect:/bejelentkezes");
    }

    @Test
    void userProfileShouldExposeUserAndTickets() {
        User user = loggedInUser("USER");
        session.setAttribute("loggedInUser", user);
        List<Ticket> tickets = List.of(new Ticket());
        when(ticketService.getTicketsByUser(1L)).thenReturn(tickets);

        String view = pageController.userProfile(session, model);

        assertThat(view).isEqualTo("profil");
        assertThat(model.getAttribute("user")).isSameAs(user);
        assertThat(model.getAttribute("tickets")).isSameAs(tickets);
    }

    @Test
    void adminDashboardShouldRedirectToLoginWithoutUser() {
        assertThat(pageController.adminDashboard(null, session, model)).isEqualTo("redirect:/bejelentkezes");
    }

    @Test
    void adminDashboardShouldRedirectNonAdminUser() {
        session.setAttribute("loggedInUser", loggedInUser("USER"));

        assertThat(pageController.adminDashboard(null, session, model)).isEqualTo("redirect:/fooldal");
    }

    @Test
    void adminDashboardShouldDefaultToTodayAndFilterShows() {
        session.setAttribute("loggedInUser", loggedInUser("ADMIN"));
        Room kamaraterem = room("Kamaraterem");
        Room studioterem = room("Stúdióterem");
        Room nagyterem = room("Nagyterem");
        Show withoutStart = show(1L, null);
        Show today = show(2L, LocalDate.now().atTime(19, 0));
        Show tomorrow = show(3L, LocalDate.now().plusDays(1).atTime(19, 0));
        List<Play> plays = List.of(play("Hamlet"));

        when(roomService.getAllRooms()).thenReturn(new ArrayList<>(List.of(kamaraterem, studioterem, nagyterem)));
        when(playService.getAllPlays()).thenReturn(plays);
        when(showService.getAllShows()).thenReturn(List.of(withoutStart, today, tomorrow));

        String view = pageController.adminDashboard(null, session, model);

        assertThat(view).isEqualTo("admin_felulet");
        assertThat(model.getAttribute("selectedDate")).isEqualTo(LocalDate.now());
        assertThat(model.getAttribute("plays")).isSameAs(plays);
        assertThat((List<Room>) model.getAttribute("rooms"))
                .extracting(Room::getName)
                .containsExactly("Nagyterem", "Kamaraterem", "Stúdióterem");
        assertThat((List<Show>) model.getAttribute("shows")).containsExactly(today);
    }

    @Test
    void adminDashboardShouldUseProvidedDateAndKeepNagyteremFirst() {
        session.setAttribute("loggedInUser", loggedInUser("ADMIN"));
        Room nagyterem = room("Nagyterem");
        Room kamaraterem = room("Kamaraterem");
        Room studioterem = room("Stúdióterem");
        LocalDate selectedDate = LocalDate.of(2030, 5, 5);
        Show onSelectedDate = show(4L, selectedDate.atTime(19, 0));
        Show today = show(5L, LocalDate.now().atTime(19, 0));

        when(roomService.getAllRooms()).thenReturn(new ArrayList<>(List.of(nagyterem, kamaraterem, studioterem)));
        when(playService.getAllPlays()).thenReturn(List.of());
        when(showService.getAllShows()).thenReturn(List.of(onSelectedDate, today));

        pageController.adminDashboard(selectedDate, session, model);

        assertThat(model.getAttribute("selectedDate")).isEqualTo(selectedDate);
        assertThat((List<Room>) model.getAttribute("rooms"))
                .extracting(Room::getName)
                .containsExactly("Nagyterem", "Kamaraterem", "Stúdióterem");
        assertThat((List<Show>) model.getAttribute("shows")).containsExactly(onSelectedDate);
    }

    @Test
    void addShowShouldRedirectWithoutUser() {
        String view = pageController.addShow(1L, 2L, LocalDateTime.now().plusDays(1), session, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/fooldal");
        verify(showService, never()).saveShow(any());
    }

    @Test
    void addShowShouldRedirectNonAdminUser() {
        session.setAttribute("loggedInUser", loggedInUser("USER"));

        String view = pageController.addShow(1L, 2L, LocalDateTime.now().plusDays(1), session, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/fooldal");
        verify(showService, never()).saveShow(any());
    }

    @Test
    void addShowShouldCreateShowForAdmin() {
        session.setAttribute("loggedInUser", loggedInUser("ADMIN"));
        Room room = room("Nagyterem");
        Play play = play("Hamlet");
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(playRepository.findById(2L)).thenReturn(Optional.of(play));
        when(showService.saveShow(any(Show.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String view = pageController.addShow(1L, 2L, startTime, session, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin");
        assertThat(flashAttributes())
                .containsEntry("success", "Előadás sikeresen rögzítve!");
        ArgumentCaptor<Show> captor = ArgumentCaptor.forClass(Show.class);
        verify(showService).saveShow(captor.capture());
        assertThat(captor.getValue().getStartTime()).isEqualTo(startTime);
        assertThat(captor.getValue().getRoom()).isSameAs(room);
        assertThat(captor.getValue().getPlay()).isSameAs(play);
    }

    @Test
    void addShowShouldReportMissingRoom() {
        session.setAttribute("loggedInUser", loggedInUser("ADMIN"));
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());
        when(playRepository.findById(2L)).thenReturn(Optional.of(play("Hamlet")));
        when(showService.saveShow(any(Show.class)))
                .thenThrow(new IllegalArgumentException("A terem kötelező!"));

        String view = pageController.addShow(1L, 2L, LocalDateTime.now().plusDays(1), session, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin");
        assertThat(flashAttributes()).containsEntry("error", "A terem kötelező!");
    }

    @Test
    void addShowShouldReportMissingPlay() {
        session.setAttribute("loggedInUser", loggedInUser("ADMIN"));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room("Nagyterem")));
        when(playRepository.findById(2L)).thenReturn(Optional.empty());
        when(showService.saveShow(any(Show.class)))
                .thenThrow(new IllegalArgumentException("A darab kötelező!"));

        pageController.addShow(1L, 2L, LocalDateTime.now().plusDays(1), session, redirectAttributes);

        assertThat(flashAttributes()).containsEntry("error", "A darab kötelező!");
    }

    @Test
    void addShowShouldReportOverlappingShow() {
        session.setAttribute("loggedInUser", loggedInUser("ADMIN"));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room("Nagyterem")));
        when(playRepository.findById(2L)).thenReturn(Optional.of(play("Hamlet")));
        when(showService.saveShow(any(Show.class)))
                .thenThrow(new IllegalStateException("Ütközés!"));

        pageController.addShow(1L, 2L, LocalDateTime.now().plusDays(1), session, redirectAttributes);

        assertThat(flashAttributes()).containsEntry("error", "Ütközés!");
    }

    @Test
    void deleteShowShouldRedirectWithoutUser() {
        assertThat(pageController.deleteShow(1L, session)).isEqualTo("redirect:/fooldal");
        verify(showRepository, never()).deleteById(any());
    }

    @Test
    void deleteShowShouldRedirectNonAdminUser() {
        session.setAttribute("loggedInUser", loggedInUser("USER"));

        assertThat(pageController.deleteShow(1L, session)).isEqualTo("redirect:/fooldal");
        verify(showRepository, never()).deleteById(any());
    }

    @Test
    void deleteShowShouldRemoveTicketsAndShowForAdmin() {
        session.setAttribute("loggedInUser", loggedInUser("ADMIN"));
        List<Ticket> tickets = List.of(new Ticket());
        when(ticketRepository.findByShowId(7L)).thenReturn(tickets);

        String view = pageController.deleteShow(7L, session);

        assertThat(view).isEqualTo("redirect:/admin");
        verify(ticketRepository).deleteAll(tickets);
        verify(showRepository).deleteById(7L);
    }
}
