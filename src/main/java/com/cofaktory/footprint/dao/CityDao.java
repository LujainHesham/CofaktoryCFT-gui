package com.cofaktory.footprint.dao;

import com.cofaktory.footprint.model.City;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import java.util.List;

public interface CityDao extends DAO<City> {
    // Get all branches in a city
    List<Integer> getBranchIdsByCityId(int cityId) throws DataAccessException;

    // Get city by name
    City getCityByName(String cityName) throws DataAccessException;
}