/*
 Copyright (C) 2016-Present Pivotal Software, Inc. All rights reserved.

 This program and the accompanying materials are made available under
 the terms of the under the Apache License, Version 2.0 (the "License”);
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package io.pivotal.ecosystem.service;

import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;

@Data
public class User implements Serializable {

    public enum Role {Broker, User};

    private String name;
    private String password;
    private Role role;

    public User(@NonNull String name, @NonNull Role role) {
        this();
        this.name = name;
        this.role = role;
    }

    public User(@NonNull String name, @NonNull String password, @NonNull Role role) {
        this(name, role);
        this.password = password;
    }

    public User() {
        super();
    }
}