package io.pivotal.cf.service.client;

import io.pivotal.cf.service.connector.HelloRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(HelloClientController.class)
public class HelloClientControllerTest {

    private static final String USER = "foo";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private HelloRepository helloRepository;


    @Test
    public void testRepo() throws Exception {
        given(this.helloRepository.greeting(USER))
                .willReturn("Hello, " + USER + "!");

        this.mvc.perform(get("/greeting?username=" + USER))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, " + USER + "!"));
    }
}