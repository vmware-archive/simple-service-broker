package io.pivotal.cf.service.connector;

import lombok.Data;
import org.springframework.cloud.service.ServiceInfo;

@Data
public class HelloServiceInfo implements ServiceInfo {

    static final String URI_SCHEME = "hello";

    private String id;
    private String hostname;
    private String username;
    private String password;
    private String port;

    public HelloServiceInfo(String id, String hostname, String port, String username, String password) {
        super();
        this.id = id;
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    String getUri() {
        return "http://" + this.hostname + ":" + this.port;
    }
}
