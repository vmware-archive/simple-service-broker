# simple-service-broker

------------------------------------------------------------------------
**NOTE**

Outdated superceeded by newer tooling such as [Spring App Broker](https://spring.io/projects/spring-cloud-app-broker) which provides a framework for building service brokers by using the [Spring Cloud Open Service Broker API](https://spring.io/projects/spring-cloud-open-service-broker). 

Some other Java based examples are:
* [Java Servie Broker](https://github.com/cf-platform-eng/pcf-examples/tree/master/java-service-broker)
* [Bookstore Service Broker](https://github.com/spring-cloud-samples/bookstore-service-broker)
* [Minimal Java Service Broker](https://github.com/cf-platform-eng/pcf-examples/tree/master/java-service-broker)
------------------------------------------------------------------------

simple-service-broker was created based on feedback from Pivotal partners who wanted to focus less on "the plumbing," and more on functionality in direct support of their products' unique capabilities.

The project is made up of several modules, each with individual READMEs that explain things in more detail:

## [simple-broker](https://github.com/cf-platform-eng/simple-service-broker/tree/master/simple-broker)
This is where the main library classes exist. The rest of the modules are examples that show how to use these classes to create a service broker.

## [sample-broker](https://github.com/cf-platform-eng/simple-service-broker/tree/master/sample-broker)
An example broker that uses the simple-broker for demonstration purposes. This module could be used as a starting point for your custom broker.

## [sample-service](https://github.com/cf-platform-eng/simple-service-broker/tree/master/sample-service)
An very simple demo back-end for the broker to talk to.

## [sample-connector](https://github.com/cf-platform-eng/simple-service-broker/tree/master/sample-connector)
An example spring cloud service connector that vastly simplifies the use of a brokered service. This is an optional feature that service broker providers can create for users of their services.
 
## [sample-client](https://github.com/cf-platform-eng/simple-service-broker/tree/master/sample-client)
An example spring boot app that uses the sample-broker provided service, via the sample-connector.

## The Demo
Together, the sample projects can be used to demo the sample-broker, and as a template for a fully functional demo of a custom service broker. Heck, why not also re-purpose the READMEs to help document your broker?

To deploy and run the samples as a group, follow this order (details in the individual project READMEs):

1. git clone the simple-service-broker project
1. mvn install the simple-service-broker
1. cf push the sample-service
1. cf push, register, and enable the sample-broker
1. cf push the sample-client

## Create a tile for your broker
For instructions on how to create an [Ops Manager tile](https://docs.pivotal.io/partners/deploying-with-ops-man-tile.html) for your broker in the sample-broker [README](https://github.com/cf-platform-eng/simple-service-broker/blob/master/sample-broker/README.md).
