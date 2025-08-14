package com.cofaktory.footprint.dao;

import java.util.List;

import com.cofaktory.footprint.myExceptions.DataAccessException;

/**
 * Generic Data Access Object (DAO) interface for CRUD operations
 * @param <T> The type of entity this DAO manages
 */
public interface DAO<T> {

    /**
     * Retrieves an entity by its ID
     * @param id The ID of the entity to retrieve
     * @return The found entity
     * @throws DataAccessException If there's an error accessing the data
     */
     T getById(int id) throws DataAccessException;

    /**
     * Retrieves all entities
     * @return List of all entities
     * @throws DataAccessException If there's an error accessing the data
     */
    List<T> getAll() throws DataAccessException;

    /**
     * Saves an entity (inserts or updates)
     * @param object The entity to save
     * @return true if successful
     * @throws DataAccessException If there's an error accessing the data
     */
    boolean save(T object) throws DataAccessException;

    /**
     * Inserts a new entity
     * @param object The entity to insert
     * @return true if successful
     * @throws DataAccessException If there's an error accessing the data
     */
    boolean insert(T object) throws DataAccessException;

    /**
     * Updates an existing entity
     * @param object The entity to update
     * @return true if successful
     * @throws DataAccessException If there's an error accessing the data
     */
    boolean update(T object) throws DataAccessException;

    /**
     * Deletes an entity
     * @param object The entity to delete
     * @return true if successful
     * @throws DataAccessException If there's an error accessing the data
     */
    boolean delete(T object) throws DataAccessException;


}