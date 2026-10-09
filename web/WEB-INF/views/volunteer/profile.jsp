<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Volunteer Profile — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container" style="padding: 2.5rem 1rem; max-width: 820px;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
        <div>
            <h2>Volunteer Rescuer Profile</h2>
            <p style="color: var(--text-muted); font-size: 0.95rem;">
                Manage your personal contact details, vehicle capacity, and operational travel radius.
            </p>
        </div>
        <div>
            <a href="${pageContext.request.contextPath}/volunteer/dashboard" class="btn btn-outline btn-sm">&larr; Back to Dashboard</a>
        </div>
    </div>

    <jsp:include page="../common/alert.jsp" />

    <div class="card" style="padding: 2rem;">
        <form action="${pageContext.request.contextPath}/volunteer/profile" method="POST">
            <input type="hidden" name="csrfToken" value="${csrfToken}" />

            <div style="margin-bottom: 1.5rem; padding-bottom: 1rem; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                <div>
                    <h4 style="color: var(--primary); margin-bottom: 0.3rem;">Account Status</h4>
                    <div style="font-size: 0.9rem; color: #555;">
                        Registered Email: <strong><c:out value="${user.email}" /></strong> &bull;
                        Role: <span class="badge badge-fresh">COMMUNITY VOLUNTEER</span>
                    </div>
                </div>
                <div>
                    <span class="badge ${profile.availabilityStatus == 'AVAILABLE' ? 'badge-fresh' : 'badge-pending'}" style="font-size: 0.95rem; padding: 0.4rem 0.8rem;">
                        Status: <c:out value="${profile.availabilityStatus != null ? profile.availabilityStatus : 'AVAILABLE'}" />
                    </span>
                </div>
            </div>

            <h4 style="color: var(--primary); margin-bottom: 1rem;">Personal Information</h4>

            <div class="form-row">
                <div class="form-group">
                    <label class="form-label" for="firstName">First Name *</label>
                    <input type="text" id="firstName" name="firstName" class="form-control" required
                           value="<c:out value="${profile.firstName}" />" placeholder="First Name" />
                </div>
                <div class="form-group">
                    <label class="form-label" for="lastName">Last Name *</label>
                    <input type="text" id="lastName" name="lastName" class="form-control" required
                           value="<c:out value="${profile.lastName}" />" placeholder="Last Name" />
                </div>
            </div>

            <div class="form-group">
                <label class="form-label" for="emergencyContactPhone">Emergency Contact Phone Number</label>
                <input type="tel" id="emergencyContactPhone" name="emergencyContactPhone" class="form-control"
                       value="<c:out value="${profile.emergencyContactPhone}" />" placeholder="e.g. +91 9876543210" />
            </div>

            <h4 style="color: var(--primary); margin: 1.5rem 0 1rem;">Base Address & Home Geofence</h4>

            <div class="form-group">
                <label class="form-label" for="addressLine1">Residential / Base Address *</label>
                <input type="text" id="addressLine1" name="addressLine1" class="form-control" required
                       value="<c:out value="${profile.addressLine1}" />" placeholder="House / Flat No, Street, Landmark" />
            </div>

            <div class="form-row">
                <div class="form-group annpath-combobox-wrapper" data-combobox-type="country">
                    <label class="form-label" for="countryDisplay">Country *</label>
                    <div class="annpath-combobox-control">
                        <input type="text" id="countryDisplay" class="annpath-combobox-input" placeholder="Search or select Country" autocomplete="off" required />
                        <input type="hidden" id="countryCode" name="countryCode" value="<c:out value="${profile.countryCode != null ? profile.countryCode : 'IN'}" />" />
                        <button type="button" class="annpath-combobox-clear" aria-label="Clear">&times;</button>
                        <button type="button" class="annpath-combobox-arrow" tabindex="-1" aria-label="Open options">
                            <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"></polyline></svg>
                        </button>
                    </div>
                    <div class="annpath-combobox-dropdown"></div>
                </div>

                <div class="form-group annpath-combobox-wrapper" data-combobox-type="state">
                    <label class="form-label" for="stateRegion">State *</label>
                    <div class="annpath-combobox-control">
                        <input type="text" id="stateRegion" name="stateRegion" class="annpath-combobox-input" placeholder="Search or type State" autocomplete="off" required
                               value="<c:out value="${profile.stateRegion}" />" />
                        <button type="button" class="annpath-combobox-clear" aria-label="Clear">&times;</button>
                        <button type="button" class="annpath-combobox-arrow" tabindex="-1" aria-label="Open options">
                            <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"></polyline></svg>
                        </button>
                    </div>
                    <div class="annpath-combobox-dropdown"></div>
                </div>

                <div class="form-group annpath-combobox-wrapper" data-combobox-type="city">
                    <label class="form-label" for="city">City *</label>
                    <div class="annpath-combobox-control">
                        <input type="text" id="city" name="city" class="annpath-combobox-input" placeholder="Search or type City" autocomplete="off" required
                               value="<c:out value="${profile.city}" />" />
                        <button type="button" class="annpath-combobox-clear" aria-label="Clear">&times;</button>
                        <button type="button" class="annpath-combobox-arrow" tabindex="-1" aria-label="Open options">
                            <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"></polyline></svg>
                        </button>
                    </div>
                    <div class="annpath-combobox-dropdown"></div>
                </div>

                <div class="form-group">
                    <label class="form-label" for="postalCode">Postal / ZIP Code *</label>
                    <input type="text" id="postalCode" name="postalCode" class="form-control" required
                           value="<c:out value="${profile.postalCode}" />" placeholder="e.g. 452001" />
                </div>
            </div>

            <!-- Hidden coordinates for volunteer dispatch engine -->
            <input type="hidden" id="latitude" name="latitude" value="<c:out value="${profile.latitude}" />" />
            <input type="hidden" id="longitude" name="longitude" value="<c:out value="${profile.longitude}" />" />

            <!-- Current Location Block -->
            <div style="background: rgba(46, 125, 50, 0.05); border: 1px solid rgba(46, 125, 50, 0.2); border-radius: 10px; padding: 1rem 1.25rem; margin-bottom: 1.5rem; display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 1rem;">
                <div>
                    <strong style="color: var(--primary); display: block; font-size: 0.95rem; margin-bottom: 0.25rem;">
                        📍 <c:out value="${not empty msg['location.current'] ? msg['location.current'] : 'Current Location'}" />
                    </strong>
                    <span id="locationStatus" class="location-status-badge" style="font-size: 0.85rem; color: var(--text-muted);">
                        <c:out value="${not empty msg['location.hint'] ? msg['location.hint'] : 'Click to automatically capture your GPS location and fill address.'}" />
                    </span>
                </div>
                <div>
                    <button type="button" id="btnDetectLocation" class="btn btn-outline btn-detect-location btn-current-location" style="display: inline-flex; align-items: center; gap: 0.4rem; white-space: nowrap;">
                        📍 <c:out value="${not empty msg['location.useCurrent'] ? msg['location.useCurrent'] : 'Use Current Location'}" />
                    </button>
                </div>
            </div>

            <h4 style="color: var(--primary); margin: 1.5rem 0 1rem;">Transport & Dispatch Preferences</h4>

            <div class="form-row">
                <div class="form-group">
                    <label class="form-label" for="vehicleType">Primary Transport Mode *</label>
                    <select id="vehicleType" name="vehicleType" class="form-control" required>
                        <option value="MOTORCYCLE" ${profile.vehicleType == 'MOTORCYCLE' ? 'selected' : ''}>Motorcycle / Scooter (Quick hot meals)</option>
                        <option value="CAR" ${profile.vehicleType == 'CAR' ? 'selected' : ''}>Car / Hatchback / Sedan</option>
                        <option value="VAN" ${profile.vehicleType == 'VAN' ? 'selected' : ''}>Van / Large MPV (Bulk parcels)</option>
                        <option value="TRUCK" ${profile.vehicleType == 'TRUCK' ? 'selected' : ''}>Mini-Truck / Commercial Carrier</option>
                        <option value="BICYCLE" ${profile.vehicleType == 'BICYCLE' ? 'selected' : ''}>Bicycle / E-Bike (Local neighborhood)</option>
                        <option value="NONE_WALKING" ${profile.vehicleType == 'NONE_WALKING' ? 'selected' : ''}>Walking / Foot courier</option>
                    </select>
                </div>
                <div class="form-group">
                    <label class="form-label" for="maxTravelDistanceKm">Maximum Travel Radius (km) *</label>
                    <input type="number" id="maxTravelDistanceKm" name="maxTravelDistanceKm" class="form-control" step="0.5" min="1" max="200" required
                           value="<c:out value="${profile.maxTravelDistanceKm != null ? profile.maxTravelDistanceKm : 15}" />" />
                    <span style="font-size: 0.8rem; color: #666;">The assignment engine matches you with missions within this radius.</span>
                </div>
            </div>

            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 2rem;">
                <a href="${pageContext.request.contextPath}/volunteer/dashboard" class="btn btn-outline">Cancel</a>
                <button type="submit" class="btn btn-primary btn-lg">Save Volunteer Profile</button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
