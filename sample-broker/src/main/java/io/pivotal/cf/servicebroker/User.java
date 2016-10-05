package io.pivotal.cf.servicebroker;

import lombok.Data;

@Data
class User {

    enum Role {Broker, User}

    private String name;
    private String password;
    private Role role;

    User(String name, String password, Role role) {
        super();
        this.name = name;
        this.password = password;
        this.role = role;
    }
}
