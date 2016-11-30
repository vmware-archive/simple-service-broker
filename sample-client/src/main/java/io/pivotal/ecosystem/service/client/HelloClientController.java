/**
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

package io.pivotal.ecosystem.service.client;

import io.pivotal.ecosystem.service.connector.HelloException;
import io.pivotal.ecosystem.service.connector.HelloRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloClientController {

    public HelloClientController(HelloRepository helloRepository) {
        this.helloRepository = helloRepository;
    }

    private HelloRepository helloRepository;

    @RequestMapping(value = "/greeting", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    ResponseEntity<String> greeting(@RequestParam(value = "username") String username) {
        try {
            return new ResponseEntity<>(helloRepository.greeting(username), HttpStatus.OK);
        } catch (HelloException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
    }
}