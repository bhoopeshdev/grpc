package server.org.example.service;

import org.example.UserResponse;
import org.example.UserServiceGrpc;
import server.org.example.model.User;
import server.org.example.repo.UserRepo;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@GRpcService
public class UserService extends UserServiceGrpc.UserServiceImplBase {

    private Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepo userRepo;


    public void createUser(org.example.UserCreateRequest request,
                           io.grpc.stub.StreamObserver<org.example.UserResponse> responseObserver) {
        logger.info("request for creating user : " + request);
        try {
            User user = User.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .build();

            userRepo.saveUser(user);
            UserResponse userResponse = UserResponse.newBuilder()
                    .setUserId(user.getUserId())
                    .setFirstName(user.getFirstName())
                    .setLastName(user.getLastName())
                    .setEmail(user.getEmail())
                    .build();
            responseObserver.onNext(userResponse);
        }
        catch (Exception exc) {
            logger.info("something went wrong : " + exc.getMessage());
            responseObserver.onError(exc);
        }
        finally {
            responseObserver.onCompleted();
        }
    }

    /**
     */
    public void getUserById(org.example.UserGetRequest request,
                            io.grpc.stub.StreamObserver<org.example.UserResponse> responseObserver) {
        logger.info("request for getting user : " + request);
        try {
            User user = userRepo.getUserById(request.getUserId());
            if (user == null) {
                throw new RuntimeException("No user found with passed id");
            }
            UserResponse userResponse = UserResponse.newBuilder()
                    .setUserId(user.getUserId())
                    .setFirstName(user.getFirstName())
                    .setLastName(user.getLastName())
                    .setEmail(user.getEmail())
                    .build();
            responseObserver.onNext(userResponse);
        }
        catch (Exception exc) {
            logger.info("something went wrong : " + exc.getMessage());
            responseObserver.onError(exc);
        }
        finally {
            responseObserver.onCompleted();
        }
    }
}
