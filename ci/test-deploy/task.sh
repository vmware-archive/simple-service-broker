#!/usr/bin/env bash

####################################### GLOBAL VARS ###########################################

## Parameters
TILE_GEN_DIR="$( cd "$1" && pwd )"
POOL_DIR="$( cd "$2" && pwd )"
TILE_DIR="$( cd "$3" && pwd )"
SAMPLE_APP_DIR="$( cd "$4" && pwd )"
TEST_APP_DIR="$( cd "$5" && pwd )"
LOG_DIR="$( cd "$6" && pwd )"
TILE_DIR="$( cd "$7" && pwd )"


TILE_FILE=`cd "${TILE_DIR}"; ls *.pivotal`
if [ -z "${TILE_FILE}" ]; then
   echo "No files matching ${TILE_DIR}/*.pivotal"
   ls -lR "${TILE_DIR}"
   exit 1
fi

PRODUCT="solace-messaging"
VERSION=`echo "${TILE_FILE}" | sed "s/${PRODUCT}-//" | sed "s/\.pivotal\$//"`

echo "PRODUCT: $PRODUCT"
echo "VERSION: $VERSION"

## Commands
PCF=${TILE_GEN_DIR}/bin/pcf

SHARED_PLAN="af308299-102f-47a3-acb0-7de72be192bf"
LARGE_PLAN="9bd51219-9cee-4570-99ab-ebe80d82c854"

####################################### FUNCTIONS ###########################################

function log() {
	echo ""
	echo `date` $1
}

function testHeader() {

 log "### Start Testing"

## Just in case we want to skip the tests
# log "### Skipping tests"
# touch $LOG_DIR/test-deploy-report-$VERSION.tgz
# exit 0

}


function lookupServiceBrokerDetails() {
 
 cf target -o solace-systems -s solace-messaging

## Capture a few details from the running service broker
 export SB_APP=`cf apps | grep solace-service-broker | grep started  | awk '{ print $1}'`
 export SB_URL=`cf apps | grep solace-service-broker | grep started  | awk '{ print $6}'`
 export SECURITY_USER_NAME=`cf env $SB_APP | grep SECURITY_USER_NAME | awk '{ print $2}'`
 export SECURITY_USER_PASSWORD=`cf env $SB_APP | grep SECURITY_USER_PASSWORD | awk '{ print $2}'`
 export SB_BASE=$SECURITY_USER_NAME:$SECURITY_USER_PASSWORD@$SB_URL

 export ALL_VMR_LIST=${ALL_VMR_LIST:-`curl -sX GET $SB_BASE/solace/manage/solace_message_routers/links`}
 export SHARED_VMR_LIST=${SHARED_VMR_LIST:-`curl -sX GET $SB_BASE/solace/manage/solace_message_routers/links/$SHARED_PLAN`}
 export LARGE_VMR_LIST=${LARGE_VMR_LIST:-`curl -sX GET $SB_BASE/solace/manage/solace_message_routers/links/$LARGE_PLAN`}

 log "ServiceBroker $SB_APP: http://${SB_URL}"
 log "Servicebroker URL BASE: ${SB_BASE} "
 log "Servicebroker LARGE_VMR_LIST: ${LARGE_VMR_LIST} "
 log "Servicebroker SHARED_VMR_LIST: ${SHARED_VMR_LIST} "
 log "Servicebroker ALL_VMR_LIST: ${ALL_VMR_LIST} "

  ## Remove at some point..
 log "ServiceBroker $SB_APP env: "
 cf env $SB_APP

}

function getServiceBrokerDebug() {

 export SB_DEBUG_STATE=`curl -sX GET $SB_BASE/solace/debug -H "Content-Type: application/json;charset=UTF-8"`
 echo $SB_DEBUG_STATE

}



function checkServiceBrokerRepoStats() {

 log "ServiceBroker: Repo stats "$1
 curl -sX GET $SB_BASE/solace/status/repositories -H "Content-Type: application/json;charset=UTF-8"

 # TODO: Add some parameter driven assertions later

}

function checkServiceBrokerServicePlanStats() {

 # Just output for logging for now
 log "ServiceBroker: service plan stats "$1
 curl -sX GET $SB_BASE/solace/status -H "Content-Type: application/json;charset=UTF-8"
 curl -sX GET $SB_BASE/solace/status/services/solace-messaging/plans/shared -H "Content-Type: application/json;charset=UTF-8"
 curl -sX GET $SB_BASE/solace/status/services/solace-messaging/plans/large -H "Content-Type: application/json;charset=UTF-8"

 # TODO: Add some parameter driven assertions later

}

