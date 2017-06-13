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
