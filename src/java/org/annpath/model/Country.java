package org.annpath.model;

import java.io.Serializable;

public class Country implements Serializable {
    private static final long serialVersionUID = 1L;

    private String countryCode;
    private String countryName;
    private String phoneCode;
    private String defaultCurrency;
    private String defaultTimezone;
    private String distanceUnit;
    private boolean active;

    public Country() {}

    public Country(String countryCode, String countryName, String phoneCode, String defaultCurrency, String defaultTimezone, String distanceUnit) {
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.phoneCode = phoneCode;
        this.defaultCurrency = defaultCurrency;
        this.defaultTimezone = defaultTimezone;
        this.distanceUnit = distanceUnit;
        this.active = true;
    }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }

    public String getPhoneCode() { return phoneCode; }
    public void setPhoneCode(String phoneCode) { this.phoneCode = phoneCode; }

    public String getDefaultCurrency() { return defaultCurrency; }
    public void setDefaultCurrency(String defaultCurrency) { this.defaultCurrency = defaultCurrency; }

    public String getDefaultTimezone() { return defaultTimezone; }
    public void setDefaultTimezone(String defaultTimezone) { this.defaultTimezone = defaultTimezone; }

    public String getDistanceUnit() { return distanceUnit; }
    public void setDistanceUnit(String distanceUnit) { this.distanceUnit = distanceUnit; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
