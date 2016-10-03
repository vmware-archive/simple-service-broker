package io.pivotal.cf.service.connector;

import lombok.ToString;
import org.springframework.cloud.service.ServiceInfo;

@ToString
class HelloServiceInfo implements ServiceInfo {

    static final String URI_SCHEME = "hello";

    private String id;
    private String host;
    private String port;
    private String adminId;
    private String adminPw;

    HelloServiceInfo(String id, String host, String port, String adminId, String adminPw) {
        super();
        this.id = id;
        this.host = host;
        this.port = port;
        this.adminId = adminId;
        this.adminPw = adminPw;
    }

    @Override
    public String getId() {
        return id;
    }

    String getUri() {
        return "http://" + host + ":" + port;
    }
}
