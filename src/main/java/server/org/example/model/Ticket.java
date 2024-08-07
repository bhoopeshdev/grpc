package server.org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Ticket {
    private Long ticketId;
    private Long userId;
    private Integer seatId;
    private String fromLocation;
    private String toLocation;
    private Double price;
    private String section;


    public Ticket(Ticket ticket) {
        this.ticketId = ticket.getTicketId();
        this.userId = ticket.getUserId();
        this.seatId = ticket.getSeatId();
        this.fromLocation = ticket.getFromLocation();
        this.toLocation = ticket.getToLocation();
        this.price = ticket.getPrice();
        this.section = ticket.getSection();
    }
}
