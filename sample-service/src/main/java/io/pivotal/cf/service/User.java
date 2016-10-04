package io.pivotal.cf.service;

import lombok.Data;
import lombok.NonNull;

@Data
public class User {

    private String name;
    private String password;

    public User(@NonNull String name, @NonNull String password) {
        this();
        this.name = name;
        this.password = password;
    }

    public User() {
        super();
    }
}