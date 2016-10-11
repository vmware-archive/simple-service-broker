package io.pivotal.cf.servicebroker.service;

import io.pivotal.cf.servicebroker.model.ServiceBinding;
import io.pivotal.cf.servicebroker.model.ServiceInstance;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;

import java.util.Map;

/**
 * Covers methods across the service broker lifecycle, as described <a href="https://docs.cloudfoundry.org/services/api.html">here.</a>
 * Classes implementing this interface will receive call-backs from the simple-broker library at the appropriate lifecycle events.
 */
public interface BrokeredService {

    /**
     * Add code here and it will be run during the create-service process. This might include
     * calling back to your underlying service to create users, schemas, fire up environments, etc.
     *
     * @param instance service instance data passed in by the cloud connector. Clients can pass additional json
     *                 as part of the create-service request, which will show up as key value pairs in instance.parameters.
     * @throws ServiceBrokerException thrown this for any errors during instance creation.
     */
    void createInstance(ServiceInstance instance) throws ServiceBrokerException;

    /**
     * Code here will be called during the delete-service instance process. You can use this to de-allocate resources
     * on your underlying service, delete user accounts, destroy environments, etc.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @throws ServiceBrokerException thrown this for any errors during instance deletion.
     */
    void deleteInstance(ServiceInstance instance) throws ServiceBrokerException;

    /**
     * Code here will be called during the update-service process. You can use this to modify
     * your service instance.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @throws ServiceBrokerException thrown this for any errors during instance deletion. Services that do not support
     *                                updating can through ServiceInstanceUpdateNotSupportedException here.
     */
    void updateInstance(ServiceInstance instance) throws ServiceBrokerException;

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
    void createBinding(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException;

    /**
     * Called during the unbind-service process. This is a good time to destroy any resources, users, connections set up during the bind process.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @param binding  binding data passed in by the cloud connector.
     * @throws ServiceBrokerException thrown this for any errors during the unbinding creation.
     */
    void deleteBinding(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException;

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
    Map<String, Object> getCredentials(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException;

    /**
     * @return true if this broker supports asynchronous operations
     */
    boolean isAsync();
}