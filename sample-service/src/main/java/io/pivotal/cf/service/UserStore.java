package io.pivotal.cf.service;

import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
class UserStore {

    private static final Map<String, User> users = new HashMap<>();

    void save(@NonNull User user) {
        users.put(user.getName(), user);
    }

    void delete(@NonNull String userName) {
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
