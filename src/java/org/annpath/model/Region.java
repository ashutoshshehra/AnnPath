package org.annpath.model;

import java.io.Serializable;

public class Region implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String countryCode;
    private String regionCode;
    private String regionName;

    public Region() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }

    public String getRegionName() { return regionName; }
    public void setRegionName(String regionName) { this.regionName = regionName; }
}
