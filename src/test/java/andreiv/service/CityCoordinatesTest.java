package andreiv.service;

import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class CityCoordinatesTest {

    @Test
    @DisplayName("Should return the coordinates for Bucharest.")
    void testCoordinatesForBucharest() {
        Optional<List<Double>> result = CityCoordinates.getCoordinates("Bucharest");

        assertTrue(result.isPresent()); // checks for a returned value
        List<Double> values = result.get();
        assertEquals(44.43225, values.getFirst(), 0.001); // checks the latitude value for Bucharest
        assertEquals(26.10626, values.getLast(), 0.001); // checks the longitude value
    }

    @Test
    @DisplayName("Should demonstrate that the coordinates search is case-insensitive.")
    void testCaseInsensitiveAspectOfCoordinatesSearch() {
        Optional<List<Double>> lowercase = CityCoordinates.getCoordinates("saint-tropez");
        Optional<List<Double>> normal = CityCoordinates.getCoordinates("Saint-Tropez");
        Optional<List<Double>> uppercase = CityCoordinates.getCoordinates("SAINT-TROPEZ");

        assertTrue(lowercase.isPresent());
        assertTrue(normal.isPresent());
        assertTrue(uppercase.isPresent());

        List<Double> lowercaseValues = lowercase.get();
        List<Double> normalValues = normal.get();
        List<Double> uppercaseValues = uppercase.get();

        assertCoordinatesEqual(lowercaseValues, normalValues, 0.001);
        assertCoordinatesEqual(lowercaseValues, uppercaseValues, 0.001);
    }

    @Test
    @DisplayName("Should return the same result for a second lookup on the same city.")
    void testReturnSameResultOnSecondLookup() {
        Optional<List<Double>> first = CityCoordinates.getCoordinates("Ramatuelle");
        Optional<List<Double>> second = CityCoordinates.getCoordinates("Ramatuelle");

        assertTrue(first.isPresent());
        assertTrue(second.isPresent());

        assertCoordinatesEqual(first.get(), second.get(), 0.001);
    }

    @Test
    @DisplayName("Should display that the second lookup (an already cached one) is faster than the first.")
    void testCachedSecondLookupDuration() {
        long start1 = System.nanoTime();
        Optional<List<Double>> first = CityCoordinates.getCoordinates("Sonder");
        long duration1 = System.nanoTime() - start1;

        long start2 = System.nanoTime();
        Optional<List<Double>> second = CityCoordinates.getCoordinates("Sonder");
        long duration2 = System.nanoTime() - start2;

        System.out.println("File lookup: " + duration1 + " ns");
        System.out.println("Cached lookup: " + duration2 + " ns");

        assertTrue(duration1 > duration2);
    }

    @Test
    @DisplayName("Should return empty for unknown city.")
    void testReturnEmptyForUnknownCity() {
        Optional<List<Double>> result = CityCoordinates.getCoordinates("ABCDEFGH");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty for an empty string given as city input.")
    void testReturnEmptyForEmptyStringInput() {
        Optional<List<Double>> result = CityCoordinates.getCoordinates("");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty for a whitespace given as city input.")
    void testReturnEmptyForWhitespaceInput() {
        Optional<List<Double>> result = CityCoordinates.getCoordinates(" ");

        assertTrue(result.isEmpty());
    }

    private static void assertCoordinatesEqual(List<Double> a, List<Double> b, double delta) {
        assertEquals(a.size(), b.size(), "Coordinate lists should have the same length");
        for (int i = 0; i < a.size(); i++) {
            assertEquals(a.get(i), b.get(i), delta, "Coordinate at index " + i);
        }
    }
}
