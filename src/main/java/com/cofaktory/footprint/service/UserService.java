package com.cofaktory.footprint.service;

import com.cofaktory.footprint.dao.UserDao;
import com.cofaktory.footprint.model.User;
import com.cofaktory.footprint.myExceptions.DataAccessException;
import com.cofaktory.footprint.myExceptions.UserNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

public class UserService {
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User authenticate(String email, String password) throws DataAccessException {
        if (userDao.authenticate(email, password)) {
            return userDao.getByEmail(email);
        }
        throw new UserNotFoundException("Invalid credentials");
    }

    public User getUserById(int id) throws DataAccessException {
        return userDao.getById(id);
    }

    public List<User> getAllUsers() throws DataAccessException {
        return userDao.getAll();
    }

    public boolean saveUser(User user) throws DataAccessException {
        return userDao.save(user);
    }

    public boolean deleteUser(User user) throws DataAccessException {
        return userDao.delete(user);
    }

    public User getUserByBranch(int branchId) throws DataAccessException {
        return userDao.getAll().stream()
                .filter(u -> u.getBranchId() == branchId)
                .findFirst()
                .orElse(null);
    }
}