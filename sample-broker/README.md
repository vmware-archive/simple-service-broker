#sample-broker
An example cloud foundry service broker that uses the simple-service-broker approach to connect to an example back-end service.

##Prerequisites
The [sample-service](https://github.com/cf-platform-eng/simple-service-broker/tree/master/sample-service) module contains the back-end service that this broker will be connecting to. Please see the [README](https://github.com/cf-platform-eng/simple-service-broker/blob/master/sample-broker/README.md) for sample-service and follow the directions there, prior to deploying the sample-broker.
  
##Using sample-broker
1. The sample-broker requires a redis datastore. To set this up:
  
  ```bash
  cf create-service p-redis shared-vm hello-ds
  ```
2. The broker makes use of spring-security to protect itself against unauthorized meddling. To set its username and password edit the [manifest.yml](https://github.com/cf-platform-eng/simple-service-broker/blob/master/sample-broker/manifest.yml) file as needed for your CF install (you probably don't want to check this in!).
1. Build the broker:
  
  ```bash
  cd sample-broker
  mvn clean install
  ```
7. Push the broker to cf:
  
  ```bash
  cf push
  ```
8. Register the broker:
  
  ```bash
  cf create-service-broker your_broker_name the_user_from_the_manifest the_password_from_the_manifest https://uri.of.your.broker.app
  ```
9. See the broker:
  
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
  2016-10-07T10:30:27.16-0400 [APP/0]      OUT 2016-10-07 14:30:27 [http-nio-8080-exec-7] INFO  i.p.c.s.service.InstanceService - creating service instance: 727b9a....
  ...
  ```
12. Bind an app to the service and check the logs again:
  
  ```bash
  cf bind-service someOtherApp hello-service
  ...
  2016-10-07T10:30:42.61-0400 [APP/0]      OUT 2016-10-07 14:30:42 [http-nio-8080-exec-8] INFO  i.p.c.s.service.BindingService - creating binding for service instance: 727b9a....
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
    "VCAP_SERVICES": {
     "hello": [
      {
       "credentials": {
        "hostname": "hello-service.cfapps-04.haas-26.pez.pivotal.io",
        "password": "8a7c8bc4-16b5-4f75-8b4f-7d26a4ae95bb",
        "port": "80",
        "uri": "hello://63810f09-c0a6-4a93-9a38-577cc5dccd0d:8a7c8bc4-16b5-4f75-8b4f-7d26a4ae95bb@hello-service.cfapps-04.haas-26.pez.pivotal.io:80",
        "username": "63810f09-c0a6-4a93-9a38-577cc5dccd0d"
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
   }
  ```
14. Unbind your app from the service and look at the logs:
  
  ```bash
  cf unbind-service someOtherApp hello-service
  ...
  2016-10-07T10:38:23.23-0400 [APP/0]      OUT 2016-10-07 14:38:23 [http-nio-8080-exec-5] INFO  i.p.cf.servicebroker.HelloBroker - deprovisioning user: 63810f0...
  ...
  ```
15. Delete the service and look at the logs:
  
  ```bash
  cf delete-service hello-service
  ...
  2016-10-07T10:39:22.43-0400 [APP/0]      OUT 2016-10-07 14:39:22 [http-nio-8080-exec-7] INFO  i.p.c.s.service.InstanceService - starting service instance delete: 727b9a...
  ...
  ```
16. Unregister and delete the broker:
  
  ```bash
  cf delete-service-broker hello-broker
  cf delete hello-broker
  ```
 
##Creating an Ops Manager Tile
Ops Manager Tiles are a way of packaging up your release so that it can be [easily configured and deployed](https://docs.pivotal.io/partners/deploying-with-ops-man-tile.html) via the Ops Manager UI.

1. To make tile creation itself an easy process, we have the tile-gen tool. See the instructions [here.](https://github.com/cf-platform-eng/tile-generator)
1. A "starter" tile.yml file is included for the sample-broker. It can be used to generate a working tile for the sample-broker, or adapted for use for your tile. Instructions on what to put in this tile are on the tile-gen site, above.
1. Before installing your tile, make sure to cf push a sample-service instance: this represents the back-end service the tile will talk to. Instructions on how to do this are [here.](https://github.com/cf-platform-eng/simple-service-broker/blob/master/sample-service/README.md)
1. You will want to unregister and uninstall any other version of the sample-broker running on the cf instance before installing the tile (if, for instance, you followed the instructions on how to manually deploy and register your sample-broker). Instructions for manually removing the broker start at item 14, above.
1. After the tile-gen tool have been installed (see the tile-gen documentation site), edit tile.yml as needed, and run "tile build" to create a tile.
1. Once your tile has been generated, use Ops Manager to upload, install, and configure it. Configuration is done on the "Settings" tab (click on the tile to access this). API version, sample-service host, and port should be set, based on the uri of the sample-service you deployed.
1. When you are returned to the Ops Manager page, hit "Apply Changes" and your broker will be deployed.