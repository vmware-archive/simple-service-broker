package io.pivotal.cf.servicebroker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@Slf4j
public class CatalogService implements org.springframework.cloud.servicebroker.service.CatalogService {

    private Catalog catalog;

    @Override
    public Catalog getCatalog() {
        if (catalog != null) {
            return catalog;
        }

        try {
            return loadCatalog();
        } catch (Exception e) {
            log.error("Error retrieving catalog.", e);
            throw new ServiceBrokerException("Unable to retrieve catalog.", e);
        }
    }

    @Override
    public ServiceDefinition getServiceDefinition(String id) {
        if (id == null) {
            return null;
        }

        for (ServiceDefinition sd : getCatalog().getServiceDefinitions()) {
            if (sd.getId().equals(id)) {
                return sd;
            }
        }
        return null;
    }

    private static String getContents(String fileName) throws IOException {
        URI u = new ClassPathResource(fileName).getURI();
        return new String(Files.readAllBytes(Paths.get(u)));
    }

    private Catalog loadCatalog() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        catalog = mapper.readValue(getContents("catalog.json"), Catalog.class);
        return catalog;
    }
}