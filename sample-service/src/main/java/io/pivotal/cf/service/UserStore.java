package io.pivotal.cf.service;

import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
class UserStore {

    private static final Map<String, User> users = new HashMap<>();

    void addUser(@NonNull User user) {
        if (userExists(user.getName())) {
            throw new HelloException("user: " + user + " already exists.");
        }
        users.put(user.getName(), user);
    }

    void deleteUser(@NonNull String userName) {
        if (!userExists(userName)) {
            throw new HelloException("user: " + userName + " does not exist.");
        }
        users.remove(userName);
    }

    User getUser(@NonNull String userName) {
        return users.get(userName);
    }

    boolean userExists(@NonNull String userName) {
        return getUser(userName) != null;
    }

    public boolean validateUser(@NonNull User user) {
        if (!userExists(user.getName())) {
            return false;
        }

        if (users.get(user).getPassword().equals(user.getPassword())) {
            return true;
        }

        return false;
    }
}
