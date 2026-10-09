<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<footer class="footer">
    <div class="container">
        <div class="footer-grid">
            <div class="footer-col">
                <a href="${pageContext.request.contextPath}/home" class="brand-logo" style="margin-bottom: 1.25rem;">
                    <div class="brand-symbol-badge">
                        <img src="${pageContext.request.contextPath}/assets/images/symbol.png" alt="AnnPath Symbol" class="brand-symbol-img">
                    </div>
                    <div class="brand-text-block">
                        <span class="brand-name">Ann<span class="brand-name-accent">Path</span></span>
                        <span class="brand-tagline">Surplus to Need</span>
                    </div>
                </a>
                <p style="color: var(--text-muted); font-size: 0.9rem; max-width: 300px; margin-bottom: 1rem;">
                    A Path from Surplus to Need. Connecting food donors with verified NGOs and dedicated volunteers to ensure zero usable food goes to waste.
                </p>
                <div style="font-size: 0.85rem; color: var(--text-light);">
                    Global Food Redistribution Platform
                </div>
            </div>
            <div class="footer-col">
                <h4><c:out value="${not empty msg['footer.roles'] ? msg['footer.roles'] : 'Three Roles'}" /></h4>
                <ul>
                    <li><a href="${pageContext.request.contextPath}/auth/register?role=DONOR"><c:out value="${not empty msg['role.donor'] ? msg['role.donor'] : 'Food Donors'}" /></a></li>
                    <li><a href="${pageContext.request.contextPath}/auth/register?role=NGO"><c:out value="${not empty msg['role.ngo'] ? msg['role.ngo'] : 'Verified NGOs'}" /></a></li>
                    <li><a href="${pageContext.request.contextPath}/auth/register?role=VOLUNTEER"><c:out value="${not empty msg['role.volunteer'] ? msg['role.volunteer'] : 'Volunteers'}" /></a></li>
                    <li><a href="${pageContext.request.contextPath}/home#journey"><c:out value="${not empty msg['nav.rescueJourney'] ? msg['nav.rescueJourney'] : 'Rescue Journey'}" /></a></li>
                </ul>
            </div>
            <div class="footer-col">
                <h4><c:out value="${not empty msg['footer.platform'] ? msg['footer.platform'] : 'Platform'}" /></h4>
                <ul>
                    <li><a href="${pageContext.request.contextPath}/home#smart-matching"><c:out value="${not empty msg['nav.matching'] ? msg['nav.matching'] : 'Smart Matching'}" /></a></li>
                    <li><a href="${pageContext.request.contextPath}/home#urgency"><c:out value="${not empty msg['how.step2.badge'] ? msg['how.step2.badge'] : 'Urgency Engine'}" /></a></li>
                    <li><a href="${pageContext.request.contextPath}/home#impact"><c:out value="${not empty msg['nav.impact'] ? msg['nav.impact'] : 'Verified Impact'}" /></a></li>
                    <li><a href="${pageContext.request.contextPath}/home#safety"><c:out value="${not empty msg['safety.badge'] ? msg['safety.badge'] : 'Trust & Safety'}" /></a></li>
                </ul>
            </div>
            <div class="footer-col">
                <h4><c:out value="${not empty msg['footer.standards'] ? msg['footer.standards'] : 'Standards & Trust'}" /></h4>
                <ul>
                    <li><a href="#"><c:out value="${not empty msg['footer.foodSafety'] ? msg['footer.foodSafety'] : 'Food Safety Standards'}" /></a></li>
                    <li><a href="#"><c:out value="${not empty msg['footer.ngoRules'] ? msg['footer.ngoRules'] : 'NGO Verification Rules'}" /></a></li>
                    <li><a href="#"><c:out value="${not empty msg['footer.privacy'] ? msg['footer.privacy'] : 'Privacy & Audit Trails'}" /></a></li>
                    <li><a href="#"><c:out value="${not empty msg['footer.receipts'] ? msg['footer.receipts'] : 'Digital Rescue Receipts'}" /></a></li>
                </ul>
            </div>
        </div>
        <div class="footer-bottom">
            <div>&copy; 2026 AnnPath. <c:out value="${not empty msg['footer.rights'] ? msg['footer.rights'] : 'All rights reserved. Built for production global food rescue.'}" /></div>
            <div>Strict Role Isolation &bull; Concurrency Handled &bull; Global-First Architecture</div>
        </div>
    </div>
</footer>

<script src="${pageContext.request.contextPath}/assets/js/app.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/location-combobox.js"></script>
<script type="text/javascript">
function applyGoogleTranslation(targetLang) {
    if (!targetLang || targetLang === 'en') return;
    var maxAttempts = 50;
    var attempts = 0;
    var interval = setInterval(function() {
        attempts++;
        var select = document.querySelector('.goog-te-combo');
        if (select) {
            if (select.value !== targetLang) {
                select.value = targetLang;
                select.dispatchEvent(new Event('change'));
            }
            clearInterval(interval);
        }
        if (attempts >= maxAttempts) {
            clearInterval(interval);
        }
    }, 120);
}

function googleTranslateElementInit() {
    new google.translate.TranslateElement({
        pageLanguage: 'en',
        includedLanguages: 'en,hi,es,fr,bn,te,mr,ta,gu,kn,pa',
        autoDisplay: true
    }, 'google_translate_element');
    applyGoogleTranslation('${currentLang}');
}

// Ensure automatic translation on all page navigations
document.addEventListener('DOMContentLoaded', function() {
    applyGoogleTranslation('${currentLang}');
});

function changeAnnPathLanguage(langCode) {
    if (!langCode) return;
    var host = window.location.hostname;
    if (langCode === 'en') {
        document.cookie = 'googtrans=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
        document.cookie = 'googtrans=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=' + host + ';';
        if (host.indexOf('.') !== -1) {
            document.cookie = 'googtrans=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=.' + host + ';';
        }
    } else {
        var cookieVal = '/en/' + langCode;
        document.cookie = 'googtrans=' + cookieVal + '; path=/;';
        document.cookie = 'googtrans=' + cookieVal + '; path=/; domain=' + host + ';';
        if (host.indexOf('.') !== -1) {
            document.cookie = 'googtrans=' + cookieVal + '; path=/; domain=.' + host + ';';
        }
    }
    
    // Trigger in-page Google Translate immediately if widget is present
    try {
        var select = document.querySelector('.goog-te-combo');
        if (select) {
            select.value = langCode;
            select.dispatchEvent(new Event('change'));
        }
    } catch (e) {}
    
    // Navigate to /locale to persist selection across session & database
    window.location.href = '${pageContext.request.contextPath}/locale?lang=' + encodeURIComponent(langCode);
}
</script>
<script type="text/javascript" src="https://translate.google.com/translate_a/element.js?cb=googleTranslateElementInit"></script>
</body>
</html>
