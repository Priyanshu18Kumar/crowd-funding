package com.crowdfund.db;

import com.crowdfund.exception.DataAccessException;

import java.util.List;

/**
 * A GENERIC interface implemented by every data-access class.
 *
 * <p>{@code T} is the type of object the DAO works with, so {@code UserDAO}
 * implements {@code DAO<User>} and {@code CampaignDAO} implements
 * {@code DAO<Campaign>}. This gives a single, consistent set of CRUD methods
 * across the whole project and shows how generics and interfaces work
 * together.</p>
 */
public interface DAO<T> {

    /** Insert a new row. */
    void insert(T item) throws DataAccessException;

    /** Update an existing row (matched by id). */
    void update(T item) throws DataAccessException;

    /** Delete a row by id. */
    void delete(int id) throws DataAccessException;

    /** Find one row by id, or null if there is none. */
    T findById(int id) throws DataAccessException;

    /** Return every row. */
    List<T> findAll() throws DataAccessException;
}
