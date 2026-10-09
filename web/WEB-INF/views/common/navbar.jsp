<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<header class="navbar">
    <div class="container nav-container">
        <!-- Brand Logo & Emblem -->
        <a href="${pageContext.request.contextPath}/home" class="brand-logo" aria-label="AnnPath Homepage">
            <div class="brand-symbol-badge">
                <img src="${pageContext.request.contextPath}/assets/images/symbol.png" alt="AnnPath Symbol" class="brand-symbol-img">
            </div>
            <div class="brand-text-block">
                <span class="brand-name">Ann<span class="brand-name-accent">Path</span></span>
                <span class="brand-tagline">Surplus to Need</span>
            </div>
        </a>

        <!-- Desktop Navigation Links -->
        <ul class="nav-links">
            <li>
                <a href="${pageContext.request.contextPath}/home" class="nav-link-item">
                    <span class="nav-link-icon">🏠</span>
                    <span><c:out value="${not empty msg['nav.home'] ? msg['nav.home'] : 'Home'}" /></span>
                </a>
            </li>
            <c:choose>
                <c:when test="${sessionScope.userRole == 'DONOR'}">
                    <li>
                        <a href="${pageContext.request.contextPath}/donor/dashboard" class="nav-link-item">
                            <span class="nav-link-icon">📊</span>
                            <span><c:out value="${not empty msg['nav.dashboard'] ? msg['nav.dashboard'] : 'Dashboard'}" /></span>
                        </a>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/donor/create" class="nav-link-item nav-link-highlight">
                            <span class="nav-link-icon">➕</span>
                            <span><c:out value="${not empty msg['btn.donateFood'] ? msg['btn.donateFood'] : 'Donate Food'}" /></span>
                        </a>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/donor/profile" class="nav-link-item">
                            <span class="nav-link-icon">👤</span>
                            <span><c:out value="${not empty msg['dash.viewProfile'] ? msg['dash.viewProfile'] : 'Profile'}" /></span>
                        </a>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/complaint" class="nav-link-item">
                            <span class="nav-link-icon">⚖️</span>
                            <span>Resolution Center</span>
                        </a>
                    </li>
                </c:when>
                <c:when test="${sessionScope.userRole == 'NGO'}">
                    <li>
                        <a href="${pageContext.request.contextPath}/ngo/dashboard" class="nav-link-item">
                            <span class="nav-link-icon">📊</span>
                            <span><c:out value="${not empty msg['nav.dashboard'] ? msg['nav.dashboard'] : 'Dashboard'}" /></span>
                        </a>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/ngo/browse" class="nav-link-item nav-link-highlight">
                            <span class="nav-link-icon">🔍</span>
                            <span><c:out value="${not empty msg['btn.findFood'] ? msg['btn.findFood'] : 'Find Food'}" /></span>
                        </a>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/ngo/requirements" class="nav-link-item">
                            <span class="nav-link-icon">📋</span>
                            <span>Requirements</span>
                        </a>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/ngo/profile" class="nav-link-item">
                            <span class="nav-link-icon">🏢</span>
                            <span>Organization Profile</span>
                        </a>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/complaint" class="nav-link-item">
                            <span class="nav-link-icon">⚖️</span>
                            <span>Resolution Center</span>
                        </a>
                    </li>
                </c:when>
                <c:when test="${sessionScope.userRole == 'VOLUNTEER'}">
                    <li>
                        <a href="${pageContext.request.contextPath}/volunteer/dashboard" class="nav-link-item">
                            <span class="nav-link-icon">📊</span>
                            <span><c:out value="${not empty msg['nav.dashboard'] ? msg['nav.dashboard'] : 'Dashboard'}" /></span>
                        </a>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/volunteer/profile" class="nav-link-item">
                            <span class="nav-link-icon">🚴</span>
                            <span><c:out value="${not empty msg['dash.viewProfile'] ? msg['dash.viewProfile'] : 'Profile'}" /></span>
                        </a>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/complaint" class="nav-link-item">
                            <span class="nav-link-icon">⚖️</span>
                            <span>Resolution Center</span>
                        </a>
                    </li>
                </c:when>
                <c:otherwise>
                    <li>
                        <a href="${pageContext.request.contextPath}/home#how-it-works" class="nav-link-item">
                            <span class="nav-link-icon">💡</span>
                            <span><c:out value="${not empty msg['nav.howItWorks'] ? msg['nav.howItWorks'] : 'How It Works'}" /></span>
                        </a>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/home#journey" class="nav-link-item">
                            <span class="nav-link-icon">🚀</span>
                            <span><c:out value="${not empty msg['nav.rescueJourney'] ? msg['nav.rescueJourney'] : 'Rescue Journey'}" /></span>
                        </a>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/home#smart-matching" class="nav-link-item">
                            <span class="nav-link-icon">🎯</span>
                            <span><c:out value="${not empty msg['nav.matching'] ? msg['nav.matching'] : 'Smart Matching'}" /></span>
                        </a>
                    </li>
                    <li>
                        <a href="${pageContext.request.contextPath}/impact" class="nav-link-item">
                            <span class="nav-link-icon">🌱</span>
                            <span><c:out value="${not empty msg['nav.impact'] ? msg['nav.impact'] : 'Real Impact'}" /></span>
                        </a>
                    </li>
                </c:otherwise>
            </c:choose>
        </ul>

        <!-- Action Items (Language, User Pill, Auth Buttons) -->
        <div class="nav-actions">
            <!-- Language Switcher -->
            <div class="lang-select-wrapper">
                <select id="annPathLangSelect" onchange="changeAnnPathLanguage(this.value)" class="lang-select" aria-label="Select Language">
                    <option value="en" ${currentLang == 'en' ? 'selected' : ''}>🇬🇧 EN</option>
                    <option value="hi" ${currentLang == 'hi' ? 'selected' : ''}>🇮🇳 हिन्दी</option>
                    <option value="es" ${currentLang == 'es' ? 'selected' : ''}>🇪🇸 ES</option>
                    <option value="fr" ${currentLang == 'fr' ? 'selected' : ''}>🇫🇷 FR</option>
                </select>
            </div>

            <c:choose>
                <c:when test="${not empty sessionScope.currentUser}">
                    <c:choose>
                        <c:when test="${sessionScope.userRole == 'DONOR'}">
                            <span class="role-pill donor">🍽️ Donor</span>
                        </c:when>
                        <c:when test="${sessionScope.userRole == 'NGO'}">
                            <span class="role-pill ngo">🏢 NGO</span>
                        </c:when>
                        <c:when test="${sessionScope.userRole == 'VOLUNTEER'}">
                            <span class="role-pill volunteer">🚴 Volunteer</span>
                        </c:when>
                    </c:choose>
                    <a href="${pageContext.request.contextPath}/notifications/list" class="btn-nav-icon" title="Notifications" aria-label="Notifications">
                        🔔
                    </a>
                    <a href="${pageContext.request.contextPath}/auth/logout" class="btn-nav-logout">
                        <c:out value="${not empty msg['nav.logout'] ? msg['nav.logout'] : 'Log Out'}" />
                    </a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/auth/login" class="btn-nav-login">
                        <c:out value="${not empty msg['nav.login'] ? msg['nav.login'] : 'Sign In'}" />
                    </a>
                    <a href="${pageContext.request.contextPath}/auth/register" class="btn-nav-register">
                        <c:out value="${not empty msg['nav.register'] ? msg['nav.register'] : 'Get Started'}" /> &rarr;
                    </a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</header>

