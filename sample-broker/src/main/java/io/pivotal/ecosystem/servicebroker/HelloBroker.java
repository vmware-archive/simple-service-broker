/**
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

import io.pivotal.ecosystem.servicebroker.model.LastOperation;
import io.pivotal.ecosystem.servicebroker.model.Operation;
import io.pivotal.ecosystem.servicebroker.model.ServiceBinding;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import io.pivotal.ecosystem.servicebroker.service.DefaultServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.OperationState;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Example service broker. Can be used as a template for creating custom service brokers
 * by adding your code in the appropriate methods. For more information on the CF service broker
 * lifecycle and API, please see See <a href="https://docs.cloudfoundry.org/services/api.html">here.</a>
 * <p>
 * This class extends DefaultServiceImpl, which has no-op implementations of the methods. This means
 * that if, for instance, your broker does not support binding you can just delete the binding methods below
 * (in other words, you do not need to implement your own no-op implementations).
 */
@Service
@Slf4j
public class HelloBroker extends DefaultServiceImpl {

    private static final String USER_NAME_KEY = "user";
    private static final String PASSWORD_KEY = "password";
    private static final String ROLE_KEY = "role";

    public HelloBroker(HelloBrokerRepository helloRepository, Environment env) {
        super();
        this.helloRepository = helloRepository;
        this.env = env;
    }

    private Environment env;

    private HelloBrokerRepository helloRepository;

    /**
     * Add code here and it will be run during the create-service process. This might include
     * calling back to your underlying service to create users, schemas, fire up environments, etc.
     *
     * @param instance service instance data passed in by the cloud connector. Clients can pass additional json
     *                 as part of the create-service request, which will show up as key value pairs in instance.parameters.
     */
    @Override
    public LastOperation createInstance(ServiceInstance instance) {
        log.info("provisioning broker user: " + instance.getId());

        try {
            User user = helloRepository.provisionUser(new User(instance.getId(), null, User.Role.Broker));
            instance.addParameter(USER_NAME_KEY, user.getName());
            instance.addParameter(ROLE_KEY, user.getRole().toString());
            instance.addParameter(PASSWORD_KEY, user.getPassword().toString());
            String msg = "broker user: " + user.getName() + " created.";
            log.info(msg);
            return new LastOperation(Operation.CREATE, OperationState.SUCCEEDED, msg);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            System.out.println(t);
            return new LastOperation(Operation.CREATE, OperationState.FAILED, t.getMessage());
        }
    }

    /**
     * Code here will be called during the delete-service instance process. You can use this to de-allocate resources
     * on your underlying service, delete user accounts, destroy environments, etc.
     *
     * @param instance service instance data passed in by the cloud connector.
     */
    @Override
    public LastOperation deleteInstance(ServiceInstance instance) {
        log.info("deprovisioning broker user: " + instance.getId());

        try {
            String name = instance.getParameter(USER_NAME_KEY).toString();
            helloRepository.deprovisionUser(name);
            String msg = "broker user: " + name + " deleted.";
            log.info(msg);
            return new LastOperation(Operation.DELETE, OperationState.SUCCEEDED, msg);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return new LastOperation(Operation.DELETE, OperationState.FAILED, t.getMessage());
        }
    }

    /**
     * Code here will be called during the update-service process. You can use this to modify
     * your service instance.
     *
     * @param instance service instance data passed in by the cloud connector.
     */
    @Override
    public LastOperation updateInstance(ServiceInstance instance) {
        log.info("updating broker user: " + instance.getId());

        try {
            User user = new User();
            user.setName(instance.getParameter(USER_NAME_KEY).toString());
            user.setPassword(instance.getParameter(PASSWORD_KEY).toString());
            user.setRole(User.Role.valueOf(instance.getParameter(ROLE_KEY).toString()));
            user = helloRepository.updateUser(user);
            String msg = "broker user: " + user.getName() + " updated.";
            log.info(msg);
            return new LastOperation(Operation.UPDATE, OperationState.SUCCEEDED, msg);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return new LastOperation(Operation.UPDATE, OperationState.FAILED, t.getMessage());
        }
    }

    /**
     * Called during the bind-service process. This is a good time to set up anything on your underlying service specifically
     * needed by an application, such as user accounts, rights and permissions, application-specific environments and connections, etc.
     * <p>
     * Services that do not support binding should set '"bindable": false,' within their catalog.json file. In this case this method
     * can be safely deleted in your implementation.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @param binding  binding data passed in by the cloud connector. Clients can pass additional json
     *                 as part of the bind-service request, which will show up as key value pairs in binding.parameters. Brokers
     *                 can, as part of this method, store any information needed for credentials and unbinding operations as key/value
     *                 pairs in binding.properties
     */
    @Override
    public LastOperation createBinding(ServiceInstance instance, ServiceBinding binding) {
        log.info("provisioning user: " + binding.getId());

        try {
            User user = helloRepository.provisionUser(new User(binding.getId(), null, User.Role.User));
            binding.getParameters().put(USER_NAME_KEY, user.getName());
            binding.getParameters().put(ROLE_KEY, user.getRole());
            binding.getParameters().put(PASSWORD_KEY, user.getPassword());
            String msg = "user: " + user.getName() + " created.";
            log.info(msg);
            return new LastOperation(Operation.BIND, OperationState.SUCCEEDED, msg);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return new LastOperation(Operation.BIND, OperationState.FAILED, t.getMessage());
        }
    }

    /**
     * Called during the unbind-service process. This is a good time to destroy any resources, users, connections set up during the bind process.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @param binding  binding data passed in by the cloud connector.
     */
    @Override
    public LastOperation deleteBinding(ServiceInstance instance, ServiceBinding binding) {
        log.info("deprovisioning user: " + binding.getId());

        try {
            String name = binding.getParameter(USER_NAME_KEY).toString();
            helloRepository.deprovisionUser(name);
            String msg = "user: " + name + " deleted.";
            log.info(msg);
            return new LastOperation(Operation.UNBIND, OperationState.SUCCEEDED, msg);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return new LastOperation(Operation.UNBIND, OperationState.FAILED, t.getMessage());
        }
    }

    /**
     * Bind credentials that will be returned as the result of a create-binding process. The format and values of these credentials will
     * depend on the nature of the underlying service. For more information and some examples, see
     * <a href=https://docs.cloudfoundry.org/services/binding-credentials.html>here.</a>
     * <p>
     * This method is called after the create-binding method: any information stored in binding.properties in the createBinding call
     * will be availble here, along with any custom data passed in as json parameters as part of the create-binding process by the client.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @param binding  binding data passed in by the cloud connector.
     * @return credentials, as a series of key/value pairs
     * @throws ServiceBrokerException thrown this for any errors during credential creation.
     */
    @Override
    public Map<String, Object> getCredentials(ServiceInstance instance, ServiceBinding binding) throws
            ServiceBrokerException {
        log.info("returning creds.");

        try {
            Map<String, Object> m = new HashMap<>();
            m.put("hostname", env.getProperty("HELLO_HOST"));
            m.put("port", env.getProperty("HELLO_PORT"));
            m.put("username", binding.getParameter(USER_NAME_KEY));
            m.put("password", binding.getParameter(PASSWORD_KEY));

            String uri = "hello://" + m.get("username") + ":" + m.get("password") + "@" + m.get("hostname") + ":" + m.get("port");
            m.put("uri", uri);

            return m;
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            throw new ServiceBrokerException(t);
        }
    }

    @Override
    public boolean isAsync() {
        return false;
    }
}