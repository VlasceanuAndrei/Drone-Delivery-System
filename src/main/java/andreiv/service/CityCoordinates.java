package andreiv.service;

import com.fasterxml.jackson.core.*;
import java.io.File;
import java.util.*;

public final class CityCoordinates {
    private static final String JSON_FILE_PATH = "src/main/resources/cities.json";
    private static final Map<String, double[]> cache = new HashMap<>();

    public static Optional<double[]> getCoordinates(String city) {
        String lowercaseCity = city.toLowerCase();

        if (cache.containsKey(lowercaseCity)) {
            return Optional.of(cache.get(lowercaseCity));
        }

        Optional<double[]> coordinates = searchInFile(lowercaseCity);

        coordinates.ifPresent(c -> cache.put(lowercaseCity, c));

        return coordinates;
    }

    private static Optional<double[]> searchInFile(String city) {
        try (JsonParser parser = new JsonFactory()
                .createParser(new File(JSON_FILE_PATH))) {
            String currentCity = null;
            double latitude = 0, longitude = 0;

            while (parser.nextToken() != null) {
                if (parser.currentToken() == JsonToken.FIELD_NAME) {
                    String field = parser.getCurrentName();
                    parser.nextToken();

                    switch (field) {
                        case "city" -> currentCity = parser.getText().toLowerCase();
                        case "lat" -> latitude = Double.parseDouble(parser.getText());
                        case "lng" -> longitude = Double.parseDouble(parser.getText());
                    }

                    if (currentCity != null && latitude != 0 && longitude != 0) {
                        if (currentCity.equals(city)) {
                            return Optional.of(new double[]{latitude, longitude});
                        }
                        currentCity = null;
                        latitude = longitude = 0;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Couldn't read city coordinates." + e.getMessage());
        }
        return Optional.empty();
    }
}
