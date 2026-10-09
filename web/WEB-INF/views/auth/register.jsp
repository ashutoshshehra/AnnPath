<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="Register Account — AnnPath" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container" style="max-width: 580px; padding: 3.5rem 1rem;">
    <div class="card" style="padding: 2.5rem 2rem;">
        <div style="text-align: center; margin-bottom: 2rem;">
            <a href="${pageContext.request.contextPath}/home" style="display: inline-block; margin-bottom: 1rem;">
                <img src="${pageContext.request.contextPath}/assets/images/symbol.png" alt="AnnPath Symbol" style="height: 64px; object-fit: contain;">
            </a>
            <h2><c:out value="${not empty msg['reg.title'] ? msg['reg.title'] : 'Join AnnPath'}" /></h2>
            <p style="color: var(--text-muted); font-size: 0.95rem;"><c:out value="${not empty msg['reg.subtitle'] ? msg['reg.subtitle'] : 'Select your role to start moving surplus to need'}" /></p>
        </div>

        <jsp:include page="../common/alert.jsp" />

        <form action="${pageContext.request.contextPath}/auth/register" method="POST">
            <input type="hidden" name="csrfToken" value="${csrfToken}" />

            <!-- Role Selector -->
            <div class="form-group">
                <label class="form-label"><c:out value="${not empty msg['reg.roleSelect'] ? msg['reg.roleSelect'] : 'I am joining as a:'}" /></label>
                <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 0.5rem;">
                    <label style="border: 1px solid var(--border-color); padding: 0.75rem; border-radius: 8px; text-align: center; cursor: pointer; font-size: 0.85rem; font-weight: 600;">
                        <input type="radio" name="role" value="DONOR" ${param.role == 'DONOR' || empty param.role ? 'checked' : ''} />
                        <div><c:out value="${not empty msg['role.donor'] ? msg['role.donor'] : 'Food Donor'}" /></div>
                    </label>
                    <label style="border: 1px solid var(--border-color); padding: 0.75rem; border-radius: 8px; text-align: center; cursor: pointer; font-size: 0.85rem; font-weight: 600;">
                        <input type="radio" name="role" value="NGO" ${param.role == 'NGO' ? 'checked' : ''} />
                        <div><c:out value="${not empty msg['role.ngo'] ? msg['role.ngo'] : 'Verified NGO'}" /></div>
                    </label>
                    <label style="border: 1px solid var(--border-color); padding: 0.75rem; border-radius: 8px; text-align: center; cursor: pointer; font-size: 0.85rem; font-weight: 600;">
                        <input type="radio" name="role" value="VOLUNTEER" ${param.role == 'VOLUNTEER' ? 'checked' : ''} />
                        <div><c:out value="${not empty msg['role.volunteer'] ? msg['role.volunteer'] : 'Volunteer'}" /></div>
                    </label>
                </div>
            </div>

            <div class="form-group">
                <label class="form-label" for="email"><c:out value="${not empty msg['reg.officialEmail'] ? msg['reg.officialEmail'] : 'Official / Contact Email'}" /></label>
                <input type="email" id="email" name="email" class="form-control" required placeholder="contact@organization.org" />
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label class="form-label" for="phoneCountryCode"><c:out value="${not empty msg['reg.phoneCode'] ? msg['reg.phoneCode'] : 'Phone Code'}" /></label>
                    <select id="phoneCountryCode" name="phoneCountryCode" class="form-select">
                        <c:forEach var="c" items="${countries}">
                            <option value="${c.phoneCode}">${c.countryName} (${c.phoneCode})</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group" style="flex: 2;">
                    <label class="form-label" for="phone"><c:out value="${not empty msg['reg.mobileNumber'] ? msg['reg.mobileNumber'] : 'Mobile / Phone Number'}" /></label>
                    <input type="tel" id="phone" name="phone" class="form-control" required placeholder="9876543210" />
                </div>
            </div>

            <div class="form-group">
                <label class="form-label" for="password"><c:out value="${not empty msg['form.password'] ? msg['form.password'] : 'Password'}" /></label>
                <input type="password" id="password" name="password" class="form-control" required minlength="8" placeholder="••••••••" />
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label class="form-label" for="preferredLanguage">Preferred Language</label>
                    <select id="preferredLanguage" name="preferredLanguage" class="form-select">
                        <option value="en" ${currentLang == 'en' ? 'selected' : ''}>English</option>
                        <option value="hi" ${currentLang == 'hi' ? 'selected' : ''}>हिन्दी (Hindi)</option>
                        <option value="es" ${currentLang == 'es' ? 'selected' : ''}>Español (Spanish)</option>
                        <option value="fr" ${currentLang == 'fr' ? 'selected' : ''}>Français (French)</option>
                    </select>
                </div>
                <div class="form-group">
                    <label class="form-label" for="preferredTimezone">Timezone</label>
                    <select id="preferredTimezone" name="preferredTimezone" class="form-select">
                        <option value="Asia/Kolkata">Asia/Kolkata (+05:30)</option>
                        <option value="America/New_York">America/New_York (-05:00)</option>
                        <option value="Europe/London">Europe/London (GMT/UTC)</option>
                        <option value="UTC">UTC Standard</option>
                    </select>
                </div>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 0.5rem;"><c:out value="${not empty msg['btn.confirm'] ? msg['btn.confirm'] : 'Create Account'}" /> &rarr;</button>
        </form>

        <div style="text-align: center; margin-top: 1.5rem; font-size: 0.9rem; color: var(--text-muted);">
            <c:out value="${not empty msg['reg.haveAccount'] ? msg['reg.haveAccount'] : 'Already have an account?'}" /> <a href="${pageContext.request.contextPath}/auth/login"><c:out value="${not empty msg['nav.login'] ? msg['nav.login'] : 'Log In'}" /></a>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
