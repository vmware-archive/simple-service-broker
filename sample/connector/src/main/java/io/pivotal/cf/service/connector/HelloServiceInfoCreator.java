package io.pivotal.cf.service.connector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.cloudfoundry.CloudFoundryServiceInfoCreator;
import org.springframework.cloud.cloudfoundry.Tags;

import java.util.Map;

@Slf4j
public class HelloServiceInfoCreator extends CloudFoundryServiceInfoCreator<HelloServiceInfo> {

    HelloServiceInfoCreator() {
        super(new Tags(HelloServiceInfo.URI_SCHEME), HelloServiceInfo.URI_SCHEME);
    }

    @Override
    public HelloServiceInfo createServiceInfo(Map<String, Object> serviceData) {
        log.info("Returning hello service info: " + serviceData.toString());

        Map<String, Object> credentials = getCredentials(serviceData);
        String id = getId(serviceData);
        String host = credentials.get("host").toString();
        String port = credentials.get("port").toString();
        String adminId = credentials.get("username").toString();
        String adminPw = credentials.get("password").toString();

        return new HelloServiceInfo(id, host, port, adminId, adminPw);
    }
}