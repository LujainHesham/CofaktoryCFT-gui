package com.cofaktory.footprint.dao.jdbcImpl;

import com.cofaktory.footprint.dao.CityDao;
import com.cofaktory.footprint.model.City;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CityDaoImpl implements CityDao {
    private final DataSource dataSource;

    public CityDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public City getById(int id) throws DataAccessException {
        String sql = "SELECT * FROM City WHERE CityID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new City(
                            rs.getInt("CityID"),
                            rs.getString("CityName")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get city by ID: " + id, e);
        }
        return null;
    }

    @Override
    public List<City> getAll() throws DataAccessException {
        List<City> cities = new ArrayList<>();
        String sql = "SELECT * FROM City";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                cities.add(new City(
                        rs.getInt("CityID"),
                        rs.getString("CityName")
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get all cities", e);
        }
        return cities;
    }

    @Override
    public boolean save(City city) throws DataAccessException {
        if (city.getCityId() > 0) {
            return update(city);
        } else {
            return insert(city);
        }
    }

    @Override
    public boolean insert(City city) throws DataAccessException {
        String sql = "INSERT INTO City (CityName) VALUES (?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, city.getCityName());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        city.setCityId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert city", e);
        }
    }

    @Override
    public boolean update(City city) throws DataAccessException {
        String sql = "UPDATE City SET CityName = ? WHERE CityID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, city.getCityName());
            stmt.setInt(2, city.getCityId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update city: " + city.getCityId(), e);
        }
    }



    @Override
    public boolean delete(City cityId) throws DataAccessException {
        String sql = "DELETE FROM City WHERE CityID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cityId.getCityId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete city: " + cityId, e);
        }
    }

    @Override
    public List<Integer> getBranchIdsByCityId(int cityId) throws DataAccessException {
        List<Integer> branchIds = new ArrayList<>();
        String sql = "SELECT BranchID FROM Branch WHERE CityID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cityId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    branchIds.add(rs.getInt("BranchID"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get branch IDs for city: " + cityId, e);
        }
        return branchIds;
    }

    @Override
    public City getCityByName(String cityName) throws DataAccessException {
        String sql = "SELECT * FROM City WHERE CityName = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cityName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new City(
                            rs.getInt("CityID"),
                            rs.getString("CityName")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get city by name: " + cityName, e);
        }
        return null;
    }
}