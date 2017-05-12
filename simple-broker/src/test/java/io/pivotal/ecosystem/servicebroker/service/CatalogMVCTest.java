package io.pivotal.ecosystem.servicebroker.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.servicebroker.controller.CatalogController;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CatalogMVCTest {

    private MockMvc mockMvc;

    @Autowired
    private CatalogService catalogService;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CatalogController(catalogService)).build();
    }

    @Test
    public void testCatalog() throws Exception {
        this.mockMvc.perform(get("/v2/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.services", hasSize(1)))
                .andDo(print());
    }
}
