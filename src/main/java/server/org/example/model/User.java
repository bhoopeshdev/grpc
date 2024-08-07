package server.org.example.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
}
