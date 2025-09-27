package com.example.controller;

import com.example.dto.UserCreateRequest;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDb() {
        userRepository.deleteAll(); // Сбрасываем БД перед каждым тестом
    }

    @Test
    void testCreateUser() throws Exception {
        UserCreateRequest req = new UserCreateRequest("Alice", "alice@example.com", 25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.age").value(25));
    }

    @Test
    void testGetUserById() throws Exception {
        User saved = userRepository.save(new User("Bob", "bob@example.com", 30));

        mockMvc.perform(get("/api/users/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Bob"))
                .andExpect(jsonPath("$.email").value("bob@example.com"))
                .andExpect(jsonPath("$.age").value(30));
    }

    @Test
    void testListUsers() throws Exception {
        userRepository.save(new User("Charlie", "charlie@example.com", 40));
        userRepository.save(new User("Dave", "dave@example.com", 28));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].email", containsInAnyOrder("charlie@example.com", "dave@example.com")));
    }

    @Test
    void testUpdateUser() throws Exception {
        User saved = userRepository.save(new User("Eve", "eve@example.com", 22));

        UserCreateRequest updateReq = new UserCreateRequest("Eve Updated", "eve.updated@example.com", 23);

        mockMvc.perform(put("/api/users/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Eve Updated"))
                .andExpect(jsonPath("$.email").value("eve.updated@example.com"))
                .andExpect(jsonPath("$.age").value(23));
    }

    @Test
    void testDeleteUser() throws Exception {
        User saved = userRepository.save(new User("Frank", "frank@example.com", 35));

        mockMvc.perform(delete("/api/users/" + saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/" + saved.getId()))
                .andExpect(status().isNotFound());
    }


    @Test
    void testGetUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateUserNotFound() throws Exception {
        UserCreateRequest req = new UserCreateRequest("Ghost", "ghost@example.com", 100);

        mockMvc.perform(put("/api/users/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteUserNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateUserWithEmptyName() throws Exception {
        UserCreateRequest req = new UserCreateRequest("", "noName@example.com", 20);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest()); // ожидаем 400
    }

    @Test
    void testCreateUserWithDuplicateEmail() throws Exception {
        userRepository.save(new User("John", "dup@example.com", 44));

        UserCreateRequest req = new UserCreateRequest("Jane", "dup@example.com", 30);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is5xxServerError()); // тк в сервисе выбрасывается RuntimeException
    }
}