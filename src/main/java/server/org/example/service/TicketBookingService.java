package server.org.example.service;

import org.example.*;
import server.org.example.model.Ticket;
import server.org.example.model.User;
import server.org.example.repo.TicketRepo;
import server.org.example.repo.UserRepo;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@GRpcService
public class TicketBookingService extends TicketBookingServiceGrpc.TicketBookingServiceImplBase {

    Logger logger = LoggerFactory.getLogger(TicketBookingService.class);

    @Autowired
    private TicketRepo ticketRepo;

    @Autowired
    private UserRepo userRepo;

    public void bookTicket(TicketRequest request,
                           io.grpc.stub.StreamObserver<TicketResponse> responseObserver) {

        logger.info("request for booking ticket : " + request);
        Ticket ticket = Ticket.builder()
                .userId(request.getUserId())
                .fromLocation(request.getFromLocation())
                .toLocation(request.getToLocation())
                .price(request.getPrice())
                .section(request.getSection())
                .build();
        try {
            User user = userRepo.getUserById(request.getUserId());
            if(user == null) {
                throw new RuntimeException("passed user id does not exists");
            }
            Ticket res = ticketRepo.saveTicket(ticket);
            TicketResponse ticketResponse = createTicketResponse(res,user);
            logger.info("response is : " + ticketResponse);
            responseObserver.onNext(ticketResponse);
        }
        catch (Exception exc) {
            logger.info("Something went wrong while saving ticket " + exc.getMessage());
            responseObserver.onError(exc);
        }
        finally {
            responseObserver.onCompleted();
        }
    }

    public void getUserTickets(UserTicketRequest request,
                               io.grpc.stub.StreamObserver<UserTicketResponse> responseObserver) {

        logger.info("request for getting user tickets : " + request);
        try {
            Long userID = request.getUserId();
            User user = userRepo.getUserById(userID);
            if(user == null) {
                throw new RuntimeException("passed user id does not exists");
            }
            List<Ticket> tickets = ticketRepo.getTicketsByUserId(userID);
            List<TicketResponse> ticketResponses = new ArrayList<>();
            for(Ticket ticket : tickets) {
                TicketResponse ticketResponse = createTicketResponse(ticket, user);
                ticketResponses.add(ticketResponse);
            }
            logger.info("response total tickets fetched for user : " + tickets.size());
            responseObserver.onNext(
                UserTicketResponse.newBuilder()
                        .addAllTicketResponse(ticketResponses)
                        .build()
            );
        }
        catch (Exception exc) {
            logger.info("Something went while getting tickets for user : " + exc.getMessage());
            responseObserver.onError(exc);
        }
        finally {
            responseObserver.onCompleted();
        }
    }

    public void getAllTickets(Empty request,
                              io.grpc.stub.StreamObserver<AllTicketResponse> responseObserver) {
        logger.info("request for getting all tickets : " + request);
        try {
            List<Ticket> tickets = ticketRepo.getAllTickets();
            List<TicketResponse> ticketResponses = new ArrayList<>();
            for(Ticket ticket : tickets) {
                User user = userRepo.getUserById(ticket.getUserId());
                TicketResponse ticketResponse = createTicketResponse(ticket, user);
                ticketResponses.add(ticketResponse);
            }
            logger.info("response total tickets fetched " + tickets.size());
            responseObserver.onNext(
                    AllTicketResponse.newBuilder()
                            .addAllTicketResponse(ticketResponses)
                            .build()
            );
        }
        catch (Exception exc) {
            logger.info("Something went while getting all tickets : " + exc.getMessage());
            responseObserver.onError(exc);
        }
        finally {
            responseObserver.onCompleted();
        }
    }

    public void modifySeat(SeatModifyRequest request,
                           io.grpc.stub.StreamObserver<TicketResponse> responseObserver) {
        logger.info("request for seat modification : " + request);
        try {
            Ticket curTicket = ticketRepo.getTicketById(request.getTicketId());
            if(curTicket == null) {
                throw new RuntimeException("No ticket exists with passed ticket id");
            }
            if(!curTicket.getUserId().equals(request.getUserId())) {
                throw new RuntimeException("Ticket belong to other user, cannot modify the ticket");
            }
            User user = userRepo.getUserById(request.getUserId());
            if(user == null) {
                throw new RuntimeException("passed user id does not exists");
            }
            if(ticketRepo.isSeatAvailable(request.getModifiedSeatId())) {
                curTicket.setSeatId(request.getModifiedSeatId());
                ticketRepo.modifyTicket(curTicket);
                responseObserver.onNext(createTicketResponse(curTicket, user));
            } else {
                throw new RuntimeException("Seat not available for seat modification");
            }
        }
        catch (Exception exc) {
            logger.info("something went wrong : ",exc.getMessage());
            responseObserver.onError(exc);
        }
        finally {
            responseObserver.onCompleted();
        }
    }

    public void removeUser(RemoveUserRequest request,
                           io.grpc.stub.StreamObserver<RemovedUserSeatResponse> responseObserver) {
        logger.info("request for removing user " + request);
        try {
            List<Ticket> tickets = ticketRepo.getTicketsByUserId(request.getUserId());
            if(tickets.size() == 0) {
                logger.info("No tickets found");
            }
            List<TicketResponse> removedUserTicketResponse = new ArrayList<>();
            for(Ticket ticket : tickets) {
                User user = userRepo.getUserById(request.getUserId());
                if(user == null) {
                    throw new RuntimeException("passed user id does not exists");
                }
                removedUserTicketResponse.add(createTicketResponse(ticket, user));
                ticketRepo.removeTicketById(ticket.getTicketId());
            }
            responseObserver.onNext(RemovedUserSeatResponse
                    .newBuilder()
                    .addAllTicketResponse(removedUserTicketResponse)
                    .build()
            );
        } catch (Exception exc) {
            logger.info("Something went wrong " + exc.getMessage());
            responseObserver.onError(exc);
        }
        finally {
            responseObserver.onCompleted();
        }
    }



    private TicketResponse createTicketResponse(Ticket ticket, User user) {
        return TicketResponse.newBuilder()
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
    }
}
