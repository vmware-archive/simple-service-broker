package io.pivotal.cf.servicebroker.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ServiceInstanceBinding implements Serializable {

    public static final long serialVersionUID = 1L;

    private String id;

    private String serviceInstanceId;

    private Map<String, Object> credentials = new HashMap<>();

    public ServiceInstanceBinding(String id, String serviceInstanceId, Map<String, Object> credentials, String syslogDrainUrl, Map<String, Object> bindResource) {
        this.id = id;
        this.serviceInstanceId = serviceInstanceId;
        setCredentials(credentials);
    }

    public String getId() {
        return id;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public Map<String, Object> getCredentials() {
        return credentials;
    }

    private void setCredentials(Map<String, Object> credentials) {
        if (credentials == null) {
            this.credentials = new HashMap<>();
        } else {
            this.credentials = credentials;
        }
    }
}
