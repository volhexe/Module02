package com.example.service;

import com.example.dao.UserDao;
import com.example.model.User;
import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User createUser(String name, String email, Integer age) throws Exception {

        if (name == null || name.trim().isBlank()) {
            throw new Exception("Name is required and cannot be blank");
        }
        if (email == null || email.trim().isBlank()) {
            throw new Exception("Email is required and cannot be blank");
        }
        if (age == null || age < 0) {
            throw new Exception("Age is required and must be non-negative");
        }
        if (userDao.findByEmail(email.trim()).isPresent()) {
            throw new Exception("Email already exists");
        }
        User user = new User(name.trim(), email.trim(), age);
        return userDao.create(user);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userDao.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    @Override
    public User updateUser(Long id, String name, String email, Integer age) throws Exception {
        Optional<User> opt = userDao.findById(id);
        if (opt.isEmpty()) {
            throw new Exception("User not found");
        }
        User user = opt.get();
        if (name != null && !name.trim().isBlank()) {
            user.setName(name.trim());
        }
        if (email != null && !email.trim().isBlank()) {

            String newEmail = email.trim();
            if (!newEmail.equals(user.getEmail()) && userDao.findByEmail(newEmail).isPresent()) {
                throw new Exception("Email already exists");
            }
            user.setEmail(newEmail);
        }
        if (age != null && age >= 0) {
            user.setAge(age);
        }
        return userDao.update(user);
    }

    @Override
    public boolean deleteUser(Long id) throws Exception {
        Optional<User> opt = userDao.findById(id);
        if (opt.isEmpty()) {
            return false;
        }
        return userDao.delete(id);
    }
}