package andreiv.service;

public final class GeoCalculations {
    public static double calculateDistance(double lat1, double long1,
                             double lat2, double long2) {
        final double EARTHS_RADIUS = 6371.0;

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double deltaLat = lat2 - lat1;

        long1 = Math.toRadians(long1);
        long2 = Math.toRadians(long2);
        double deltaLong = long2 - long1;

        double x = Math.pow(Math.sin(deltaLat / 2), 2) +
                    Math.cos(lat1) * Math.cos(lat2) *
                    Math.pow(Math.sin(deltaLong / 2), 2);

        return 2 * EARTHS_RADIUS * Math.asin(Math.sqrt(x));
    }

    static double calculateAngle(double lat1, double long1,
                          double lat2, double long2) {
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        long1 = Math.toRadians(long1);
        long2 = Math.toRadians(long2);
        double deltaLong = long2 - long1;

        double y = Math.sin(deltaLong) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) -
                    Math.sin(lat1) * Math.cos(lat2) *
                    Math.cos(deltaLong);

        double angle = Math.toDegrees(Math.atan2(y, x));
        return (angle + 360) % 360;
    }

    boolean isWithinTolerance(double bearing1, double bearing2, double toleranceThreshold) {
        double diff = Math.abs(((bearing1 - bearing2 + 540) % 360) - 180);
        return diff <= toleranceThreshold;
    }
}
