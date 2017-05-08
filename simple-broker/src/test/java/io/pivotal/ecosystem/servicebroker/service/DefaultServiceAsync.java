package io.pivotal.ecosystem.servicebroker.service;

public class DefaultServiceAsync extends DefaultServiceImpl {

    @Override
    public boolean isAsync() {
        return true;
    }
}