<!-- Mobile Thumb-Friendly Bottom Navigation Shell -->
<nav class="mobile-bottom-nav" aria-label="Mobile Navigation">
    <a href="${pageContext.request.contextPath}/home" class="mobile-nav-item">
        <span class="mobile-icon">🏠</span>
        <span>Home</span>
    </a>
    <c:choose>
        <c:when test="${sessionScope.userRole == 'DONOR'}">
            <a href="${pageContext.request.contextPath}/donor/dashboard" class="mobile-nav-item">
                <span class="mobile-icon">📊</span>
                <span>Dashboard</span>
            </a>
            <a href="${pageContext.request.contextPath}/donor/create" class="mobile-nav-item">
                <span class="mobile-icon">➕</span>
                <span>Donate</span>
            </a>
            <a href="${pageContext.request.contextPath}/notifications/list" class="mobile-nav-item">
                <span class="mobile-icon">🔔</span>
                <span>Alerts</span>
            </a>
            <a href="${pageContext.request.contextPath}/donor/profile" class="mobile-nav-item">
                <span class="mobile-icon">👤</span>
                <span>Profile</span>
            </a>
        </c:when>
        <c:when test="${sessionScope.userRole == 'NGO'}">
            <a href="${pageContext.request.contextPath}/ngo/dashboard" class="mobile-nav-item">
                <span class="mobile-icon">📊</span>
                <span>Dashboard</span>
            </a>
            <a href="${pageContext.request.contextPath}/ngo/browse" class="mobile-nav-item">
                <span class="mobile-icon">🔍</span>
                <span>Find Food</span>
            </a>
            <a href="${pageContext.request.contextPath}/notifications/list" class="mobile-nav-item">
                <span class="mobile-icon">🔔</span>
                <span>Alerts</span>
            </a>
            <a href="${pageContext.request.contextPath}/ngo/profile" class="mobile-nav-item">
                <span class="mobile-icon">🏢</span>
                <span>Profile</span>
            </a>
        </c:when>
        <c:when test="${sessionScope.userRole == 'VOLUNTEER'}">
            <a href="${pageContext.request.contextPath}/volunteer/dashboard" class="mobile-nav-item">
                <span class="mobile-icon">📊</span>
                <span>Dashboard</span>
            </a>
            <a href="${pageContext.request.contextPath}/volunteer/profile" class="mobile-nav-item">
                <span class="mobile-icon">👤</span>
                <span>Profile</span>
            </a>
            <a href="${pageContext.request.contextPath}/notifications/list" class="mobile-nav-item">
                <span class="mobile-icon">🔔</span>
                <span>Alerts</span>
            </a>
        </c:when>
        <c:otherwise>
            <a href="${pageContext.request.contextPath}/home#how-it-works" class="mobile-nav-item">
                <span class="mobile-icon">⚡</span>
                <span>Process</span>
            </a>
            <a href="${pageContext.request.contextPath}/impact" class="mobile-nav-item">
                <span class="mobile-icon">📊</span>
                <span>Impact</span>
            </a>
            <a href="${pageContext.request.contextPath}/auth/login" class="mobile-nav-item">
                <span class="mobile-icon">🔑</span>
                <span>Sign In</span>
            </a>
        </c:otherwise>
    </c:choose>
</nav>
