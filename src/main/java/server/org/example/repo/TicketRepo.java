package server.org.example.repo;

import server.org.example.model.Ticket;

import java.util.List;

public interface TicketRepo {

    Ticket saveTicket(Ticket ticket);

    Ticket getTicketById(Long ticketId);

    List<Ticket> getTicketsByUserId(Long userId);

    List<Ticket> getAllTickets();

    Ticket modifyTicket(Ticket ticket);

    Boolean isSeatAvailable(Integer seatId);

    void removeTicketById(Long ticketId);
}
