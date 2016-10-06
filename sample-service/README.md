#sample-service
This is a sample service that acts as "the thing we are creating a broker for." Since the focus is on building a broker and not the complexity of the underlying service, the service is purposefully simplistic.

At a high level, the service allows for provisioning of user accounts:
 * "broker" accounts for use by the broker to manage service instances
 * "user" accounts that are tied to bound instances 
 
Once these accounts are provisioned, "broker" users can create, update and delete service instances, and "user" users can get a nice greeting from the backend service.

TODO, basic auth to control access to endpoints.

##Using sample-service
1. Git checkout (if you have not already checked out the parent project):
  
  ```bash
  git clone git@github.com:cf-platform-eng/simple-service-broker.git
  cd simple-service-broker
  ```
2. Log into cf and target an org/space where you have space developer privileges.
1. Build the app:
  
  ```bash
  cd sample-service
  mvn clean install
  ```
4. Push the app to cf:
  
  ```bash
  cf push
  ```