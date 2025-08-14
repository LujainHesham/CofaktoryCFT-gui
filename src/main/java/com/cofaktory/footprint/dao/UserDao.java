package com.cofaktory.footprint.dao;

import com.cofaktory.footprint.model.User;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import java.sql.SQLException;

public interface UserDao extends DAO<User> {
    User getByEmail(String email) throws DataAccessException;

    boolean authenticate(String email, String password) throws DataAccessException;

    boolean userExists(User userId) throws SQLException;
}