package io.pivotal.cf.servicebroker;

import io.pivotal.cf.servicebroker.model.ServiceBinding;
import io.pivotal.cf.servicebroker.model.ServiceInstance;
import io.pivotal.cf.servicebroker.service.DefaultServiceImpl;
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
        User user = helloRepository.provisionUser(new User(instance.getId(), null, User.Role.Broker));
        instance.getParameters().put("user", user);

        log.info("broker user: " + user.getName() + " created.");
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

        User user = (User) instance.getParameters().get("user");
        helloRepository.deprovisionUser(user.getName());
        instance.getParameters().remove("user");

        log.info("broker user: " + user.getName() + " deleted.");
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
        User user = (User) instance.getParameters().get("user");
        user = helloRepository.updateUser(user.getName(), user);
        instance.getParameters().put("user", user);

        log.info("broker user: " + user.getName() + " updated.");
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
        User user = helloRepository.provisionUser(new User(binding.getId(), null, User.Role.User));
        instance.getParameters().put("user", user);

        log.info("user: " + user.getName() + " created.");
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

        User user = (User) binding.getParameters().get("user");
        helloRepository.deprovisionUser(user.getName());
        binding.getParameters().remove("user");

        log.info("user: " + user.getName() + " deleted.");
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
    public Map<String, Object> getCredentials(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException {

        User user = (User) binding.getParameters().get("user");

        Map<String, Object> m = new HashMap<>();
        m.put("hostname", env.getProperty("hostname"));
        m.put("port", env.getProperty("port"));
        m.put("username", user.getName());
        m.put("password", user.getPassword());

        String uri = "hello://" + m.get("username") + ":" + m.get("password") + "@" + m.get("hostname") + ":" + m.get("port");
        m.put("uri", uri);

        return m;
    }

    @Override
    public boolean isAsync() {
        //TODO deal with async
        return false;
    }
}