function addMessageRouterToServiceBroker() {

  URL=$1
  PLAN=$2

  #echo ${URL}
  USER_PART=`echo ${URL} | awk -F\: '{ print $2}'`
  SEMP_USER=${USER_PART//\/\//}
  PASSWORD_HOST_PART=`echo ${URL} | awk -F\: '{ print $3}'`
  SEMP_PASSWORD=`echo ${PASSWORD_HOST_PART} | awk -F\@ '{ print $1}'`
  SEMP_HOST=`echo ${PASSWORD_HOST_PART} | awk -F\@ '{ print $2}'`
  SSH_PORT=`echo ${URL} | awk -F\: '{ print $4}'`
  SEMP_PORT="8080"

  log "ServiceBroker: Adding MessageRouter on PLAN [ $PLAN ] Using SEMP_USER [ $SEMP_USER ] SEMP_PASSWORD [ XXX ] SEMP_HOST [ $SEMP_HOST ] SEMP_PORT [ $SEMP_PORT ]"

  curl -sX POST $SB_BASE/solace/resources/solace_message_routers -d "{ \"sempHost\": \"$SEMP_HOST\",  \"sempPort\": \"$SEMP_PORT\", \"sempUser\": \"$SEMP_USER\", \"sempPassword\": \"$SEMP_PASSWORD\", \"planId\": \"$PLAN\" ,\"sshPort\" : \"$SSH_PORT\" }"  -H "Content-Type: application/json;charset=UTF-8" | grep "{}"

}


function addSharedMessageRoutersToServiceBroker() {

 URL_LIST=` echo $SHARED_VMR_LIST | tr ',' '\n'`

 for URL in $URL_LIST ; do
    addMessageRouterToServiceBroker ${URL} ${SHARED_PLAN}
 done

}

function addLargeMessageRoutersToServiceBroker() {

 URL_LIST=` echo $LARGE_VMR_LIST | tr ',' '\n'`

 for URL in $URL_LIST ; do
    addMessageRouterToServiceBroker ${URL} ${LARGE_PLAN}
 done

}

function resetServiceBroker() {

   checkServiceBrokerRepoStats " repositories before reset"

   log "ServiceBroker: reset repositories"
   curl -sX PUT $SB_BASE/solace/reset/repositories -H "Content-Type: application/json;charset=UTF-8"

   checkServiceBrokerRepoStats " repositories after reset"

}

function lookupSampleApplicationDetails() {

 export APP_URL=`cf apps | grep sample-cloud-app | grep started  | awk '{ print $6}'`
 log "SampleApplication URL: http://"$APP_URL

}


function installTools() {

log "### installTools"

### Add supporting tools (Or pick another image contains the tools )
log "### installTools:wget"
apk add --update wget

log "### installTools:curl"
apk add --update curl

log "### installTools:ca-certificates"
apk add --update ca-certificates

#log "### installTools:tar"
#apk add --update tar

log "### installTools:util-linux"
apk add --update util-linux

log "### installTools:cf-cli"
log "Get cf command & add to path"
wget "https://cli.run.pivotal.io/stable?release=linux64-binary" -O cli.tar.gz
tar -xvzf cli.tar.gz
export PATH=$PATH:`pwd`

log "### installTools:libstdc++"
apk add --update libstdc++

log "### installTools:openjdk"
apk add --update openjdk8
apk add --update openjdk8-jre
apk add --update openjdk8-jre-base
apk add --update openjdk8-jre-lib

ls -la /usr/lib/jvm/java-1.8-openjdk/bin
export JAVA_HOME=/usr/lib/jvm/java-1.8-openjdk
ls -la $JAVA_HOME
export PATH=$JAVA_HOME/bin:$PATH
java -version
javac -version

}

####################################### TEST FUNCTIONS ###########################################

function unbindService() {
	log "Listing of Services, before Unbinding service "$1" " $2
	cf services
        FOUND=`cf services | grep $1 | grep $2 | wc -l`
	log "Found $FOUND, before Unbinding service "$1" " $2
	if [ "$FOUND" -gt "0" ]; then
  		cf unbind-service $1 $2
		log "Deleted binding "$1" "$2
	else
		log "Binding not found "$1" "$2
	fi
}

function waitForServiceDelete() {

## Waits for a service to be deleted
REMAINING=`cf services | grep $1 | wc -l`
MAXWAIT=10
while [ "$REMAINING" -gt "0" ] && [ "$MAXWAIT" -gt "0" ]; do
  log "Waiting for delete of $1 to finish ($REMAINING), timeout counter: $MAXWAIT"
  sleep 10 
  cf services
  REMAINING=`cf services | grep $1 | wc -l`
  let MAXWAIT=MAXWAIT-1
  checkServiceBrokerServicePlanStats " Watching async delete of $1 ($REMAINING), timeout counter: $MAXWAIT"
done

}

function forceDeleteService() {
	log "Listing of services before Deleting service "$1
	cf services
        FOUND=`cf services | grep $1 | wc -l`
	log "Found $FOUND, before Deleting service "$1
	if [ "$FOUND" -gt "0" ]; then
	 	cf delete-service $1 -f
		log "Deleted service "$1
		waitForServiceDelete $1
	else
		log "Service not found "$1
	fi
}

function forceDeleteServiceNoWait() {
	log "Listing of services before Deleting service "$1
	cf services
        FOUND=`cf services | grep $1 | wc -l`
	log "Found $FOUND, before Deleting service "$1
	if [ "$FOUND" -gt "0" ]; then
	 	cf delete-service $1 -f
		log "Deleted service "$1
	else
		log "Service not found "$1
	fi
}


function testTileInstallationAndLogin() {

 # Rely on `is-available` non-zero exit code when not available
 log "Check that tile is available"
 $PCF is-available solace-messaging

 log "cf login"
 echo "solace-systems" | $PCF target

}

function switchToTestOrgAndSpace() {

 # Create (will proceed event if it exists)
 log "Will create and target org: solace-test"
 cf create-org solace-test
 cf target -o solace-test 

 log "Will create and target space: test"
 cf create-space test
 cf target -o solace-test -s test

}

function testMarketPlace() {

 ## Checking Marketplace
 log "Enabling access to the Solace Service Broker provided service: solace-messaging"
 cf enable-service-access solace-messaging

 log "Marketplace:"
 cf m

 #todo: Need to install tile for this check to work (enable_global_access_to_plans)
 # Rely on grep's non-0 exit code to fail script

 log "Checking marketplace for solace service: solace-messaging"
 cf m | grep solace-messaging
 log "Checking marketplace for solace service plan: shared"
 cf m | grep shared
 log "Checking marketplace for solace service plan: large"
 cf m | grep large

 log "Checking marketplace for solace service plan: shared, free"
 cf m -s solace-messaging | grep shared | grep free
 log "Checking marketplace for solace service plan: large, free"
 cf m -s solace-messaging | grep large | grep free

}

function useAndDeleteAllPlanServices() {
	useAllPlanServices  $1 $2 $3
	useAllPlanCredentials  $1 $2 $3 $4

	## Do a restart test, confirm before and after state 
	## of captured configuration (Debug) is the same.
	getServiceBrokerDebug > $LOG_DIR/beforeRestart
	log "Captured Service Broker DbConfig beforeRestart"
	ls -la $LOG_DIR/beforeRestart

	restartServiceBroker
 	switchToTestOrgAndSpace

	getServiceBrokerDebug > $LOG_DIR/afterRestart
	log "Captured Service Broker DbConfig afterRestart"
	ls -la $LOG_DIR/afterRestart

	diff $LOG_DIR/beforeRestart $LOG_DIR/afterRestart
	log "Compared Service Broker DbConfig beforeRestart and afterRestart: $?"

	cf apps
	cf services

	releaseAllPlanCredentials $1 $2 $3 $4
	deleteAllPlanServices $1 $2 $3
}

function restartServiceBroker() {
 	cf target -o solace-systems -s solace-messaging
	cf restart $SB_APP
}

function createEmptyApps() {

 MAXCREDS=$1

 if [ ! -d $LOG_DIR/empty_app ]; then
    mkdir $LOG_DIR/empty_app
 fi
 touch $LOG_DIR/empty_app/empty_file

 APP_COUNTER=0
 while [  $APP_COUNTER -lt $MAXCREDS ]; do
   export EMPTY_APP_NAME="a_"$APP_COUNTER
   log "Creating application $EMPTY_APP_NAME"
   (cd $LOG_DIR/empty_app; cf push $EMPTY_APP_NAME -no-start -no-route -p . -m 1m -k 1m)
   let APP_COUNTER=APP_COUNTER+1 
 done

}

function deleteEmptyApps() {

 MAXCREDS=$1

 APP_COUNTER=0
 while [  $APP_COUNTER -lt $MAXCREDS ]; do
   export EMPTY_APP_NAME="a_"$APP_COUNTER
   log "Deleting application $EMPTY_APP_NAME"
   cf delete $EMPTY_APP_NAME -f
   let APP_COUNTER=APP_COUNTER+1 
 done

}

function useAllPlanCredentials() {

 PLAN_NAME=$1
 URL_LIST=` echo $2 | tr ',' '\n'`
 MAXVPN=$3
 MAXCREDS=$4

 ## TODO: Assert that all services are available
 checkServiceBrokerServicePlanStats " Before filling up credentials on plan $PLAN_NAME"

 VMR=0; 
 for URL in $URL_LIST ; do

   ## Bind alls apps for all VPNs to MAXCREDS each
   COUNTER=0
   while [  $COUNTER -lt $MAXVPN ]; do
     export INSTANCE_NAME=$PLAN_NAME"_"$VMR"_"$COUNTER
     APP_COUNTER=0
     while [  $APP_COUNTER -lt $MAXCREDS ]; do
       export EMPTY_APP_NAME="a_"$APP_COUNTER
       log "Binding service $INSTANCE_NAME to $EMPTY_APP_NAME"
       cf bind-service $EMPTY_APP_NAME $INSTANCE_NAME
       let APP_COUNTER=APP_COUNTER+1 
     done

     let COUNTER=COUNTER+1 
   done

   let VMR=VMR+1 

 done

 ## TODO: Assert that all services are used
 checkServiceBrokerServicePlanStats " Filled up credentials on plan $PLAN_NAME"

}

function releaseAllPlanCredentials() {

 PLAN_NAME=$1
 URL_LIST=` echo $2 | tr ',' '\n'`
 MAXVPN=$3
 MAXCREDS=$4

 ## TODO: Assert that all services are available
 checkServiceBrokerServicePlanStats " Before releasing credentials on plan $PLAN_NAME"

 VMR=0; 
 for URL in $URL_LIST ; do
   ## Unbind alls apps from all VPNs to MAXCREDS each
   COUNTER=0
   while [  $COUNTER -lt $MAXVPN ]; do
     export INSTANCE_NAME=$PLAN_NAME"_"$VMR"_"$COUNTER
     APP_COUNTER=0
     while [  $APP_COUNTER -lt $MAXCREDS ]; do
       export EMPTY_APP_NAME="a_"$APP_COUNTER
       log "Unbinding service $INSTANCE_NAME from $EMPTY_APP_NAME"
       cf unbind-service $EMPTY_APP_NAME $INSTANCE_NAME
       let APP_COUNTER=APP_COUNTER+1 
     done

     let COUNTER=COUNTER+1 
   done

   let VMR=VMR+1 

 done

 checkServiceBrokerServicePlanStats " Released credentials on plan $PLAN_NAME"

}

function useAllPlanServices() {

 PLAN_NAME=$1
 URL_LIST=` echo $2 | tr ',' '\n'`
 MAXVPN=$3

 ## TODO: Assert that all services are available
 checkServiceBrokerServicePlanStats " Before filling up services on plan $PLAN_NAME"

 VMR=0; 
 for URL in $URL_LIST ; do

   ## Fill up the plan to max $MAXVPN
   COUNTER=0
   while [  $COUNTER -lt $MAXVPN ]; do
     INSTANCE_NAME=$PLAN_NAME"_"$VMR"_"$COUNTER
     log "Creating service "$INSTANCE_NAME" on VMR "$VMR" Counter "$COUNTER
     cf create-service solace-messaging $PLAN_NAME $INSTANCE_NAME
     let COUNTER=COUNTER+1 
   done

   let VMR=VMR+1 

 done

 ## TODO: Assert that all services are used
 checkServiceBrokerServicePlanStats " Filled up services on plan $PLAN_NAME"

}


function deleteAllPlanServices() {

 PLAN_NAME=$1
 URL_LIST=` echo $2 | tr ',' '\n'`
 MAXVPN=$3

 ## Do Deletes
 VMR=0; 
 for URL in $URL_LIST ; do

   ## Delete all previously created plan services
   COUNTER=0
   while [  $COUNTER -lt $MAXVPN ]; do
     INSTANCE_NAME=$PLAN_NAME"_"$VMR"_"$COUNTER
     log "Deleting service "$INSTANCE_NAME"  on VMR "$VMR" Counter "$COUNTER
     forceDeleteServiceNoWait $INSTANCE_NAME
     let COUNTER=COUNTER+1 
   done

   ## Wait on all deletes
   COUNTER=0
   while [  $COUNTER -lt $MAXVPN ]; do
     INSTANCE_NAME=$PLAN_NAME"_"$VMR"_"$COUNTER
     log "Waiting on Deleting service "$INSTANCE_NAME"  on VMR "$VMR" Counter "$COUNTER
     waitForServiceDelete $INSTANCE_NAME
     let COUNTER=COUNTER+1 
   done


   let VMR=VMR+1 

 done

 ## TODO: Assert that all services are released
 checkServiceBrokerServicePlanStats " Released all on plan $PLAN_NAME"

}

function testServiceBroker() {

  testMarketPlace

  ## Reset SB
  resetServiceBroker

  addLargeMessageRoutersToServiceBroker
  log "ServiceBroker: After Adding Large Plan"
  curl -sX GET $SB_BASE/solace/status/repositories -H "Content-Type: application/json;charset=UTF-8"

  addSharedMessageRoutersToServiceBroker
  log "ServiceBroker: After Adding Shared Plan"
  curl -sX GET $SB_BASE/solace/status/repositories -H "Content-Type: application/json;charset=UTF-8"

  createEmptyApps 100

  useAndDeleteAllPlanServices shared $SHARED_VMR_LIST 5 20

  useAndDeleteAllPlanServices large $LARGE_VMR_LIST 1 100

  deleteEmptyApps 100

}


function  installSampleApplication() {

 log "SampleApplication: About to install"

 checkServiceBrokerServicePlanStats "SampleApplication: before application install"

 ls $SAMPLE_APP_DIR
 cf push sample-cloud-app -p $SAMPLE_APP_DIR/sample-cloud-app.jar -b java_buildpack_offline

 log "List applications"
 cf apps

 log "SampleApplication: Confirming application is started"
 cf apps | grep sample-cloud-app | grep started

 cf logs sample-cloud-app --recent

}

function cleanupBeforeSampleApplication() {

 log "SampleApplication: Cleanup before testing - Unbinding"
 unbindService sample-cloud-app sample_app_large1
 unbindService sample-cloud-app sample_app_shared1

 log "SampleApplication: Cleanup before testing - Services"
 # Just in case we are iterating
 forceDeleteServiceNoWait sample_app_shared1
 forceDeleteServiceNoWait sample_app_large1 

 waitForServiceDelete sample_app_shared1
 waitForServiceDelete sample_app_large1

}


function  createServicesForSampleApplication() {

 log "SampleApplication: Create a shared service-instance"
 cf create-service solace-messaging shared sample_app_shared1
 cf services | grep sample_app_shared1 | grep "create succeeded"

 log "SampleApplication: Create a large service-instance"
 cf create-service solace-messaging large sample_app_large1
 cf services | grep sample_app_large1 | grep "create succeeded"

 checkServiceBrokerServicePlanStats " after application install and creating services"

}

function bindSampleApplicationToServices() {

 log "SampleApplication: bind to service sample_app_shared1"
 cf bind-service sample-cloud-app sample_app_shared1
 cf services | grep sample_app_shared1 | grep sample-cloud-app

 log "SampleApplication: bind to service sample_app_large1"
 cf bind-service sample-cloud-app sample_app_large1
 cf services | grep sample_app_large1 | grep sample-cloud-app

 log "SampleApplication: env"
 cf env sample-cloud-app

 checkServiceBrokerServicePlanStats " after binding to services "

}


function restageSampleApplication() {

 ## Restage
 cf restage sample-cloud-app
 # Reconfirm started
 cf apps | grep sample-cloud-app | grep started
 ## Reconfirm bindings
 cf services | grep sample_app_shared1 | grep sample-cloud-app
 cf services | grep sample_app_large1 | grep sample-cloud-app

}


function testSampleApplication() {

 lookupSampleApplicationDetails

 cf logs sample-cloud-app --recent

 log "SampleApplication: Status"
 curl -sX GET -H "Authorization: Basic c29sYWNlZGVtbzpzb2xhY2VkZW1v"  http://$APP_URL/status 

 log "SampleApplication: Adding subscription in application"
 ## Add subscription
 curl -sX POST -H "Authorization: Basic c29sYWNlZGVtbzpzb2xhY2VkZW1v" -H "Content-Type: application/json;charset=UTF-8" -d '{"subscription": "test"}' http://$APP_URL/subscription # | grep "{}"

 FOUND_EXPECTED_MESSAGE=0
 MAXWAIT=5
 while [ "$FOUND_EXPECTED_MESSAGE" -lt "1" ] && [ "$MAXWAIT" -gt "0" ]; do

   log "SampleApplication: Sending a message to subscription"
   ## Send a message a couple of times.. and wait
   curl -sX POST -H "Authorization: Basic c29sYWNlZGVtbzpzb2xhY2VkZW1v" -H "Content-Type: application/json;charset=UTF-8" -d '{"topic": "test", "body": "TEST_MESSAGE"}' http://$APP_URL/message | grep "{}"
   curl -sX POST -H "Authorization: Basic c29sYWNlZGVtbzpzb2xhY2VkZW1v" -H "Content-Type: application/json;charset=UTF-8" -d '{"topic": "test", "body": "TEST_MESSAGE"}' http://$APP_URL/message | grep "{}"

   log "Waiting for message timeout counter: $MAXWAIT"
   sleep 10

   LAST_STATUS=`curl -sX GET -H "Authorization: Basic c29sYWNlZGVtbzpzb2xhY2VkZW1v" http://$APP_URL/status`
   log "SampleApplication: Last Status $LAST_STATUS"

   LAST_MESSAGE=`curl -sX GET -H "Authorization: Basic c29sYWNlZGVtbzpzb2xhY2VkZW1v"  http://$APP_URL/message`
   log "SampleApplication: Last Message $LAST_MESSAGE"

   FOUND_EXPECTED_MESSAGE=`echo $LAST_MESSAGE | grep TEST_MESSAGE | grep "test" | wc -l`
   let MAXWAIT=MAXWAIT-1
 done
 
 log "SampleApplication: Status"
 curl -sX GET -H "Authorization: Basic c29sYWNlZGVtbzpzb2xhY2VkZW1v"  http://$APP_URL/status 

 cf logs sample-cloud-app --recent

}

function cleanupAfterSampleApplication() {

  checkServiceBrokerServicePlanStats "SampleApplication: Before unbinding"

  log "SampleApplication: Unbinding"
  unbindService sample-cloud-app sample_app_large1
  unbindService sample-cloud-app sample_app_shared1

  checkServiceBrokerServicePlanStats "SampleApplication: After unbinding"

  log "SampleApplication: Deleting Service"
  forceDeleteServiceNoWait sample_app_shared1
  forceDeleteServiceNoWait sample_app_large1

  waitForServiceDelete sample_app_shared1
  waitForServiceDelete sample_app_large1

  checkServiceBrokerServicePlanStats "SampleApplication: After delete services "

  log "SampleApplication: Deleted"
  cf delete sample-cloud-app -f

}

function installAndTestSampleApplication() {

  log "### Installing And Testing Sample Application"
  installSampleApplication

## Should not be needed in a clean environment
  cleanupBeforeSampleApplication

  createServicesForSampleApplication

  bindSampleApplicationToServices

  restageSampleApplication

  testSampleApplication

  cleanupAfterSampleApplication

}

function  installTestApplications() {

 log "TestApplications: About to install"

 checkServiceBrokerServicePlanStats "TestApplications: before application install"

 ls $TEST_APP_DIR
 cf push 0-test-app -p $TEST_APP_DIR/test-app.jar -b java_buildpack_offline
 cf push 2-test-app -p $TEST_APP_DIR/test-app.jar -b java_buildpack_offline
 ( cd $SOLACE_TILE_DIR/test-app-nodejs; cf push 2-test-nodejs )


 log "List applications"
 cf apps

 log "TestApplication: Confirming application(s) are started"
 cf apps | grep test-app | grep started
 cf apps | grep 2-test-nodejs | grep started


}

function cleanupBeforeTestApplications() {

 log "TestApplications: Cleanup before testing - Unbinding"
 unbindService 2-test-app test_app_large1
 unbindService 2-test-app test_app_shared1

 unbindService 2-test-nodejs test_nodejs_shared1
 unbindService 2-test-nodejs test_nodejs_shared2

 log "TestApplications: Cleanup before testing - Services"
 # Just in case we are iterating
 forceDeleteServiceNoWait test_app_shared1
 forceDeleteServiceNoWait test_app_large1 

 forceDeleteServiceNoWait test_nodejs_shared1
 forceDeleteServiceNoWait test_nodejs_shared2

 waitForServiceDelete test_app_shared1
 waitForServiceDelete test_app_large1

 waitForServiceDelete test_nodejs_shared1
 waitForServiceDelete test_nodejs_shared2

}

function  createServicesForTestApplications() {

 log "TestApplication: Create a shared service-instance"
 cf create-service solace-messaging shared test_app_shared1
 cf services | grep test_app_shared1 | grep "create succeeded"

 log "TestApplication: Create a large service-instance"
 cf create-service solace-messaging large test_app_large1
 cf services | grep test_app_large1 | grep "create succeeded"

 log "TestApplication: Create a shared service-instance"
 cf create-service solace-messaging shared test_nodejs_shared1
 cf services | grep test_nodejs_shared1 | grep "create succeeded"

 log "TestApplication: Create a shared service-instance"
 cf create-service solace-messaging shared test_nodejs_shared2
 cf services | grep test_nodejs_shared2 | grep "create succeeded"

 checkServiceBrokerServicePlanStats " after test application install and creating services"

}

function bindTestApplicationsToServices() {

 log "TestApplication: bind to service test_app_shared1"
 cf bind-service 2-test-app test_app_shared1
 cf services | grep test_app_shared1 | grep 2-test-app

 log "TestApplication: bind to service test_app_large1"
 cf bind-service 2-test-app test_app_large1
 cf services | grep test_app_large1 | grep 2-test-app

 log "TestApplication: env"
 cf env 2-test-app

 log "TestApplication: bind to service test_nodejs_shared1"
 cf bind-service 2-test-nodejs test_nodejs_shared1
 cf services | grep test_nodejs_shared1 | grep 2-test-nodejs

 log "TestApplication: bind to service test_nodejs_shared2"
 cf bind-service 2-test-nodejs test_nodejs_shared2
 cf services | grep test_nodejs_shared2 | grep 2-test-nodejs

 log "TestApplication: env"
 cf env 2-test-nodejs

 checkServiceBrokerServicePlanStats " after binding to services "

}


function restageTestApplications() {

 ## Restage
 cf restage 2-test-app
 # Reconfirm started
 cf apps | grep 2-test-app | grep started
 ## Reconfirm bindings
 cf services | grep test_app_shared1 | grep 2-test-app
 cf services | grep test_app_large1 | grep 2-test-app


 ## Restage
 cf restage 2-test-nodejs
 # Reconfirm started
 cf apps | grep 2-test-nodejs | grep started
 ## Reconfirm bindings
 cf services | grep test_nodejs_shared1 | grep 2-test-nodejs
 cf services | grep test_nodejs_shared2 | grep 2-test-nodejs

}


function cleanupAfterTestApplications() {

  checkServiceBrokerServicePlanStats "TestApplication: Before unbinding"

  log "TestApplication: Unbinding"
  unbindService 2-test-app test_app_large1
  unbindService 2-test-app test_app_shared1

  unbindService 2-test-nodejs test_nodejs_shared1
  unbindService 2-test-nodejs test_nodejs_shared2

  checkServiceBrokerServicePlanStats "TestApplication: After unbinding"

  log "TestApplication: Deleting Service"
  forceDeleteServiceNoWait test_app_shared1
  forceDeleteServiceNoWait test_app_large1

  forceDeleteServiceNoWait test_nodejs_shared1
  forceDeleteServiceNoWait test_nodejs_shared2

  waitForServiceDelete test_app_shared1
  waitForServiceDelete test_app_large1

  waitForServiceDelete test_nodejs_shared1
  waitForServiceDelete test_nodejs_shared2

  checkServiceBrokerServicePlanStats "TestApplication: After delete services "

  log "TestApplication: Deleted"
  cf delete 0-test-app -f
  cf delete 2-test-app -f
  cf delete 2-test-nodejs -f

}

function lookupTestApplicationDetails() {

 export B0_TEST_APP_URL=`cf apps | grep 0-test-app | grep started  | awk '{ print $6}'`
 log "TestApplication URL: http://"${B0_TEST_APP_URL}

 export B2_TEST_APP_URL=`cf apps | grep 2-test-app | grep started  | awk '{ print $6}'`
 log "TestApplication URL: http://"${B2_TEST_APP_URL}

 export B2_TEST_NODEJS_URL=`cf apps | grep 2-test-nodejs | grep started  | awk '{ print $6}'`
 log "TestApplication URL: http://"${B2_TEST_NODEJS_URL}

 export TEST_APP_URLS="${B0_TEST_APP_URL},${B2_TEST_APP_URL},${B2_TEST_NODEJS_URL}"

}

function runTestsWithTestApplications() {

 lookupTestApplicationDetails

## Check the test-server
 cd $SOLACE_TILE_DIR/test-server

 ## Already defined: LARGE_VMR_LIST , SHARED_VMR_LIST
 # Run the tests with the $TEST_APP_URLS 
 log "Test name: $1"
 log "TEST_APP_URLS=$TEST_APP_URLS"
 log "LARGE_VMR_LIST=$LARGE_VMR_LIST"
 log "SHARED_VMR_LIST=$SHARED_VMR_LIST"
 log "./gradlew clean test --info"
 ## Make sure to provide SECURITY_USER_NAME SECURITY_USER_PASSWORD as we don't want 
 ## to re-use the ones from the environment as they are for the service broker
 SECURITY_USER_NAME="solacetest" SECURITY_USER_PASSWORD="solacetest" ./gradlew clean test --info | tee $LOG_DIR/test.log

 REPORT_NAME="test_report_"$1
 log "Archiving Report $REPORT_NAME"
 tar -czf $LOG_DIR/$REPORT_NAME.tgz build/reports build/test-results

}

function installAndRunTests() {

 cleanupBeforeTestApplications

 installTestApplications

 createServicesForTestApplications
 bindTestApplicationsToServices
 restageTestApplications
 ## RUN THE TESTING
 runTestsWithTestApplications all_apps

 cleanupAfterTestApplications

 ## Capture the output and report it..

 log "Archiving All Test Reports"
 cd $LOG_DIR
 ls -l test_report*.tgz
 tar -czf $LOG_DIR/test-deploy-report-$VERSION.tgz test_report*.tgz
 log "Test Deploy Report:"
 ls -l $LOG_DIR/test-deploy-report-$VERSION.tgz

 log "Checking build results (exit and fail if not successful):"
 ## Non 0 return code will fail the build when not successful.
 grep "BUILD SUCCESSFUL" $LOG_DIR/test.log

}

function explorePCFEnv() {


log "${POOL_DIR}:"
ls -la ${POOL_DIR}
log "${POOL_DIR}/metadata:"
cat ${POOL_DIR}/metadata
log "${POOL_DIR}/name:"
cat ${POOL_DIR}/name
log "${POOL_DIR}/pcf/:"
ls -la ${POOL_DIR}/pcf
log "${POOL_DIR}/pcf/claimed:"
ls -la ${POOL_DIR}/pcf/claimed
log "${POOL_DIR}/pcf/unclaimed:"
ls -la ${POOL_DIR}/pcf/unclaimed


PIE_NAME=`cat ${POOL_DIR}/name`
log "${POOL_DIR}/pcf/claimed/${PIE_NAME}:"
cat ${POOL_DIR}/pcf/claimed/$PIE_NAME

}

####################################### MAIN ###########################################

cd ${POOL_DIR}

explorePCFEnv

# Insert tests here
# You have access to the pcf command, and you are in the dir that has the metadata file

# Typical tests here would:
# - Connect to your deployed services and brokers
# - Verify that you can create service instances
# - Bind those to test apps
# - Make sure the right things happen

## Enable or Disable exit on error
set -e

  installTools

  testHeader

  log "### Tests"

##
# Test Tile
##

 testTileInstallationAndLogin

## Get some details about the Service Broker for later use..
 lookupServiceBrokerDetails

## Swtich to a test org and space for all other activities
 switchToTestOrgAndSpace

##
# Test Service Broker provided functions
##

 testServiceBroker

# set -x # activate debugging from here

##
# Install and Smoke Test the Sample Application
##

 installAndTestSampleApplication

##
# Install and run a test application
##

 installAndRunTests

# set +x	# stop debugging from here

## 
#  TODO:
#
#  Assertions for success criteria based on stats from ServiceBroker
#  Tests to command cf environment for testing: no longer relies on script to issue cf commands
#
#
##
