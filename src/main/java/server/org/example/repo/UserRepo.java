package server.org.example.repo;


import server.org.example.model.User;

public interface UserRepo {

    User getUserById(Long userId);

    void saveUser(User user);
}
