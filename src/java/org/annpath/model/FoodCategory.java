package org.annpath.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class FoodCategory implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String categoryCode;
    private String categoryName;
    private boolean perishable;
    private int defaultShelfLifeHours;
    private String defaultStorageCondition;
    private BigDecimal urgencyWeight;

    public FoodCategory() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public boolean isPerishable() { return perishable; }
    public void setPerishable(boolean perishable) { this.perishable = perishable; }

    public int getDefaultShelfLifeHours() { return defaultShelfLifeHours; }
    public void setDefaultShelfLifeHours(int defaultShelfLifeHours) { this.defaultShelfLifeHours = defaultShelfLifeHours; }

    public String getDefaultStorageCondition() { return defaultStorageCondition; }
    public void setDefaultStorageCondition(String defaultStorageCondition) { this.defaultStorageCondition = defaultStorageCondition; }

    public BigDecimal getUrgencyWeight() { return urgencyWeight; }
    public void setUrgencyWeight(BigDecimal urgencyWeight) { this.urgencyWeight = urgencyWeight; }
}
