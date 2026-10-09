<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Donor Profile — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container" style="padding: 2.5rem 1rem; max-width: 820px;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
        <div>
            <h2>Donor Organization Profile</h2>
            <p style="color: var(--text-muted); font-size: 0.95rem;">
                Manage your commercial donor identity, location geofence, and default pickup instructions.
            </p>
        </div>
        <div>
            <a href="${pageContext.request.contextPath}/donor/dashboard" class="btn btn-outline btn-sm">&larr; Back to Dashboard</a>
        </div>
    </div>

    <jsp:include page="../common/alert.jsp" />

    <div class="card" style="padding: 2rem;">
        <form action="${pageContext.request.contextPath}/donor/profile" method="POST">
            <input type="hidden" name="csrfToken" value="${csrfToken}" />

            <div style="margin-bottom: 1.5rem; padding-bottom: 1rem; border-bottom: 1px solid var(--border-color);">
                <h4 style="color: var(--primary); margin-bottom: 0.5rem;">Account Information</h4>
                <div style="font-size: 0.9rem; color: #555;">
                    Registered Email: <strong><c:out value="${user.email}" /></strong> &bull;
                    Role: <span class="badge badge-fresh">FOOD DONOR</span>
                </div>
            </div>

            <h4 style="color: var(--primary); margin-bottom: 1rem;">Organization Details</h4>

            <div class="form-row">
                <div class="form-group">
                    <label class="form-label" for="organizationName">Organization / Business Name *</label>
                    <input type="text" id="organizationName" name="organizationName" class="form-control" required
                           value="<c:out value="${profile.organizationName}" />" placeholder="e.g. Royal Palace Hotel & Catering" />
                </div>
                <div class="form-group">
                    <label class="form-label" for="contactPerson">Contact Person *</label>
                    <input type="text" id="contactPerson" name="contactPerson" class="form-control" required
                           value="<c:out value="${profile.contactPerson}" />" placeholder="Full Name" />
                </div>
            </div>

            <div class="form-group">
                <label class="form-label" for="donorType">Donor Facility Type *</label>
                <select id="donorType" name="donorType" class="form-control" required>
                    <option value="RESTAURANT" ${profile.donorType == 'RESTAURANT' ? 'selected' : ''}>Restaurant / Cafe</option>
                    <option value="HOTEL" ${profile.donorType == 'HOTEL' ? 'selected' : ''}>Hotel / Hospitality</option>
                    <option value="EVENT_CATERER" ${profile.donorType == 'EVENT_CATERER' ? 'selected' : ''}>Event Caterer / Banquet Hall</option>
                    <option value="SUPERMARKET" ${profile.donorType == 'SUPERMARKET' ? 'selected' : ''}>Supermarket / Grocery Retailer</option>
                    <option value="CORPORATE_CAFETERIA" ${profile.donorType == 'CORPORATE_CAFETERIA' ? 'selected' : ''}>Corporate Cafeteria</option>
                    <option value="INDIVIDUAL" ${profile.donorType == 'INDIVIDUAL' ? 'selected' : ''}>Individual / Community Kitchen</option>
                </select>
            </div>

            <h4 style="color: var(--primary); margin: 1.5rem 0 1rem;">Pickup Address & Geofence</h4>

            <div class="form-group">
                <label class="form-label" for="addressLine1">Primary Street Address *</label>
                <input type="text" id="addressLine1" name="addressLine1" class="form-control" required
                       value="<c:out value="${profile.addressLine1}" />" placeholder="Plot No, Street, Landmark" />
            </div>

            <div class="form-group">
                <label class="form-label" for="addressLine2">Address Line 2 (Optional)</label>
                <input type="text" id="addressLine2" name="addressLine2" class="form-control"
                       value="<c:out value="${profile.addressLine2}" />" placeholder="Building, Floor, Suite" />
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

            <!-- Hidden coordinates for backend matching -->
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

            <div class="form-group">
                <label class="form-label" for="defaultPickupInstructions">Standard Pickup Instructions</label>
                <textarea id="defaultPickupInstructions" name="defaultPickupInstructions" class="form-control" rows="3"
                          placeholder="e.g. Please approach rear loading dock B, mention AnnPath food rescue to security gate..."><c:out value="${profile.defaultPickupInstructions}" /></textarea>
            </div>

            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 2rem;">
                <a href="${pageContext.request.contextPath}/donor/dashboard" class="btn btn-outline">Cancel</a>
                <button type="submit" class="btn btn-primary btn-lg">Save Profile Changes</button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
