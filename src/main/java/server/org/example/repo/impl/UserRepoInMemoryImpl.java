package server.org.example.repo.impl;

import server.org.example.model.User;
import server.org.example.repo.UserRepo;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepoInMemoryImpl implements UserRepo {

    Map<Long, User> userMap;
    AtomicLong userIdSequence;

    UserRepoInMemoryImpl() {
        userMap = new ConcurrentHashMap<>();
        userIdSequence = new AtomicLong(0);
    }

    @Override
    public User getUserById(Long userId) {
        if(userMap.containsKey(userId)) {
            return userMap.get(userId);
        }
        return null;
    }

    @Override
    public void saveUser(User user) {
        Long userId = userIdSequence.incrementAndGet();
        user.setUserId(userId);
        userMap.put(user.getUserId(),user);
    }
}
