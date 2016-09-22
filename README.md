# hello-broker

Simple spring boot servicebroker.

See the Hello class: your code goes here.

1. git checkout
1. mvn build
1. edit manifest
1. edit password in application.properties (don't check in!)

pws example (https://docs.run.pivotal.io/services/managing-service-brokers.html)
1. cf push
1. register the broker: cf create-service-broker hello-broker user changeme https://hello-broker.cfapps.io --space-scoped
1. create an instance: cf create-service hello hi hello-service
1. look at the logs: cf logs hello-broker --recent

should see something like: 2016-09-22T10:10:11.53-0400 [APP/0]      OUT 2016-09-22 14:10:11 [http-nio-8080-exec-10] INFO  i.p.c.s.hello.HelloService - hello!, I am creating a service instance!

1. bind an app to the service: cf bind-service sql-quote-service hello-service