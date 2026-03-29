package andreiv.service;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class GeoCalculationsTest {
    private GeoCalculations calculations;

    @BeforeEach
    void setup() {
        calculations = new GeoCalculations();
    }

    @Test
    @DisplayName("Should return 0km for the same coordinates.")
    void testReturnZeroForSameCoordinates() {
        double distance = calculations.calculateDistance(43.55135, 7.01275, 43.55135, 7.01275);

        assertEquals(0.0, distance, 0.001);
    }

    @Test
    @DisplayName("Should return the correct distance between 2 known cities.")
    void testReturnDistanceBetweenTwoKnownCities() {
        double distance = calculations.calculateDistance(43.63292, 6.99911, 43.57803, 7.05451); // computes the distance between Valbonne and Vallauris
        System.out.println("The computed distance between Valbonne and Vallauris is " + distance + "km.");

        assertEquals(7.6, distance, 0.5);
    }

    @Test
    @DisplayName("The distance between 2 cities should be symmetric. A->B = B->A")
    void testSymmetricDistanceBetweenSameCities() {
        // the distance from Tamsweg to Vienna should be the same as the distance from Vienna to Tamsweg
        double first = calculations.calculateDistance(47.12808, 13.81102, 48.20849, 16.37208);
        double second = calculations.calculateDistance(48.20849, 16.37208, 47.12808, 13.81102);

        assertEquals(first, second, 0.001);
    }

    @Test
    @DisplayName("The angle between the same coordinates should be 0.")
    void testAngleIsZeroForTheSameCoordinates() {
        double angle = calculations.calculateAngle(44.43225, 26.10626, 44.43225, 26.10626);

        assertEquals(0.0, angle, 0.001);
    }

    @Test
    @DisplayName("The angle between 2 cities should be always positive.")
    void testAngleIsPositive() {
        double angle = calculations.calculateAngle(45.64861, 25.60613, 45.8, 24.15);

        assertTrue(angle >= 0);
    }

    @Test
    @DisplayName("Should return opposite angles for a reversed route.")
    void testReturnOppositeAnglesForReversedRoute() {
        double angle1 = calculations.calculateAngle(46.49937, 9.84327, 46.47215, 7.28685);
        double angle2 = calculations.calculateAngle(46.47215, 7.28685, 46.49937, 9.84327);

        double expected = (angle1 + 180) % 360;
        assertEquals(expected, angle2, 5.0);
    }

    @Test
    @DisplayName("Should return true when the angle difference is within threshold value.")
    void testReturnTrueWhenDifferenceIsWithinThreshold() {
        boolean result = calculations.isWithinTolerance(33.0, 42.0, 15.0);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when the angle difference isn't within threshold value.")
    void testReturnFalseWhenDifferenceExceedsThreshold() {
        boolean result = calculations.isWithinTolerance(65.0, 32.0, 32.0);

        assertFalse(result);
    }
}