package andreiv.service;

import org.junit.jupiter.api.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public class CityCoordinatesTest {
    private CityCoordinates coordinates;

    @BeforeEach
    void setup() {
        coordinates = new CityCoordinates();
    }

    @Test
    @DisplayName("Should return the coordinates for Bucharest.")
    void testCoordinatesForBucharest() {
        Optional<double[]> result = coordinates.getCoordinates("Bucharest");

        assertTrue(result.isPresent()); // checks for a returned value
        double[] values = result.get();
        assertEquals(44.43225, values[0], 0.001); // checks the latitude value for Bucharest
        assertEquals(26.10626, values[1], 0.001); // checks the longitude value
    }

    @Test
    @DisplayName("Should demonstrate that the coordinates search is case-insensitive.")
    void testCaseInsensitiveAspectOfCoordinatesSearch() {
        Optional<double[]> lowercase = coordinates.getCoordinates("saint-tropez");
        Optional<double[]> normal = coordinates.getCoordinates("Saint-Tropez");
        Optional<double[]> uppercase = coordinates.getCoordinates("SAINT-TROPEZ");

        assertTrue(lowercase.isPresent());
        assertTrue(normal.isPresent());
        assertTrue(uppercase.isPresent());

        double[] lowercaseValues = lowercase.get();
        double[] normalValues = normal.get();
        double[] uppercaseValues = uppercase.get();

        assertArrayEquals(lowercase.get(), normal.get(), 0.001);
        assertArrayEquals(lowercase.get(), uppercase.get(), 0.001);
    }

    @Test
    @DisplayName("Should return the same result for a second lookup on the same city.")
    void testReturnSameResultOnSecondLookup() {
        Optional<double[]> first = coordinates.getCoordinates("Ramatuelle");
        Optional<double[]> second = coordinates.getCoordinates("Ramatuelle");

        assertTrue(first.isPresent());
        assertTrue(second.isPresent());

        assertArrayEquals(first.get(), second.get(), 0.001);
    }

    @Test
    @DisplayName("Should display that the second lookup (an already cached one) is faster than the first.")
    void testCachedSecondLookupDuration() {
        long start1 = System.nanoTime();
        Optional<double[]> first = coordinates.getCoordinates("Sonder");
        long duration1 = System.nanoTime() - start1;

        long start2 = System.nanoTime();
        Optional<double[]> second = coordinates.getCoordinates("Sonder");
        long duration2 = System.nanoTime() - start2;

        System.out.println("File lookup: " + duration1 + " ns");
        System.out.println("Cached lookup: " + duration2 + " ns");

        assertTrue(duration1 > duration2);
    }

    @Test
    @DisplayName("Should return empty for unknown city.")
    void testReturnEmptyForUnknownCity() {
        Optional<double[]> result = coordinates.getCoordinates("ABCDEFGH");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty for an empty string given as city input.")
    void testReturnEmptyForEmptyStringInput() {
        Optional<double[]> result = coordinates.getCoordinates("");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty for a whitespace given as city input.")
    void testReturnEmptyForWhitespaceInput() {
        Optional<double[]> result = coordinates.getCoordinates(" ");

        assertTrue(result.isEmpty());
    }
}
