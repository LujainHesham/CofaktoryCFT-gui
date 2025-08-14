package com.cofaktory.footprint.dao;

import com.cofaktory.footprint.model.CarbonFootprintMetrics;

import java.util.List;

public interface CarbonFootprintDao {

    /**
     * Calculate total carbon emissions for a city.
     *
     * @param cityName Name of the city.
     * @return Total carbon emissions in kilograms.
     */
    double calculateTotalEmissionsByCity(String cityName);

    /**
     * Get carbon emissions details for all branches in a city.
     *
     * @param cityName Name of the city.
     * @return List of CarbonFootprintMetrics for each branch.
     */
    List<CarbonFootprintMetrics> getEmissionsDetailsByCity(String cityName);

    /**
     * Get carbon emissions details for a specific branch.
     *
     * @param branchId ID of the branch.
     * @return CarbonFootprintMetrics for the branch.
     */
    CarbonFootprintMetrics getEmissionsDetailsByBranch(int branchId);

    /**
     * Get reduction strategies for a specific branch.
     *
     * @param branchId ID of the branch.
     * @return List of reduction strategy descriptions.
     */
    List<String> getReductionStrategies(int branchId);
}