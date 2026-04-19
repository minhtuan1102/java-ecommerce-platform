package com.tuan.ecommerce.modules.shop.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuan.ecommerce.modules.auth.application.dto.RegisterRequest;
import com.tuan.ecommerce.modules.auth.application.dto.AuthResponse;
import com.tuan.ecommerce.modules.shop.application.dto.CreateShopRequest;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ShopControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private Filter springSecurityFilterChain;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.webApplicationContext)
                .addFilters(this.springSecurityFilterChain)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    void shouldRegisterShopAndRetrieveItSuccessfully() throws Exception {
        // 1. Register a new user
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("user" + uniqueSuffix);
        registerRequest.setEmail("user" + uniqueSuffix + "@mail.com");
        registerRequest.setPassword("password123");

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = registerResult.getResponse().getContentAsString();
        String accessToken = com.jayway.jsonpath.JsonPath.read(responseContent, "$.accessToken");

        // 2. Create a shop for this user
        CreateShopRequest createShopRequest = new CreateShopRequest();
        createShopRequest.setName("My Awesome Shop " + uniqueSuffix);
        createShopRequest.setDescription("Best shop ever");

        MvcResult shopResult = mockMvc.perform(post("/api/v1/shops")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createShopRequest)))
                .andReturn();
        
        if (shopResult.getResponse().getStatus() != 201) {
            System.err.println("SHOP CREATION FAILED: " + shopResult.getResponse().getContentAsString());
        }
        
        org.assertj.core.api.Assertions.assertThat(shopResult.getResponse().getStatus()).isEqualTo(201);

        // 3. Try to fetch my shop (user now has ROLE_SELLER dynamically added to DB, but token might need refresh if roles are baked in token)
        // Login again to get a fresh token
        com.tuan.ecommerce.modules.auth.application.dto.LoginRequest loginRequest = new com.tuan.ecommerce.modules.auth.application.dto.LoginRequest();
        loginRequest.setEmail(registerRequest.getEmail());
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
                
        String loginResponseContent = loginResult.getResponse().getContentAsString();
        String newAccessToken = com.jayway.jsonpath.JsonPath.read(loginResponseContent, "$.accessToken");

        // Now call my-shop with new token
        mockMvc.perform(get("/api/v1/shops/my-shop")
                .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("My Awesome Shop " + uniqueSuffix));
    }
}


