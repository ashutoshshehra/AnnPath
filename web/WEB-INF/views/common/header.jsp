<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${currentLocale != null ? currentLocale : 'en'}" scope="session" />
<fmt:setBundle basename="messages" scope="session" />
<!DOCTYPE html>
<html lang="${currentLang != null ? currentLang : 'en'}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0">
    <meta name="theme-color" content="#176B4D">
    <title><c:out value="${pageTitle != null ? pageTitle : 'AnnPath — A Path from Surplus to Need'}" /></title>
    <!-- Favicon & Brand Icons -->
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/images/favicon.png">
    <link rel="apple-touch-icon" href="${pageContext.request.contextPath}/assets/images/symbol.png">
    <!-- Premium Fonts: Poppins (Headings) & Inter (UI/Body) -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Poppins:wght@600;700;800&display=swap" rel="stylesheet">
    <!-- Leaflet Map CSS for Operational Rescue Explorer (Local with CDN fallback) -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/vendor/leaflet/leaflet.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.css">
    <!-- Brand Stylesheet -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <c:if test="${isReceiptPage}">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/print-receipt.css">
    </c:if>
    <style>
        body { top: 0px !important; position: static !important; }
        iframe.skiptranslate, .goog-te-banner-frame, #goog-gt-tt, .goog-te-balloon-frame, .goog-tooltip, .goog-tooltip:hover { display: none !important; }
        .goog-text-highlight { background: none !important; box-shadow: none !important; }
        #google_translate_element { position: absolute; left: -9999px; top: -9999px; width: 1px; height: 1px; overflow: hidden; opacity: 0; pointer-events: none; }
    </style>
</head>
<body>
<div id="google_translate_element" style="position: absolute; left: -9999px; top: -9999px; width: 1px; height: 1px; overflow: hidden; opacity: 0; pointer-events: none;"></div>
