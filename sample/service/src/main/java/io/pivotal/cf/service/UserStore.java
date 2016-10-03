package io.pivotal.cf.service;


import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
class UserStore {

    private static final Map<String, String> users = new HashMap<>();

    void addUser(@NonNull String userId, @NonNull String password) {
        if (userExists(userId)) {
            throw new HelloException("user: " + userId + " already exists.");
        }
        users.put(userId, password);
    }

    void deleteUser(@NonNull String userId) {
        if (!userExists(userId)) {
            throw new HelloException("user: " + userId + " does not exist.");
        }
        users.remove(userId);
    }

    boolean userExists(@NonNull String userId) {
        return users.containsKey(userId);
    }

    public boolean validateUser(@NonNull String userId, @NonNull String password) {
        if (!userExists(userId)) {
            return false;
        }

        if (users.get(userId).equals(password)) {
            return true;
        }

        return false;
    }
}
