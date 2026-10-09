package com.journal.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"server.address=127.0.0.1"})
@ActiveProfiles("test")
class LocalConfigurationTest {

    @Autowired
    private Environment env;

    @Test
    void testLocalAddressBound() {
        assertThat(env.getProperty("server.address")).isEqualTo("127.0.0.1");
    }
}
