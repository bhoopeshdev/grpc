package client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.TicketBookingServiceGrpc;
import org.example.UserServiceGrpc;

import static org.example.TicketBookingServiceGrpc.TicketBookingServiceBlockingStub;
import static org.example.UserServiceGrpc.UserServiceBlockingStub;

public class GrpcHelper {

    private static final String IP_ADDRESS = "localhost";
    private static final Integer PORT = 6565;
    private ManagedChannel managedChannel;
    private TicketBookingServiceBlockingStub ticketBookingStub;
    private UserServiceBlockingStub userServiceStub;

    GrpcHelper() {
        managedChannel =
                ManagedChannelBuilder
                        .forAddress("localhost",6565)
                        .usePlaintext()
                        .build();
        ticketBookingStub =
                TicketBookingServiceGrpc.newBlockingStub(managedChannel);
        userServiceStub =
                UserServiceGrpc.newBlockingStub(managedChannel);
    }

    public TicketBookingServiceBlockingStub getTicketBookingStub() {
        return ticketBookingStub;
    }

    public UserServiceBlockingStub getUserServiceStub() {
        return userServiceStub;
    }
}
