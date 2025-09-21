package com.example.service;

import com.example.dao.UserDao;
import com.example.dao.UserDaoImpl;
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
        User user = new User(name, email, age);
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
        if (name != null && !name.isBlank()) user.setName(name.trim());
        if (email != null && !email.isBlank()) user.setEmail(email.trim());
        if (age != null) user.setAge(age);
        return userDao.update(user);
    }

    @Override
    public boolean deleteUser(Long id) throws Exception {
        return userDao.delete(id);
    }
}
