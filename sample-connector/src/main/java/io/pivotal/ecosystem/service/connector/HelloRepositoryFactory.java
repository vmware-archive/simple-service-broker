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

package io.pivotal.ecosystem.service.connector;

import feign.Feign;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloRepositoryFactory {

    public HelloRepository create(HelloServiceInfo info) {
        log.info("creating helloRepository with info: " + info);

        return Feign.builder()
                .errorDecoder(new HelloErrorDecoder())
                .target(HelloRepository.class, info.getUri());
    }
}
