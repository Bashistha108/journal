package com.journal.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LocalOnlyFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testValidGetRequest() throws Exception {
        mockMvc.perform(get("/api/v1/tables")
                .header("Host", "localhost:8080"))
                .andExpect(status().isOk());
    }

    @Test
    void testMissingXTJClientOnPost() throws Exception {
        mockMvc.perform(post("/api/v1/tables")
                .header("Host", "localhost:8080")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN_ORIGIN"));
    }

    @Test
    void testForeignOrigin() throws Exception {
        mockMvc.perform(get("/api/v1/tables")
                .header("Host", "localhost:8080")
                .header("Origin", "http://evil.com"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN_ORIGIN"));
    }

    @Test
    void testBadHost() throws Exception {
        mockMvc.perform(get("/api/v1/tables")
                .header("Host", "10.0.0.5:8080"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN_ORIGIN"));
    }
    
    @Test
    void testSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/tables")
                .header("Host", "localhost:8080"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"));
    }
}
