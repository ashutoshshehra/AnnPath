<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="NGO Profile — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container" style="padding: 2.5rem 1rem; max-width: 860px;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
        <div>
            <h2>Non-Profit Relief Profile</h2>
            <p style="color: var(--text-muted); font-size: 0.95rem;">
                Manage your organization identity, representative details, service coverage radius, and daily intake capacity.
            </p>
        </div>
        <div>
            <a href="${pageContext.request.contextPath}/ngo/dashboard" class="btn btn-outline btn-sm">&larr; Back to Dashboard</a>
        </div>
    </div>

    <jsp:include page="../common/alert.jsp" />

    <div class="card" style="padding: 2rem;">
        <form action="${pageContext.request.contextPath}/ngo/profile" method="POST">
            <input type="hidden" name="csrfToken" value="${csrfToken}" />

            <div style="margin-bottom: 1.5rem; padding-bottom: 1rem; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                <div>
                    <h4 style="color: var(--secondary); margin-bottom: 0.3rem;">Account Verification & Status</h4>
                    <div style="font-size: 0.9rem; color: #555;">
                        Registered Email: <strong><c:out value="${user.email}" /></strong> &bull;
                        Role: <span class="badge badge-standard">RELIEF NGO</span>
                    </div>
                </div>
                <div>
                    <span class="badge ${profile.verificationStatus == 'VERIFIED' ? 'badge-fresh' : 'badge-pending'}" style="font-size: 0.95rem; padding: 0.4rem 0.8rem;">
                        <c:out value="${profile.verificationStatus != null ? profile.verificationStatus : 'VERIFIED'}" />
                    </span>
                </div>
            </div>

            <h4 style="color: var(--secondary); margin-bottom: 1rem;">Organization Details</h4>

            <div class="form-row">
                <div class="form-group">
                    <label class="form-label" for="organizationName">Organization Legal Name *</label>
                    <input type="text" id="organizationName" name="organizationName" class="form-control" required
                           value="<c:out value="${profile.organizationName}" />" placeholder="e.g. Hope Food Distribution Foundation" />
                </div>
                <div class="form-group">
                    <label class="form-label" for="establishedYear">Established Year</label>
                    <input type="number" id="establishedYear" name="establishedYear" class="form-control" min="1900" max="2099"
                           value="<c:out value="${profile.establishedYear}" />" placeholder="e.g. 2018" />
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label class="form-label" for="registrationNumber">Registration / Trust Number</label>
                    <input type="text" id="registrationNumber" name="registrationNumber" class="form-control"
                           value="<c:out value="${profile.registrationNumber}" />" placeholder="e.g. REG-NGO-49821" />
                </div>
                <div class="form-group">
                    <label class="form-label" for="registrationAuthority">Registration Authority</label>
                    <input type="text" id="registrationAuthority" name="registrationAuthority" class="form-control"
                           value="<c:out value="${profile.registrationAuthority}" />" placeholder="e.g. Charity Commission / Registrar of Societies" />
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label class="form-label" for="representativeName">Representative / Contact Person *</label>
                    <input type="text" id="representativeName" name="representativeName" class="form-control" required
                           value="<c:out value="${profile.representativeName}" />" placeholder="Full Name" />
                </div>
                <div class="form-group">
                    <label class="form-label" for="representativeDesignation">Designation</label>
                    <input type="text" id="representativeDesignation" name="representativeDesignation" class="form-control"
                           value="<c:out value="${profile.representativeDesignation}" />" placeholder="e.g. Operations Director / Center Lead" />
                </div>
            </div>

            <div class="form-group">
                <label class="form-label" for="website">Official Website / Public Page</label>
                <input type="url" id="website" name="website" class="form-control"
                       value="<c:out value="${profile.website}" />" placeholder="https://www.example-relief.org" />
            </div>

            <h4 style="color: var(--secondary); margin: 1.5rem 0 1rem;">Center Address & Delivery Geofence</h4>

            <div class="form-group">
                <label class="form-label" for="addressLine1">Relief Center Street Address *</label>
                <input type="text" id="addressLine1" name="addressLine1" class="form-control" required
                       value="<c:out value="${profile.addressLine1}" />" placeholder="Building, Street, Landmark" />
            </div>

            <div class="form-group">
                <label class="form-label" for="addressLine2">Address Line 2 (Optional)</label>
                <input type="text" id="addressLine2" name="addressLine2" class="form-control"
                       value="<c:out value="${profile.addressLine2}" />" placeholder="Suite, Facility Wing" />
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

            <!-- Hidden coordinates for backend proximity matching -->
            <input type="hidden" id="latitude" name="latitude" value="<c:out value="${profile.latitude}" />" />
            <input type="hidden" id="longitude" name="longitude" value="<c:out value="${profile.longitude}" />" />

            <!-- Current Location Block -->
            <div style="background: rgba(30, 58, 138, 0.05); border: 1px solid rgba(30, 58, 138, 0.2); border-radius: 10px; padding: 1rem 1.25rem; margin-bottom: 1.5rem; display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 1rem;">
                <div>
                    <strong style="color: var(--secondary); display: block; font-size: 0.95rem; margin-bottom: 0.25rem;">
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

            <h4 style="color: var(--secondary); margin: 1.5rem 0 1rem;">Operations & Capacity Parameters</h4>

            <div class="form-row">
                <div class="form-group">
                    <label class="form-label" for="serviceRadiusKm">Service Radius (km) *</label>
                    <input type="number" id="serviceRadiusKm" name="serviceRadiusKm" class="form-control" step="0.5" min="1" max="500" required
                           value="<c:out value="${profile.serviceRadiusKm != null ? profile.serviceRadiusKm : 50}" />" />
                    <span style="font-size: 0.8rem; color: #666;">Radius used by smart matching engine to find surplus within range.</span>
                </div>
                <div class="form-group">
                    <label class="form-label" for="maxDailyCapacityKg">Max Daily Intake Capacity (kg) *</label>
                    <input type="number" id="maxDailyCapacityKg" name="maxDailyCapacityKg" class="form-control" step="1" min="5" max="50000" required
                           value="<c:out value="${profile.maxDailyCapacityKg != null ? profile.maxDailyCapacityKg : 500}" />" />
                    <span style="font-size: 0.8rem; color: #666;">Maximum quantity of food intake your center can handle daily.</span>
                </div>
            </div>

            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 2rem;">
                <a href="${pageContext.request.contextPath}/ngo/dashboard" class="btn btn-outline">Cancel</a>
                <button type="submit" class="btn btn-secondary btn-lg">Save NGO Profile</button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
