# hello-broker
A simple spring boot cf service broker.
##Using the sample

1. Git checkout:
  
  ```bash
  git clone git@github.com:cf-platform-eng/simple-service-broker.git
  cd simple-service-broker
  ```
1. Log into cf and target an org/space where you have space developer privileges.
1. Create the redis datastore for the broker:
  
  ```bash
  cf create-service p-redis shared-vm hello-ds
  ```
4. Edit the broker password in application.properties (you probably don't want to check this in!)
1. Edit the manifest.yml file to give your broker an appropriate name.
1. Build the app:
  
  ```bash
  mvn clean install
  ```
7. Push the app to cf:
  
  ```bash
  cf push
  ```
8. Register the broker:
  
  ```bash
  cf create-service-broker <your broker name> user <the password from application.properties> https://<uri of your broker app>
  ```
9. See your new broker:
  
  ```bash
  cf service-brokers
  Getting service brokers as admin...
  
  name                          url
  ...
  hello-broker                  https://your-broker-url
  ...
  
  cf service-access
  Getting service access as admin...
  ...
  broker: hello-broker
     service   plan   access   orgs
     hello     hi     none
  ...
  
  cf enable-service-access hello
  Enabling access to all plans of service hello for all orgs as admin...

  cf marketplace
  Getting services from marketplace in org your-org / space your-space as you...
  OK
  
  service          plans                                 description
  hello            hi                                    Friendly service that greets you
  ...
  ```
10. Create an instance:
  
  ```bash
  cf create-service hello hi hello-service
  ```
11. Look at the broker logs:
  
  ```bash
  cf logs hello-broker --recent
  ...
  2016-09-22T10:10:11.53-0400 [APP/0]      OUT 2016-09-22 14:10:11 [http-nio-8080-exec-10] INFO  i.p.c.s.hello.HelloService - hello!, I am creating a service instance!
  ...
  ```
12. Bind an app to the service and check the logs again:
  
  ```bash
  cf bind-service someOtherApp hello-service
  ...
  2016-09-22T10:42:42.81-0400 [APP/0]      OUT 2016-09-22 14:42:42 [http-nio-8080-exec-10] INFO  i.p.c.s.hello.HelloService - hello!, I am creating a binding!
  2016-09-22T10:42:42.81-0400 [APP/0]      OUT 2016-09-22 14:42:42 [http-nio-8080-exec-10] INFO  i.p.c.s.hello.HelloService - hello!, I am returning credentials!
  ...
  ```
13. Restage the service you bound to and look at its env: you should see some hello credentials in there:
  
  ```bash
  cf restage someOtherApp
  cf env someOtherApp
  ...
  ```
  ```json
   {
    "hello": [
     {
      "credentials": {
       "database": "helloDB",
       "host": "helloHost",
       "password": "world",
       "port": "helloPort",
       "uri": "http://hello:world@helloHost:helloPort/helloDB",
       "username": "hello"
      },
      "label": "hello",
      "name": "hello-service",
      "plan": "hi",
      "provider": null,
      "syslog_drain_url": null,
      "tags": [
       "hello"
      ],
      "volume_mounts": []
     }
    ]
   }
  ```
14. Unbind your app from the service and look at the logs:
  
  ```bash
  cf unbind-service someOtherApp hello-service
  ...
  2016-09-22T10:48:39.32-0400 [APP/0]      OUT 2016-09-22 14:48:39 [http-nio-8080-exec-3] INFO  i.p.c.s.hello.HelloService - hello!, I am deleting a binding!
  ...
  ```
15. Delete the service and look at the logs:
  
  ```bash
  cf delete-service hello-service
  ...
  2016-09-22T10:50:28.76-0400 [APP/0]      OUT 2016-09-22 14:50:28 [http-nio-8080-exec-8] INFO  i.p.c.s.hello.HelloService - hello!, I am deleting a service instance!
  ...
  ```
16. Unregister and delete the broker:
  
  ```bash
  cf delete-service-broker hello-broker
  cf delete hello-broker
  ```

##Creating your own broker

1. Fork this repo (so you can save your changes into your own repository).
1. Edit the pom.xml, application.properties and manifest.yml files as needed.
1. Edit the application.properties and manifest.yml files as needed.
1. Edit the sample catalog.yml file so that it describes your service and plans. Information on what goes into a catalog file can be found [here.](https://docs.cloudfoundry.org/services/api.html)
1. Rename the Hello class file to something appropriate for your service.
1. Add your service implementation to the example methods in the Hello class. Documentation on what the methods do and how to use them is in the Hello.java [source file.](https://github.com/cf-platform-eng/simple-service-broker/blob/master/src/main/java/io/pivotal/cf/servicebroker/broker/HelloService.java)
1. Create some unit tests (or modify the existing tests) to test your broker.
1. Follow the steps outlined above to build, deploy and verify your broker.

##Creating a tile for your broker

1. See the instructions here for checking out and using the tile-generator tool [here.](https://github.com/cf-platform-eng/tile-generator)
1. A "starter" tile.yml file is already included for the hello broker. Edit this file to configure the tile generator as needed for your broker.
