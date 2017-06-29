# simple-broker
Simple Broker is a set of classes that make it easier to create spring cloud service brokers. It builds upon the [spring-cloud-cloudfoundry-service-broker](https://github.com/spring-cloud/spring-cloud-cloudfoundry-service-broker) project by providing concrete model implementations for persistent instance and binding domain classes, and by marshalling required request and response objects.

To create you own broker, follow these steps:

1. git clone the parent project (alternatively, you can fork this repository and modify it as needed).
1. Build and install. This will load the simple-broker jar into your local maven repository so you can make use of it as a library for your broker.
```bash
git clone git@github.com:cf-platform-eng/simple-service-broker.git
cd simple-service-broker
mvn install
  ```
To implement you broker you will then:

3. Provide a concrete implementation of the [BrokeredService](src/main/java/io/pivotal/ecosystem/servicebroker/service/BrokeredService.java) interface. You can either implement the interface directly, or extend the [DefaultServiceImpl](src/main/java/io/pivotal/ecosystem/servicebroker/service/DefaultServiceImpl.java) class (in which case you only need to override the methods you care about). The methods in the BrokeredService interface represent call-backs that will be invoked during the appropriate service broker [lifecycle events.](https://docs.cloudfoundry.org/services/api.html)
1. Provide a security password for your broker via environment variables, as in [this example.](../sample-broker/manifest.yml) You probably do not want to check in this file!
1. Create a catalog.json file that describes your service and its plans. The format and content of this file is documented [here](https://docs.cloudfoundry.org/services/catalog-metadata.html) and an example can be found [here.](src/test/resources/application.properties)
1. Provide the rest of the spring "scaffolding" for your broker, such as components to connect to your back-end service, plus  spring boot Application and Configuration files.

The [sample-broker](../sample-broker) project can be used as a template to help get you started.

Detailed instructions on how to deploy and test your broker can be found in the [README](../sample-broker/README.md) for the sample-broker module.
