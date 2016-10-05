package io.pivotal.cf.service;

import lombok.Data;
import lombok.NonNull;

@Data
public class User {

    public enum UserType {User, Admin};

    private String name;
    private String password;
    private UserType type;

    public User(@NonNull String name, @NonNull String password, @NonNull UserType type) {
        this();
        this.name = name;
        this.password = password;
        this.type = type;
    }

    public User() {
        super();
    }
}