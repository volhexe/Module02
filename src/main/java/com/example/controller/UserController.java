package com.example.controller;

import com.example.dto.UserCreateRequest;
import com.example.dto.UserDto;
import com.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserDto> list() {
        LOGGER.info("GET /api/users list");
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> get(@PathVariable ("id") Long id) {
        LOGGER.info("GET /api/users/{}", id);
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody UserCreateRequest req) {
        LOGGER.info("POST /api/users - {}", req);
        UserDto created = service.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable ("id") Long id, @RequestBody UserCreateRequest req) {
        LOGGER.info("PUT /api/users/{}", id);
        try {
            return ResponseEntity.ok(service.update(id, req));
        } catch (RuntimeException e) {
            LOGGER.warn("Update failed for id={}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ("id") Long id) {
        LOGGER.info("DELETE /api/users/{}", id);
        boolean deleted = service.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}