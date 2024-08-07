package server.org.example.repo.impl;

import server.org.example.model.Ticket;
import server.org.example.repo.TicketRepo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TicketRepoInMemoryImpl implements TicketRepo {

    Map<Long, Ticket> ticketMap;
    AtomicLong ticketIdSequence;
    AtomicInteger seatBookingSequence;

    TicketRepoInMemoryImpl() {
        ticketMap = new ConcurrentHashMap<>();
        seatBookingSequence = new AtomicInteger(0);
        ticketIdSequence = new AtomicLong(0L);
    }

    @Override
    public Ticket saveTicket(Ticket ticket) {
        Integer seatId = seatBookingSequence.incrementAndGet();
        while (!isSeatAvailable(seatId)) {
            seatId = seatBookingSequence.incrementAndGet();
        }
        ticket.setSeatId(seatId);
        Long ticketId = ticketIdSequence.incrementAndGet();
        ticket.setTicketId(ticketId);
        ticketMap.put(ticket.getTicketId(),ticket);
        return ticket;
    }

    @Override
    public Ticket getTicketById(Long ticketId) {
        return ticketMap.getOrDefault(ticketId,null);
    }

    @Override
    public List<Ticket> getTicketsByUserId(Long userId) {
        List<Ticket> tickets = ticketMap.entrySet().stream()
                .filter(m -> m.getValue().getUserId().equals(userId))
                .map(m -> m.getValue()).toList();
        return tickets;
    }

    @Override
    public List<Ticket> getAllTickets() {
        return ticketMap.entrySet().stream()
                .map(m -> m.getValue())
                .toList();
    }

    @Override
    public Ticket modifyTicket(Ticket ticket) {
        if (!ticketMap.containsKey(ticket.getTicketId())) {
            throw new NoSuchElementException("passed ticket id not found for modification");
        }
        ticketMap.put(ticket.getTicketId(),ticket);
        return ticket;
    }

    @Override
    public Boolean isSeatAvailable(Integer seatId) {
        return ticketMap.entrySet().stream()
                .noneMatch(m -> m.getValue().getSeatId().equals(seatId));
    }

    @Override
    public void removeTicketById(Long ticketId) {
        Optional<Long> ticketIdToRemove = ticketMap.entrySet().stream()
                .filter(m -> m.getKey()
                        .equals(ticketId))
                .map(m -> m.getKey()).findFirst();
        if(ticketIdToRemove.isPresent()) {
            ticketMap.remove(ticketIdToRemove.get());
        }
    }
}
