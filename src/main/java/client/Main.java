package client;

import org.example.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.example.TicketBookingServiceGrpc.TicketBookingServiceBlockingStub;


public class Main {

    static Logger logger = LoggerFactory.getLogger(Main.class);
    static GrpcHelper grpcHelper;
    static UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;
    static TicketBookingServiceBlockingStub ticketBookingStub;

    public static void main(String[] args) {

        GrpcHelper grpcHelper = new GrpcHelper();
        userServiceBlockingStub = grpcHelper.getUserServiceStub();
        ticketBookingStub = grpcHelper.getTicketBookingStub();

        //create Users
        List<UserResponse> userResponses = createUsers();

        // Book Ticket
        TicketResponse ticketResponse1 = bookTicket(userResponses.get(0));
        TicketResponse ticketResponse2 = bookTicket(userResponses.get(1));

        // Get All ticket
        AllTicketResponse allTicketResponse = getAllTicket();

        //Modify Seat Ticket
        TicketResponse ticketResponse3 = modifySeat(ticketResponse1);

        // Get Ticket of User
        UserTicketResponse userTicketResponse = getTicketsBuUserId(userResponses.get(1));

        // Delete User Seat and Get all tickets
        RemovedUserSeatResponse removedUserSeatResponse = removeUserSeat(userResponses.get(0));
        AllTicketResponse allTicketResponse2 = getAllTicket();
    }

    private static RemovedUserSeatResponse removeUserSeat(UserResponse userResponse) {
        RemoveUserRequest removeUserRequest = RemoveUserRequest.newBuilder()
                .setUserId(userResponse.getUserId())
                .build();
        RemovedUserSeatResponse removedUserSeatResponse = ticketBookingStub.removeUser(removeUserRequest);
        logger.info("response from removing tickets for user " + userResponse.getUserId() + " is " +
                removedUserSeatResponse);
        return removedUserSeatResponse;
    }

    private static UserTicketResponse getTicketsBuUserId(UserResponse userResponse) {
        UserTicketRequest userTicketRequest = UserTicketRequest.newBuilder()
                .setUserId(userResponse.getUserId())
                .build();
        UserTicketResponse userTicketResponse = ticketBookingStub.getUserTickets(userTicketRequest);
        logger.info("response from fetching all ticket for user id " + userResponse.getUserId() + " is : " +
                userTicketResponse);
        return userTicketResponse;
    }

    private static TicketResponse modifySeat(TicketResponse ticketResponse1) {

        logger.info("modifying seat from " + ticketResponse1.getSeatId()  +
                " to " + (ticketResponse1.getSeatId() + 5));

        SeatModifyRequest seatModifyRequest = SeatModifyRequest.newBuilder()
                .setSeatId(ticketResponse1.getSeatId())
                .setModifiedSeatId(ticketResponse1.getSeatId() + 5)
                .setUserId(ticketResponse1.getUserId())
                .setTicketId(ticketResponse1.getTicketId())
                .build();
        TicketResponse ticketResponse = ticketBookingStub.modifySeat(seatModifyRequest);
        logger.info("response from modify seat request " + ticketResponse);
        return ticketResponse;
    }

    private static AllTicketResponse getAllTicket() {
        AllTicketResponse allTicketResponse =
                ticketBookingStub.getAllTickets(Empty.newBuilder().build());
        logger.info("response for getting all tickets " + allTicketResponse);
        return allTicketResponse;
    }

    private static List<UserResponse> createUsers() {
        List<UserResponse> userResponses = new ArrayList<>();
        // create first User
        UserCreateRequest userCreateRequest1 = UserCreateRequest.newBuilder()
                .setFirstName("john")
                .setLastName("Doe")
                .setEmail("john.doe@gmail.com")
                .build();
        UserResponse userResponse1 = userServiceBlockingStub.createUser(userCreateRequest1);
        logger.info("user response after creation : " + userResponse1);
        userResponses.add(userResponse1);

        // create second User
        UserCreateRequest userCreateRequest2 = UserCreateRequest.newBuilder()
                .setFirstName("Bhoopesh")
                .setLastName("Kumar")
                .setEmail("bhoopesh.kumar@gmail.com")
                .build();
        UserResponse userResponse2 = userServiceBlockingStub.createUser(userCreateRequest2);
        logger.info("user response after creation : " + userResponse2);
        userResponses.add(userResponse2);
        return userResponses;
    }

    private static TicketResponse bookTicket(UserResponse userResponse) {
        TicketRequest ticketRequest = TicketRequest.newBuilder()
                .setUserId(userResponse.getUserId())
                .setFromLocation("London")
                .setToLocation("France")
                .setSection("sectionA")
                .setPrice(20)
                .setSection("sectionA")
                .build();
        TicketResponse ticketResponse = ticketBookingStub.bookTicket(ticketRequest);
        logger.info("client response received : " + ticketResponse);
        return ticketResponse;
    }
}
