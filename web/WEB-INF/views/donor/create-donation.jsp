<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Create Food Donation — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container" style="max-width: 880px; padding: 2.5rem 1rem 4rem;">
    <!-- Wizard Header -->
    <div style="text-align: center; margin-bottom: 2rem;">
        <span class="badge badge-fresh" style="margin-bottom: 0.5rem;">New Food Surplus Rescue</span>
        <h2 style="font-size: 2rem; margin: 0 0 0.5rem 0;">Publish Surplus Food Listing</h2>
        <p style="color: var(--text-muted); max-width: 580px; margin: 0 auto;">
            Structured data ensures instant affinity matching with local relief NGOs and swift volunteer dispatch.
        </p>
    </div>

    <jsp:include page="../common/alert.jsp" />

    <!-- 5-Step Stepper Header -->
    <div class="wizard-header">
        <div class="wizard-step-tab active" id="stepTab1" data-step="1">
            <span class="step-num">1</span>
            <span class="step-text">Food Details</span>
        </div>
        <div class="wizard-step-tab" id="stepTab2" data-step="2">
            <span class="step-num">2</span>
            <span class="step-text">Timing</span>
        </div>
        <div class="wizard-step-tab" id="stepTab3" data-step="3">
            <span class="step-num">3</span>
            <span class="step-text">Storage & Safety</span>
        </div>
        <div class="wizard-step-tab" id="stepTab4" data-step="4">
            <span class="step-num">4</span>
            <span class="step-text">Pickup Logistics</span>
        </div>
        <div class="wizard-step-tab" id="stepTab5" data-step="5">
            <span class="step-num">5</span>
            <span class="step-text">Review & Publish</span>
        </div>
    </div>

    <div class="card" style="padding: 2.5rem 2rem; background: #ffffff;">
        <form id="donationWizardForm" action="${pageContext.request.contextPath}/donor/create" method="POST">
            <input type="hidden" name="csrfToken" value="${csrfToken}" />

            <!-- Hidden coordinates for backend proximity engine -->
            <input type="hidden" id="pickupLatitude" name="pickupLatitude"
                   value="<c:out value='${donorProfile != null && donorProfile.latitude != null ? donorProfile.latitude : ""}' />" />
            <input type="hidden" id="pickupLongitude" name="pickupLongitude"
                   value="<c:out value='${donorProfile != null && donorProfile.longitude != null ? donorProfile.longitude : ""}' />" />

            <!-- ================= STEP 1: FOOD DETAILS ================= -->
            <div id="wizardStep1" class="wizard-step active">
                <h3 style="margin-bottom: 0.5rem; color: var(--primary);">01. Food Categorization & Volume</h3>
                <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1.5rem;">
                    Accurately describe the surplus food so matching shelters can plan intake.
                </p>

                <div class="form-group">
                    <label class="form-label" for="title">Food Title / Description Summary *</label>
                    <input type="text" id="title" name="title" class="form-control" required
                           placeholder="e.g. 50 Fresh Vegetable Biryani Portions with Raita" />
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="foodCategoryId">Food Category *</label>
                        <select id="foodCategoryId" name="foodCategoryId" class="form-select" required>
                            <c:forEach var="c" items="${categories}">
                                <option value="${c.id}"><c:out value="${c.categoryName}" /></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="dietaryType">Dietary Classification *</label>
                        <select id="dietaryType" name="dietaryType" class="form-select" required>
                            <option value="VEGETARIAN">Vegetarian (Pure Veg)</option>
                            <option value="VEGAN">Vegan</option>
                            <option value="NON_VEGETARIAN">Non-Vegetarian</option>
                            <option value="HALAL">Halal Certified</option>
                            <option value="KOSHER">Kosher</option>
                            <option value="GLUTEN_FREE">Gluten Free</option>
                        </select>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="totalQuantity">Estimated Quantity *</label>
                        <input type="number" id="totalQuantity" name="totalQuantity" class="form-control" required min="1" step="0.1" placeholder="e.g. 50" />
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="unitId">Measurement Unit *</label>
                        <select id="unitId" name="unitId" class="form-select" required>
                            <c:forEach var="u" items="${units}">
                                <option value="${u.id}"><c:out value="${u.unitName}" /> (<c:out value="${u.unitCode}" />)</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div style="display: flex; justify-content: flex-end; margin-top: 2rem;">
                    <button type="button" class="btn btn-primary" data-wizard-action="next">
                        Continue to Timing &rarr;
                    </button>
                </div>
            </div>

            <!-- ================= STEP 2: TIMING ================= -->
            <div id="wizardStep2" class="wizard-step">
                <h3 style="margin-bottom: 0.5rem; color: var(--primary);">02. Timing & Urgency Windows</h3>
                <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1.5rem;">
                    The Urgency Engine calculates expiry countdowns and rescue priority based on these timestamps.
                </p>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="preparationTime">Preparation Timestamp *</label>
                        <input type="datetime-local" id="preparationTime" name="preparationTime" class="form-control" required />
                        <span style="font-size: 0.8rem; color: var(--text-muted);">When the food was cooked or packed.</span>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="availableFrom">Ready for Pickup From *</label>
                        <input type="datetime-local" id="availableFrom" name="availableFrom" class="form-control" required />
                        <span style="font-size: 0.8rem; color: var(--text-muted);">Earliest pickup window.</span>
                    </div>
                </div>

                <div class="form-group">
                    <label class="form-label" for="availableUntil">Available Until (Expiry Deadline) *</label>
                    <input type="datetime-local" id="availableUntil" name="availableUntil" class="form-control" required />
                    <span style="font-size: 0.8rem; color: #dc2626; font-weight: 600;">
                        ⚠️ Crucial: After this timestamp, food will be marked expired to protect health safety.
                    </span>
                </div>

                <div style="display: flex; justify-content: space-between; margin-top: 2rem;">
                    <button type="button" class="btn btn-outline" data-wizard-action="prev">&larr; Back</button>
                    <button type="button" class="btn btn-primary" data-wizard-action="next">Continue to Safety &rarr;</button>
                </div>
            </div>

            <!-- ================= STEP 3: STORAGE & SAFETY ================= -->
            <div id="wizardStep3" class="wizard-step">
                <h3 style="margin-bottom: 0.5rem; color: var(--primary);">03. Storage Condition & Food Safety</h3>
                <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1.5rem;">
                    Help couriers prepare insulated food boxes, chill bags, or heating carriers.
                </p>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="storageCondition">Storage Condition *</label>
                        <select id="storageCondition" name="storageCondition" class="form-select" required>
                            <option value="WARM_HEATED">Warm / Heated (Fresh Cooked)</option>
                            <option value="ROOM_TEMP">Room Temperature (Dry Goods / Bakery)</option>
                            <option value="REFRIGERATED">Refrigerated (2°C – 5°C)</option>
                            <option value="FROZEN">Deep Frozen (-18°C)</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="packagingType">Packaging Material *</label>
                        <input type="text" id="packagingType" name="packagingType" class="form-control" required
                               placeholder="e.g. Sealed Food-grade Foil Trays, Cardboard Containers" />
                    </div>
                </div>

                <div class="form-group">
                    <label class="form-label" for="description">Allergens, Ingredients & Safe Handling Instructions</label>
                    <textarea id="description" name="description" class="form-control" rows="3"
                              placeholder="e.g. Contains dairy, peanuts, mustard seeds. Keep container upright during transport."></textarea>
                </div>

                <div style="display: flex; justify-content: space-between; margin-top: 2rem;">
                    <button type="button" class="btn btn-outline" data-wizard-action="prev">&larr; Back</button>
                    <button type="button" class="btn btn-primary" data-wizard-action="next">Continue to Pickup Logistics &rarr;</button>
                </div>
            </div>

            <!-- ================= STEP 4: PICKUP LOGISTICS ================= -->
            <div id="wizardStep4" class="wizard-step">
                <h3 style="margin-bottom: 0.5rem; color: var(--primary);">04. Pickup Location & Logistics</h3>
                <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1.5rem;">
                    Exact location allows proximity calculations for nearby couriers and shelters.
                </p>

                <!-- Current Location Action Banner -->
                <div style="background: rgba(23, 107, 77, 0.05); border: 1.5px solid rgba(23, 107, 77, 0.2); border-radius: 12px; padding: 1.25rem; margin-bottom: 1.5rem; display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 1rem;">
                    <div>
                        <strong style="color: var(--primary); display: block; font-size: 1rem; margin-bottom: 0.25rem;">
                            📍 Auto-Detect Current GPS Location
                        </strong>
                        <span id="locationStatus" class="location-status-badge" style="font-size: 0.85rem; color: var(--text-muted);">
                            One-tap GPS auto-fill for precise coordinates and street address.
                        </span>
                    </div>
                    <div>
                        <button type="button" id="btnDetectLocation" class="btn btn-outline btn-detect-location btn-current-location" style="display: inline-flex; align-items: center; gap: 0.4rem; white-space: nowrap;">
                            📍 Use Current Location
                        </button>
                    </div>
                </div>

                <div class="form-group">
                    <label class="form-label" for="pickupAddress">Street Address *</label>
                    <input type="text" id="pickupAddress" name="pickupAddress" class="form-control" required
                           value="<c:out value='${donorProfile != null ? donorProfile.addressLine1 : ""}' />" placeholder="123 Main Street, Kitchen Annex" />
                </div>

                <div class="form-row">
                    <div class="form-group annpath-combobox-wrapper" data-combobox-type="country">
                        <label class="form-label" for="pickupCountryDisplay">Country *</label>
                        <div class="annpath-combobox-control">
                            <input type="text" id="pickupCountryDisplay" class="annpath-combobox-input" placeholder="Search or select Country" autocomplete="off" required />
                            <input type="hidden" id="pickupCountryCode" name="pickupCountryCode" value="<c:out value="${donorProfile != null && not empty donorProfile.countryCode ? donorProfile.countryCode : 'IN'}" />" />
                            <button type="button" class="annpath-combobox-clear" aria-label="Clear">&times;</button>
                            <button type="button" class="annpath-combobox-arrow" tabindex="-1" aria-label="Open options">
                                <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"></polyline></svg>
                            </button>
                        </div>
                        <div class="annpath-combobox-dropdown"></div>
                    </div>

                    <div class="form-group annpath-combobox-wrapper" data-combobox-type="state">
                        <label class="form-label" for="pickupRegion">State / Region *</label>
                        <div class="annpath-combobox-control">
                            <input type="text" id="pickupRegion" name="pickupRegion" class="annpath-combobox-input" placeholder="Search or type State" autocomplete="off" required
                                   value="<c:out value='${donorProfile != null ? donorProfile.stateRegion : ""}' />" />
                            <button type="button" class="annpath-combobox-clear" aria-label="Clear">&times;</button>
                            <button type="button" class="annpath-combobox-arrow" tabindex="-1" aria-label="Open options">
                                <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"></polyline></svg>
                            </button>
                        </div>
                        <div class="annpath-combobox-dropdown"></div>
                    </div>

                    <div class="form-group annpath-combobox-wrapper" data-combobox-type="city">
                        <label class="form-label" for="pickupCity">City *</label>
                        <div class="annpath-combobox-control">
                            <input type="text" id="pickupCity" name="pickupCity" class="annpath-combobox-input" placeholder="Search or type City" autocomplete="off" required
                                   value="<c:out value='${donorProfile != null ? donorProfile.city : ""}' />" />
                            <button type="button" class="annpath-combobox-clear" aria-label="Clear">&times;</button>
                            <button type="button" class="annpath-combobox-arrow" tabindex="-1" aria-label="Open options">
                                <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"></polyline></svg>
                            </button>
                        </div>
                        <div class="annpath-combobox-dropdown"></div>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="pickupPostalCode">Postal Code *</label>
                        <input type="text" id="pickupPostalCode" name="pickupPostalCode" class="form-control" required
                               value="<c:out value='${donorProfile != null ? donorProfile.postalCode : ""}' />" placeholder="Postal Code" />
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="contactName">Pickup Contact Person *</label>
                        <input type="text" id="contactName" name="contactName" class="form-control" required
                               value="<c:out value='${donorProfile != null ? donorProfile.contactPerson : ""}' />" placeholder="Head Chef / Manager" />
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="contactPhone">Contact Phone Number *</label>
                        <input type="tel" id="contactPhone" name="contactPhone" class="form-control" required
                               value="<c:out value='${sessionScope.currentUser.phoneNumber}' />" placeholder="9876543210" />
                    </div>
                </div>

                <div class="form-group">
                    <label class="form-label" for="pickupInstructions">Pickup Instructions for Couriers</label>
                    <textarea id="pickupInstructions" name="pickupInstructions" class="form-control" rows="2"
                              placeholder="e.g. Use rear loading dock. Call contact upon arrival. Ring bell #3."></textarea>
                </div>

                <div style="display: flex; justify-content: space-between; margin-top: 2rem;">
                    <button type="button" class="btn btn-outline" data-wizard-action="prev">&larr; Back</button>
                    <button type="button" class="btn btn-primary" data-wizard-action="next">Proceed to Review &rarr;</button>
                </div>
            </div>

            <!-- ================= STEP 5: REVIEW & PUBLISH ================= -->
            <div id="wizardStep5" class="wizard-step">
                <h3 style="margin-bottom: 0.5rem; color: var(--primary);">05. Review & Confirm Surplus Listing</h3>
                <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1.5rem;">
                    Please verify details before broadcast. Once published, proximity matching begins automatically.
                </p>

                <div style="background: var(--bg-main); border: 1px solid var(--border-color); border-radius: 12px; padding: 1.5rem; margin-bottom: 2rem;">
                    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1.25rem;">
                        <div>
                            <span style="font-size: 0.78rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted);">Food Item</span>
                            <div id="reviewTitle" style="font-size: 1.1rem; font-weight: 700; color: var(--text-main); margin-top: 0.2rem;">—</div>
                            <div id="reviewCategory" style="font-size: 0.85rem; color: var(--primary); font-weight: 600;">—</div>
                        </div>
                        <div>
                            <span style="font-size: 0.78rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted);">Quantity & Classification</span>
                            <div id="reviewQuantity" style="font-size: 1.1rem; font-weight: 700; color: var(--secondary); margin-top: 0.2rem;">—</div>
                            <div id="reviewDietary" style="font-size: 0.85rem; color: var(--text-muted);">—</div>
                        </div>
                        <div>
                            <span style="font-size: 0.78rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted);">Storage & Packaging</span>
                            <div id="reviewStorage" style="font-size: 0.95rem; font-weight: 600; color: var(--text-main); margin-top: 0.2rem;">—</div>
                        </div>
                        <div>
                            <span style="font-size: 0.78rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted);">Expiry Window</span>
                            <div id="reviewUntil" style="font-size: 0.95rem; font-weight: 700; color: #dc2626; margin-top: 0.2rem;">—</div>
                        </div>
                        <div>
                            <span style="font-size: 0.78rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted);">Pickup Address</span>
                            <div id="reviewPickup" style="font-size: 0.9rem; font-weight: 600; color: var(--text-main); margin-top: 0.2rem;">—</div>
                        </div>
                        <div>
                            <span style="font-size: 0.78rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted);">Contact Person</span>
                            <div id="reviewContact" style="font-size: 0.9rem; font-weight: 600; color: var(--text-main); margin-top: 0.2rem;">—</div>
                        </div>
                    </div>
                </div>

                <div style="background: rgba(23, 107, 77, 0.06); border: 1px solid rgba(23, 107, 77, 0.2); border-radius: 8px; padding: 1rem 1.25rem; font-size: 0.85rem; color: var(--primary); margin-bottom: 2rem;">
                    ✓ By publishing, you certify that food was prepared under safe food handling standards and is safe for consumption within the specified availability window.
                </div>

                <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                    <button type="button" class="btn btn-outline" data-wizard-action="prev">&larr; Back to Edit</button>
                    <button type="submit" class="btn btn-primary btn-lg" style="padding: 0.8rem 2.5rem; font-weight: 700;">
                        🚀 Publish Food Donation
                    </button>
                </div>
            </div>
        </form>
    </div>
</div>

<script>
    // Pre-fill sensible default times
    window.addEventListener('DOMContentLoaded', () => {
        const now = new Date();
        const fmt = (d) => {
            const pad = (n) => String(n).padStart(2, '0');
            return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + 'T' + pad(d.getHours()) + ':' + pad(d.getMinutes());
        };
        const prep = document.getElementById('preparationTime');
        const from = document.getElementById('availableFrom');
        const until = document.getElementById('availableUntil');
        if (prep && !prep.value) prep.value = fmt(now);
        if (from && !from.value) from.value = fmt(now);
        if (until && !until.value) {
            const later = new Date(now.getTime() + (24 * 60 * 60 * 1000));
            until.value = fmt(later);
        }
    });
</script>

<jsp:include page="../common/footer.jsp" />
