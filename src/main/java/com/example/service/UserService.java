package com.example.service;

import com.example.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(String name, String email, Integer age) throws Exception;
    Optional<User> getUserById(Long id);
    List<User> getAllUsers();
    User updateUser(Long id, String name, String email, Integer age) throws Exception;
    boolean deleteUser(Long id) throws Exception;

}
