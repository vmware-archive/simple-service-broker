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

package io.pivotal.ecosystem.servicebroker;

import io.pivotal.ecosystem.servicebroker.model.ServiceBinding;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import io.pivotal.ecosystem.servicebroker.service.DefaultServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
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
     * @throws ServiceBrokerException thrown this for any errors during instance creation.
     */
    @Override
    public void createInstance(ServiceInstance instance) throws ServiceBrokerException {

        //TODO use admin creds to talk to service
        log.info("provisioning broker user: " + instance.getId());

        try {
            User user = helloRepository.provisionUser(new User(instance.getId(), User.Role.Broker));
            instance.addParameter("user", user);
            log.info("broker user: " + user.getName() + " created.");
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            throw new ServiceBrokerException(t);
        }
    }

    /**
     * Code here will be called during the delete-service instance process. You can use this to de-allocate resources
     * on your underlying service, delete user accounts, destroy environments, etc.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @throws ServiceBrokerException thrown this for any errors during instance deletion.
     */
    @Override
    public void deleteInstance(ServiceInstance instance) throws ServiceBrokerException {
        //TODO use admin creds to talk to service
        log.info("deprovisioning broker user: " + instance.getId());

        try {
            User user = (User) instance.getParameter("user");
            helloRepository.deprovisionUser(user.getName());
            instance.getParameters().remove("user");

            log.info("broker user: " + user.getName() + " deleted.");
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            throw new ServiceBrokerException(t);
        }
    }

    /**
     * Code here will be called during the update-service process. You can use this to modify
     * your service instance.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @throws ServiceBrokerException thrown this for any errors during instance deletion. Services that do not support
     *                                updating can through ServiceInstanceUpdateNotSupportedException here.
     */
    @Override
    public void updateInstance(ServiceInstance instance) throws ServiceBrokerException {
        //TODO change user/pw for this instance, use admin creds to talk to service
        log.info("updating broker user: " + instance.getId());

        try {
            User user = (User) instance.getParameter("user");
            user = helloRepository.updateUser(user.getName(), user);
            instance.getParameters().put("user", user);

            log.info("broker user: " + user.getName() + " updated.");
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            throw new ServiceBrokerException(t);
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
     * @throws ServiceBrokerException thrown this for any errors during binding creation.
     */
    @Override
    public void createBinding(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException {
        //TODO use admin creds to talk to service
        log.info("provisioning user: " + binding.getId());

        try {
            User user = helloRepository.provisionUser(new User(binding.getId(), User.Role.User));
            binding.getParameters().put("user", user);

            log.info("user: " + user.getName() + " created.");
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            throw new ServiceBrokerException(t);
        }
    }

    /**
     * Called during the unbind-service process. This is a good time to destroy any resources, users, connections set up during the bind process.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @param binding  binding data passed in by the cloud connector.
     * @throws ServiceBrokerException thrown this for any errors during the unbinding creation.
     */
    @Override
    public void deleteBinding(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException {
        //TODO use admin creds to talk to service
        log.info("deprovisioning user: " + binding.getId());

        try {
            User user = (User) binding.getParameter("user");
            helloRepository.deprovisionUser(user.getName());
            binding.getParameters().remove("user");

            log.info("user: " + user.getName() + " deleted.");
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            throw new ServiceBrokerException(t);
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
            User user = (User) binding.getParameters().get("user");

            Map<String, Object> m = new HashMap<>();
            m.put("hostname", env.getProperty("HELLO_HOST"));
            m.put("port", env.getProperty("HELLO_PORT"));
            m.put("username", user.getName());
            m.put("password", user.getPassword());

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
        //TODO deal with async
        return false;
    }
}