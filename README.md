#simple-service-broker
simple-service-broker was created based on feedback from Pivotal partners who wanted to focus less on "the plumbing," and more on functionality that directly supports their products' unique capabilities.

The project is made up of several modules, each with individual READMEs that explain in more detail their use:

##simple-broker
This is where the main library classes exist. The rest of the modules are examples that show how to use these classes to create a service broker.

##sample-broker
An example broker that uses the simple-broker for demonstration purposes. This module could be used as a starting point for your custom broker.

##sample-service
An very simple demo back-end for the broker to talk to.

##sample-connector
An example spring cloud service connector that vastly simplifies the use of a brokered service by spring cloud client applications. This is an optional feature that service broker providers can create for users of their services.
 
##sample-client
An example spring boot app that makes use of the sample-broker provided service via the sample-connector.

Together, the sample projects can be used as a template for a fully functional demo of a custom service broker. Heck, why not also re-purpose the READMEs to help document your broker?


##[todo] Create a tile for your broker

1. See the instructions here for checking out and using the tile-generator tool [here.](https://github.com/cf-platform-eng/tile-generator)
1. A "starter" tile.yml file is already included for the sample broker. Edit this file to configure the tile generator as needed for your broker.
1. test this, document it.....

##[todo] Create a pipeline for your broker

1. create example pipeline, document it here....