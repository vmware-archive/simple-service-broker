#sample-client
This is an example spring boot sample application that makes use of the sample-connector to connect to the sample-service via the sample-broker, amply. 

##Using sample-client
1. Git checkout and build the modules (if you have not already done so):

  ```bash
  git clone git@github.com:cf-platform-eng/simple-service-broker.git
  cd simple-service-broker
  mvn clean install
  ```
2. To use the client, you will need to deploy the back-end ([sample-service](https://github.com/cf-platform-eng/simple-service-broker/tree/master/sample-service)) and configure and deploy the broker ([sample-broker](https://github.com/cf-platform-eng/simple-service-broker/tree/master/sample-broker)). Instruction on how to do this are in the respective README files.
1. Then, deploy sample-client. There is nothing to configure, other than maybe changing the name of the application in the manifest file.

  ```bash
  cd sample-client
  cf push
  ```
4. to see the client in action, first get its location via cf:

  ```bash
  cf a
  Getting apps in org your-org / space your-space as admin...
  OK
  
  name                 requested state   instances   memory   disk   urls
  hello-broker         started           1/1         512M     1G     hello-broker.your.domain.io
  hello-client         started           1/1         256M     1G     hello-client.your.domain.io
  hello-service        started           1/1         256M     1G     hello-service.your.domain.io
  ```

  According to the above, our uri is hello-client.your.domain.io, for instance.

5. Open a browser and enter the following url:

  ```
  http://hello-client.your.domain.io/greeting?username=foo
  ```
  
  The app will respond with with a 401 and a polite message.
  
6. To get the valid user name for this client, use the following cf command to access the app's credentials:

  ```bash
  cf env hello-client
  Getting env variables for app hello-client in org simple-service-broker-org / space simple-service-broker-space as admin...
  OK
  {
   "VCAP_SERVICES": {
    "hello": [
     {
      "credentials": {
       "hostname": "hello-service.your.domain.io",
       "password": "36ca2497-ec49-490b-8c6e-7d9f6ecfc711",
       "port": "80",
       "uri": "hello://83c52f6c-16a3-4e67-b19f-f6f4e6e82c08:36ca2497-ec49-490b-8c6e-7d9f6ecfc711@hello-service.your.domain.io:80",
       "username": "83c52f6c-16a3-4e67-b19f-f6f4e6e82c08"
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
  According to the above, the username used to bind to hello-service is 83c52f6c-16a3-4e67-b19f-f6f4e6e82c08
  
7. replace the username in the url with the one you pulled from cf and resubmit. For example:
  
   ```
    http://hello-client.your.domain.io/greeting?username=83c52f6c-16a3-4e67-b19f-f6f4e6e82c08
    ```
  
##What Just Happened?
Looking at the project [source directory](https://github.com/cf-platform-eng/simple-service-broker/tree/master/sample-client/src/main/java/io/pivotal/cf/service/client), there is no obvious configuration in sample-client. Because it uses [sample-connector](https://github.com/cf-platform-eng/simple-service-broker/tree/master/sample-connector), all we needed to do to get it to connect to sample-service is:

1. Add hello-service as a dependency in the manifest file.
1. Add the @ServiceScan annotation to the [Application](https://github.com/cf-platform-eng/simple-service-broker/blob/master/sample-client/src/main/java/io/pivotal/cf/service/client/Application.java) class.
1. Add the @RestController annotation to [HelloClientController](https://github.com/cf-platform-eng/simple-service-broker/blob/master/sample-client/src/main/java/io/pivotal/cf/service/client/HelloClientController.java) and include [HelloRepository](https://github.com/cf-platform-eng/simple-service-broker/blob/master/sample-connector/src/main/java/io/pivotal/cf/service/connector/HelloRepository.java) as a constructor param.
  
##What we did *not* need to do:
We did not need to:
  * tell sample-client what the hello-service was
  * or where to find it
  * or what credentials to use
  * or parse through json or directly process environment variables
  * or anything other than include the sample-client jar in our [project dependencies](https://github.com/cf-platform-eng/simple-service-broker/blob/master/sample-client/pom.xml#L26-L30)

Consider providing a connector to your broker: your clients will thank you.
