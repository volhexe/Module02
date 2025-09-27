package com.example.service;

import com.example.dto.UserCreateRequest;
import com.example.dto.UserDto;
import com.example.model.User;
import com.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    private UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getAge(), u.getCreatedAt());
    }

    @Override
    @Transactional
    public UserDto create(UserCreateRequest request) {
        LOGGER.info("Creating user email={} name={}", request.email(), request.name());
        User user = new User(request.name(), request.email(), request.age());
        try {
            User saved = repository.save(user);
            LOGGER.info("User created id={} email={}", saved.getId(), saved.getEmail());
            return toDto(saved);
        } catch (DataIntegrityViolationException dive) {
            LOGGER.warn("Constraint violation creating user email={}", request.email(), dive);
            throw new RuntimeException("Constraint violation: " + dive.getMessage(), dive);
        }
    }

    @Override
    public Optional<UserDto> getById(Long id) {
        LOGGER.debug("Fetching user id={}", id);
        return repository.findById(id).map(u -> {
            LOGGER.debug("Found user id={} email={}", u.getId(), u.getEmail());
            return toDto(u);
        });
    }

    @Override
    public List<UserDto> getAll() {
        LOGGER.debug("Listing all users");
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserCreateRequest request) {
        LOGGER.info("Updating user id={}", id);
        User user = repository.findById(id).orElseThrow(() -> {
            LOGGER.warn("User not found id={}", id);
            return new RuntimeException("User not found");
        });
        if (request.name() != null && !request.name().isBlank()) user.setName(request.name().trim());
        if (request.email() != null && !request.email().isBlank()) user.setEmail(request.email().trim());
        if (request.age() != null) user.setAge(request.age());
        User saved = repository.save(user);
        LOGGER.info("User updated id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        LOGGER.info("Deleting user id={}", id);
        if (!repository.existsById(id)) {
            LOGGER.debug("Delete failed user not found id={}", id);
            return false;
        }
        repository.deleteById(id);
        LOGGER.info("User deleted id={}", id);
        return true;
    }
}
