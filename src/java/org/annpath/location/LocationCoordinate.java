package org.annpath.location;

import java.io.Serializable;
import java.math.BigDecimal;

public class LocationCoordinate implements Serializable {
    private static final long serialVersionUID = 1L;

    private double latitude;
    private double longitude;

    public LocationCoordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LocationCoordinate(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude != null ? latitude.doubleValue() : 0.0;
        this.longitude = longitude != null ? longitude.doubleValue() : 0.0;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
