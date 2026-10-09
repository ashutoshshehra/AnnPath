package org.annpath.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class NgoRequirement implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long ngoUserId;
    private String title;
    private Integer foodCategoryId;
    private String dietaryType; // ANY, VEGETARIAN, VEGAN, NON_VEGETARIAN, HALAL, KOSHER
    private BigDecimal minQuantity;
    private BigDecimal maxQuantity;
    private Integer unitId;
    private boolean recurring;
    private String priorityLevel; // LOW, MEDIUM, HIGH, URGENT
    private boolean active;
    private Timestamp createdAt;

    // Joined Presentation Fields
    private String categoryName;
    private String unitName;
    private String ngoName;

    public NgoRequirement() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getNgoUserId() { return ngoUserId; }
    public void setNgoUserId(Long ngoUserId) { this.ngoUserId = ngoUserId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getFoodCategoryId() { return foodCategoryId; }
    public void setFoodCategoryId(Integer foodCategoryId) { this.foodCategoryId = foodCategoryId; }

    public String getDietaryType() { return dietaryType; }
    public void setDietaryType(String dietaryType) { this.dietaryType = dietaryType; }

    public BigDecimal getMinQuantity() { return minQuantity; }
    public void setMinQuantity(BigDecimal minQuantity) { this.minQuantity = minQuantity; }

    public BigDecimal getMaxQuantity() { return maxQuantity; }
    public void setMaxQuantity(BigDecimal maxQuantity) { this.maxQuantity = maxQuantity; }

    public Integer getUnitId() { return unitId; }
    public void setUnitId(Integer unitId) { this.unitId = unitId; }

    public boolean isRecurring() { return recurring; }
    public void setRecurring(boolean recurring) { this.recurring = recurring; }

    public String getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(String priorityLevel) { this.priorityLevel = priorityLevel; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getNgoName() { return ngoName; }
    public void setNgoName(String ngoName) { this.ngoName = ngoName; }
}
