package server.org.example.service;

import io.grpc.stub.StreamObserver;
import org.example.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.org.example.model.Ticket;
import server.org.example.model.User;
import server.org.example.repo.TicketRepo;
import server.org.example.repo.UserRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketBookingServiceTest {

    @Mock
    private TicketRepo ticketRepo;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private TicketBookingService ticketBookingService;

    private User user;
    private Ticket ticket;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        ticket = Ticket.builder()
                .ticketId(UUID.randomUUID().getMostSignificantBits())
                .userId(user.getUserId())
                .fromLocation("Location A")
                .toLocation("Location B")
                .price(100.0)
                .seatId(1)
                .build();
    }

    @Test
    void testBookTicket() {
        TicketRequest request = TicketRequest.newBuilder()
                .setUserId(user.getUserId())
                .setFromLocation("Location A")
                .setToLocation("Location B")
                .setPrice(100.0)
                .build();

        StreamObserver<TicketResponse> responseObserver = mock(StreamObserver.class);

        when(userRepo.getUserById(user.getUserId())).thenReturn(user);
        when(ticketRepo.saveTicket(any(Ticket.class))).thenReturn(ticket);

        ticketBookingService.bookTicket(request, responseObserver);
        TicketResponse ticketResponse = TicketResponse.newBuilder()
                .setTicketId(ticket.getTicketId())
                .setSeatId(ticket.getSeatId())
                .setUserId(ticket.getUserId())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setEmail(user.getEmail())
                .setFromLocation(ticket.getFromLocation())
                .setToLocation(ticket.getToLocation())
                .setPrice(ticket.getPrice())
                .build();

        verify(userRepo, times(1)).getUserById(user.getUserId());
        verify(ticketRepo, times(1)).saveTicket(any(Ticket.class));
        verify(responseObserver, times(1)).onNext(ticketResponse);
        verify(responseObserver, times(1)).onCompleted();
    }

    @Test
    public void testGetUserTickets() {
        UserTicketRequest request = UserTicketRequest.newBuilder()
                .setUserId(user.getUserId())
                .build();

        StreamObserver<UserTicketResponse> responseObserver = mock(StreamObserver.class);

        List<Ticket> tickets = new ArrayList<>();
        tickets.add(ticket);

        when(userRepo.getUserById(user.getUserId())).thenReturn(user);
        when(ticketRepo.getTicketsByUserId(user.getUserId())).thenReturn(tickets);

        ticketBookingService.getUserTickets(request, responseObserver);
        TicketResponse ticketResponse = TicketResponse.newBuilder()
                .setTicketId(ticket.getTicketId())
                .setSeatId(ticket.getSeatId())
                .setUserId(ticket.getUserId())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setEmail(user.getEmail())
                .setFromLocation(ticket.getFromLocation())
                .setToLocation(ticket.getToLocation())
                .setPrice(ticket.getPrice())
                .build();
        List<TicketResponse> ticketResponses = List.of(ticketResponse);
        UserTicketResponse userTicketResponse = UserTicketResponse.newBuilder()
                        .addTicketResponse(ticketResponse)
                                .build();

        verify(userRepo, times(1)).getUserById(user.getUserId());
        verify(ticketRepo, times(1)).getTicketsByUserId(user.getUserId());
        verify(responseObserver, times(1)).onNext(userTicketResponse);
        verify(responseObserver, times(1)).onCompleted();
    }

    @Test
    public void testGetAllTickets() {
        Empty request = Empty.newBuilder().build();

        StreamObserver<AllTicketResponse> responseObserver = mock(StreamObserver.class);

        List<Ticket> tickets = new ArrayList<>();
        tickets.add(ticket);

        when(ticketRepo.getAllTickets()).thenReturn(tickets);
        when(userRepo.getUserById(anyLong())).thenReturn(user);

        ticketBookingService.getAllTickets(request, responseObserver);

        verify(ticketRepo, times(1)).getAllTickets();
        verify(userRepo, times(1)).getUserById(user.getUserId());
        verify(responseObserver, times(1)).onNext(any(AllTicketResponse.class));
        verify(responseObserver, times(1)).onCompleted();
    }

    @Test
    public void testModifySeatIfAvailable() {
        SeatModifyRequest request = SeatModifyRequest.newBuilder()
                .setTicketId(ticket.getTicketId())
                .setUserId(user.getUserId())
                .setSeatId(1)
                .setModifiedSeatId(2)
                .build();

        StreamObserver<TicketResponse> responseObserver = mock(StreamObserver.class);

        when(ticketRepo.getTicketById(ticket.getTicketId())).thenReturn(ticket);
        when(userRepo.getUserById(user.getUserId())).thenReturn(user);
        when(ticketRepo.isSeatAvailable(request.getModifiedSeatId())).thenReturn(true);

        Ticket modifiedTicket = new Ticket(ticket);
        modifiedTicket.setSeatId(request.getModifiedSeatId());
        when(ticketRepo.modifyTicket(ticket)).thenReturn(modifiedTicket);

        ticketBookingService.modifySeat(request, responseObserver);
        TicketResponse ticketResponse = TicketResponse.newBuilder()
                .setTicketId(modifiedTicket.getTicketId())
                .setSeatId(modifiedTicket.getSeatId())
                .setUserId(modifiedTicket.getUserId())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setEmail(user.getEmail())
                .setFromLocation(modifiedTicket.getFromLocation())
                .setToLocation(modifiedTicket.getToLocation())
                .setPrice(modifiedTicket.getPrice())
                .build();
        verify(ticketRepo, times(1)).getTicketById(ticket.getTicketId());
        verify(userRepo, times(1)).getUserById(user.getUserId());
        verify(ticketRepo, times(1)).isSeatAvailable(request.getModifiedSeatId());
        verify(ticketRepo, times(1)).modifyTicket(ticket);
        verify(responseObserver, times(1)).onNext(ticketResponse);
        verify(responseObserver, times(1)).onCompleted();
    }

    @Test
    public void testModifySeatIfNotAvailable() {
        SeatModifyRequest request = SeatModifyRequest.newBuilder()
                .setTicketId(ticket.getTicketId())
                .setUserId(user.getUserId())
                .setSeatId(1)
                .setModifiedSeatId(2)
                .build();

        StreamObserver<TicketResponse> responseObserver = mock(StreamObserver.class);

        when(ticketRepo.getTicketById(ticket.getTicketId())).thenReturn(ticket);
        when(userRepo.getUserById(user.getUserId())).thenReturn(user);
        when(ticketRepo.isSeatAvailable(request.getModifiedSeatId())).thenReturn(false);

        ticketBookingService.modifySeat(request, responseObserver);

        verify(ticketRepo, times(1)).getTicketById(ticket.getTicketId());
        verify(userRepo, times(1)).getUserById(user.getUserId());
        verify(ticketRepo, times(1)).isSeatAvailable(request.getModifiedSeatId());

        // modifyTicket won't be called and exception is raised
        verify(ticketRepo, times(0)).modifyTicket(ticket);
        verify(responseObserver, times(1)).onError(any(RuntimeException.class));
        verify(responseObserver, times(1)).onCompleted();
    }

    @Test
    public void testRemoveUser() {
        RemoveUserRequest request = RemoveUserRequest.newBuilder()
                .setUserId(user.getUserId())
                .build();

        StreamObserver<RemovedUserSeatResponse> responseObserver = mock(StreamObserver.class);

        List<Ticket> tickets = new ArrayList<>();
        tickets.add(ticket);

        when(ticketRepo.getTicketsByUserId(request.getUserId())).thenReturn(tickets);
        when(userRepo.getUserById(request.getUserId())).thenReturn(user);

        ticketBookingService.removeUser(request, responseObserver);
        TicketResponse ticketResponse = TicketResponse.newBuilder()
                .setTicketId(ticket.getTicketId())
                .setSeatId(ticket.getSeatId())
                .setUserId(ticket.getUserId())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setEmail(user.getEmail())
                .setFromLocation(ticket.getFromLocation())
                .setToLocation(ticket.getToLocation())
                .setPrice(ticket.getPrice())
                .build();
        RemovedUserSeatResponse removedUserSeatResponse = RemovedUserSeatResponse.newBuilder()
                        .addTicketResponse(ticketResponse).build();

        verify(ticketRepo, times(1)).getTicketsByUserId(request.getUserId());
        verify(userRepo, times(1)).getUserById(request.getUserId());
        verify(responseObserver, times(1)).onNext(removedUserSeatResponse);
        verify(responseObserver, times(1)).onCompleted();
    }
}
