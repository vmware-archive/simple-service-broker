/*
 * Copyright (C) 2016-Present Pivotal Software, Inc. All rights reserved.
 * <p>
 * This program and the accompanying materials are made available under
 * the terms of the under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.ecosystem.servicebroker;

import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@Slf4j
@Profile("cloud")
public class HelloConfig {

    @Bean
    public HelloBrokerRepository helloRepository(Environment env) {
        String url = "http://" + env.getProperty("HELLO_HOST") + ":" + env.getProperty("HELLO_PORT");
        log.info("connecting to service at: " + url);
        try {
            return Feign.builder().encoder(new GsonEncoder()).decoder(new GsonDecoder()).target(HelloBrokerRepository.class, url);
        } catch (Throwable t) {
            log.error("error connecting to service.", t);
            throw new ServiceBrokerException(t);
        }
    }
}