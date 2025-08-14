import com.cofaktory.footprint.model.CoffeeDistribution;
import com.cofaktory.footprint.model.CoffeeProduction;
import com.cofaktory.footprint.service.EmissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmissionsServiceTest {
    private EmissionService emissionsService;

    @BeforeEach
    public void setup() {
        emissionsService = new EmissionService();
    }

    @Test
    public void testCalculateProductionEmissions() {
        // Create a test production entity
        CoffeeProduction production = new CoffeeProduction();
        production.setProductionQuantitiesKG(100.0);

        // Calculate emissions
        emissionsService.calculateProductionEmissions(production);

        // Verify the result (6.4 * 100 = 640)
        assertEquals(640.0, production.getCarbonEmissionsKG(), 0.01);
    }

    @Test
    public void testCalculateDistributionEmissions() {
        // Create a test distribution entity
        CoffeeDistribution distribution = new CoffeeDistribution();
        distribution.setVehicleType("minivan");
        distribution.setNumberOfVehicles(2);
        distribution.setDistancePerVehicleKM(50.0);

        // Calculate emissions
        emissionsService.calculateDistributionEmissions(distribution);

        // Verify results
        assertEquals(100.0, distribution.getDistancePerVehicleKM()*2, 0.01);
        // Based on formula: 50 * 10 * 2 * 2.68 = 2680
        assertEquals(2680.0, distribution.getCarbonEmissionsKg(), 0.01);
    }
}