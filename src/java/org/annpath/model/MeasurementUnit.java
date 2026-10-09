package org.annpath.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class MeasurementUnit implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String unitCode;
    private String unitName;
    private String unitType; // WEIGHT, VOLUME, COUNT
    private BigDecimal conversionFactorToKg;

    public MeasurementUnit() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUnitCode() { return unitCode; }
    public void setUnitCode(String unitCode) { this.unitCode = unitCode; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getUnitType() { return unitType; }
    public void setUnitType(String unitType) { this.unitType = unitType; }

    public BigDecimal getConversionFactorToKg() { return conversionFactorToKg; }
    public void setConversionFactorToKg(BigDecimal conversionFactorToKg) { this.conversionFactorToKg = conversionFactorToKg; }
}
