package io.pivotal.cf.servicebroker.service;

import io.pivotal.cf.servicebroker.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class, loader = AnnotationConfigContextLoader.class)
public class CatalogServiceTest {

    @Autowired
    private CatalogService catalogService;

    @Test
    public void testGetEntitledCatalog() throws ServiceBrokerException {
        Catalog catalog = catalogService.getCatalog();
        assertNotNull(catalog);
        assertTrue(catalog.getServiceDefinitions().size() > 0);
    }

    @Test
    public void testGetEntitledCatalogItem() throws ServiceBrokerException {
        assertNull(catalogService.getServiceDefinition(null));
        assertNull(catalogService.getServiceDefinition(""));
        assertNotNull(catalogService.getServiceDefinition(TestConfig.SD_ID));
    }
}
