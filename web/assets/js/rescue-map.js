/**
 * AnnPath — Interactive Operational Rescue Map
 * High-performance surplus & relief route visualizer powered by OpenStreetMap & Leaflet
 * Strict Role Isolation &bull; Concurrency Handled &bull; Zero Framework Dependencies
 */
(function () {
    'use strict';

    let mapInstance = null;
    let mapInitialized = false;

    /**
     * Toggles between Grid View and Map View
     */
    function toggleMapView() {
        const toggleBtn = document.getElementById('btnToggleMapView');
        const gridView = document.getElementById('surplusGridView');
        const mapViewWrapper = document.getElementById('surplusMapViewWrapper');

        if (!toggleBtn || !mapViewWrapper) return;

        const isMapCurrentlyVisible = (mapViewWrapper.style.display !== 'none' && mapViewWrapper.style.display !== '');

        if (isMapCurrentlyVisible) {
            // Switch to Grid View
            mapViewWrapper.style.display = 'none';
            if (gridView) gridView.style.display = '';
            toggleBtn.innerHTML = '🗺️ Switch to Map View';
            toggleBtn.classList.remove('active-view');
        } else {
            // Switch to Map View
            mapViewWrapper.style.display = 'block';
            if (gridView) gridView.style.display = 'none';
            toggleBtn.innerHTML = '📋 Switch to Grid View';
            toggleBtn.classList.add('active-view');

            // Initialize or invalidate map size
            if (!mapInitialized) {
                initRescueMap();
            } else if (mapInstance) {
                setTimeout(() => {
                    mapInstance.invalidateSize();
                }, 100);
                setTimeout(() => {
                    mapInstance.invalidateSize();
                }, 300);
            }

            // Smooth scroll to top of map wrapper
            mapViewWrapper.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }
    }

    /**
     * Initializes the Leaflet map and plots NGO base and live surplus pins
     */
    function initRescueMap() {
        const mapEl = document.getElementById('rescueMap');
        if (!mapEl) return;

        // Check if Leaflet library is available
        if (typeof L === 'undefined') {
            mapEl.innerHTML = `
                <div style="height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 2rem; text-align: center; color: var(--text-muted);">
                    <span style="font-size: 2.5rem; margin-bottom: 0.5rem;">🗺️</span>
                    <strong style="color: var(--text-main); font-size: 1.05rem;">Interactive Map Loading</strong>
                    <p style="font-size: 0.88rem; max-width: 380px; margin: 0.5rem 0 1rem 0;">
                        Leaflet mapping library is loading or network connection is offline.
                    </p>
                    <button type="button" class="btn btn-outline btn-sm" onclick="window.AnnPathMap.initRescueMap()">
                        🔄 Retry Loading Map
                    </button>
                </div>
            `;
            return;
        }

        // Avoid double initialization
        if (mapInitialized && mapInstance) {
            mapInstance.invalidateSize();
            return;
        }

        // Read NGO Base coordinates
        const latAttr = mapEl.getAttribute('data-center-lat');
        const lngAttr = mapEl.getAttribute('data-center-lng');
        const ngoName = mapEl.getAttribute('data-ngo-name') || 'Your NGO Base';

        let ngoLat = parseFloat(latAttr);
        let ngoLng = parseFloat(lngAttr);
        const hasNgoCoords = (!isNaN(ngoLat) && !isNaN(ngoLng) && ngoLat !== 0 && ngoLng !== 0);

        // Read Surplus Items from DOM
        const items = document.querySelectorAll('[data-map-item]');
        const validListings = [];
        const bounds = [];

        items.forEach(item => {
            const lat = parseFloat(item.getAttribute('data-lat'));
            const lng = parseFloat(item.getAttribute('data-lng'));
            if (!isNaN(lat) && !isNaN(lng) && lat !== 0 && lng !== 0) {
                validListings.push({
                    lat: lat,
                    lng: lng,
                    id: item.getAttribute('data-listing-id') || '',
                    title: item.getAttribute('data-title') || 'Food Surplus',
                    urgency: item.getAttribute('data-urgency') || 'FRESH',
                    quantity: item.getAttribute('data-quantity') || '',
                    category: item.getAttribute('data-category') || 'Food',
                    donor: item.getAttribute('data-donor') || 'Donor',
                    city: item.getAttribute('data-city') || '',
                    address: item.getAttribute('data-address') || '',
                    distance: item.getAttribute('data-distance') || ''
                });
                bounds.push([lat, lng]);
            }
        });

        // Determine initial center and zoom level
        let initialCenter = [22.7196, 75.8577]; // Default Indore / Central India
        let initialZoom = 12;

        if (hasNgoCoords) {
            initialCenter = [ngoLat, ngoLng];
            initialZoom = 12;
            bounds.push([ngoLat, ngoLng]);
        } else if (validListings.length > 0) {
            initialCenter = [validListings[0].lat, validListings[0].lng];
            initialZoom = 12;
        } else {
            initialCenter = [20.5937, 78.9629]; // All India
            initialZoom = 5;
        }

        try {
            // Clean container if it has error/loading placeholder
            mapEl.innerHTML = '';

            mapInstance = L.map(mapEl, {
                zoomControl: true,
                scrollWheelZoom: true
            }).setView(initialCenter, initialZoom);

            // OpenStreetMap tile layer with high-res attribution
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19,
                attribution: '&copy; <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noopener">OpenStreetMap</a> contributors | AnnPath Operational Map'
            }).addTo(mapInstance);

            // 1. Plot NGO Base Marker if coordinates exist
            if (hasNgoCoords) {
                const ngoIcon = L.divIcon({
                    className: 'ngo-base-pin',
                    html: `
                        <div style="background: #2563eb; width: 34px; height: 34px; border-radius: 50%; border: 3px solid #ffffff; box-shadow: 0 3px 10px rgba(0,0,0,0.35); display: flex; align-items: center; justify-content: center; color: #ffffff; font-size: 16px; cursor: pointer;">
                            🏢
                        </div>
                    `,
                    iconSize: [34, 34],
                    iconAnchor: [17, 17],
                    popupAnchor: [0, -18]
                });

                const ngoMarker = L.marker([ngoLat, ngoLng], { icon: ngoIcon }).addTo(mapInstance);
                ngoMarker.bindPopup(`
                    <div style="font-family: inherit; padding: 4px 6px; min-width: 180px;">
                        <strong style="color: #2563eb; font-size: 1rem; display: block; margin-bottom: 3px;">🏢 ${escapeHtml(ngoName)}</strong>
                        <span style="font-size: 0.8rem; color: #64748b; font-weight: 500;">Your Registered NGO Facility Base</span>
                    </div>
                `);
            }

            // 2. Plot Surplus Food Listings
            validListings.forEach(item => {
                let pinColor = '#10b981'; // FRESH: Emerald green
                let badgeBg = '#ecfdf5';
                let badgeText = '#065f46';
                let emoji = '🍲';

                if (item.urgency === 'CRITICAL') {
                    pinColor = '#ef4444'; // CRITICAL: Red
                    badgeBg = '#fef2f2';
                    badgeText = '#991b1b';
                    emoji = '🚨';
                } else if (item.urgency === 'EXPIRING_SOON') {
                    pinColor = '#f59e0b'; // EXPIRING_SOON: Amber
                    badgeBg = '#fffbeb';
                    badgeText = '#92400e';
                    emoji = '⏱️';
                }

                // Category-specific emoji
                if (item.category.includes('Baked') || item.category.includes('Bread')) {
                    emoji = '🥖';
                } else if (item.category.includes('Fruit') || item.category.includes('Produce')) {
                    emoji = '🍎';
                } else if (item.category.includes('Dairy')) {
                    emoji = '🥛';
                }

                const foodPinIcon = L.divIcon({
                    className: 'surplus-food-pin',
                    html: `
                        <div style="background: ${pinColor}; width: 30px; height: 30px; border-radius: 50%; border: 3px solid #ffffff; box-shadow: 0 3px 8px rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; color: #ffffff; font-size: 14px; cursor: pointer; transition: transform 0.2s;"
                             onmouseover="this.style.transform='scale(1.2)';"
                             onmouseout="this.style.transform='scale(1)';"
                             title="${escapeHtml(item.title)}">
                            ${emoji}
                        </div>
                    `,
                    iconSize: [30, 30],
                    iconAnchor: [15, 15],
                    popupAnchor: [0, -16]
                });

                const popupContent = `
                    <div style="font-family: inherit; min-width: 220px; max-width: 280px; padding: 4px;">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
                            <span style="font-size: 0.72rem; font-weight: 800; padding: 2px 7px; border-radius: 9999px; background: ${badgeBg}; color: ${badgeText};">${item.urgency}</span>
                            ${item.distance ? `<span style="font-size: 0.75rem; color: #64748b; font-weight: 600;">📏 ${escapeHtml(item.distance)} km</span>` : ''}
                        </div>
                        <h4 style="margin: 0 0 6px 0; font-size: 0.98rem; font-weight: 700; color: #176B4D; line-height: 1.3;">${escapeHtml(item.title)}</h4>
                        <div style="font-size: 0.82rem; color: #334155; margin-bottom: 4px;">
                            <strong>📦 Quantity:</strong> ${escapeHtml(item.quantity)}
                        </div>
                        <div style="font-size: 0.82rem; color: #334155; margin-bottom: 4px;">
                            <strong>🥗 Category:</strong> ${escapeHtml(item.category)}
                        </div>
                        <div style="font-size: 0.82rem; color: #334155; margin-bottom: 8px;">
                            <strong>🏨 Donor:</strong> ${escapeHtml(item.donor)} ${item.city ? '(' + escapeHtml(item.city) + ')' : ''}
                        </div>
                        ${item.id ? `
                            <button type="button" onclick="window.AnnPathMap.focusListing('${escapeHtml(item.id)}')"
                                    style="width: 100%; text-align: center; padding: 6px 12px; font-size: 0.82rem; font-weight: 700; border-radius: 6px; cursor: pointer; display: block; border: none; background: #F28C28; color: #ffffff; box-shadow: 0 2px 5px rgba(242, 140, 40, 0.4); transition: background 0.2s;">
                                📋 View & Request Batch &rarr;
                            </button>
                        ` : ''}
                    </div>
                `;

                const marker = L.marker([item.lat, item.lng], { icon: foodPinIcon }).addTo(mapInstance);
                marker.bindPopup(popupContent);
            });

            // Adjust view to fit all pins
            if (bounds.length > 1) {
                mapInstance.fitBounds(bounds, { padding: [50, 50], maxZoom: 14 });
            } else if (bounds.length === 1) {
                mapInstance.setView(bounds[0], 14);
            }

            mapInitialized = true;

            // Invalidate size to ensure clean tile rendering
            setTimeout(() => {
                if (mapInstance) mapInstance.invalidateSize();
            }, 150);
            setTimeout(() => {
                if (mapInstance) mapInstance.invalidateSize();
            }, 400);

        } catch (err) {
            console.error('AnnPath Map Initialization Error:', err);
        }
    }

    /**
     * Focuses on a specific listing card in Grid View with smooth scrolling & highlight glow
     */
    function focusListing(listingId) {
        if (!listingId) return;

        // 1. Ensure we are in Grid View
        const mapViewWrapper = document.getElementById('surplusMapViewWrapper');
        if (mapViewWrapper && mapViewWrapper.style.display !== 'none') {
            toggleMapView();
        }

        // 2. Locate the listing card in DOM
        setTimeout(() => {
            const card = document.getElementById('listing-' + listingId);
            if (card) {
                card.scrollIntoView({ behavior: 'smooth', block: 'center' });
                card.classList.add('card-highlight-glow');
                setTimeout(() => {
                    card.classList.remove('card-highlight-glow');
                }, 3000);
            }
        }, 120);
    }

    /**
     * Helper to escape HTML to prevent XSS
     */
    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    // Expose AnnPathMap to global scope for button onclick and popup handlers
    window.AnnPathMap = {
        toggleView: toggleMapView,
        initRescueMap: initRescueMap,
        focusListing: focusListing,
        getMapInstance: function () { return mapInstance; }
    };

    /**
     * Wire up event listeners immediately on script execution and DOM ready
     */
    function attachListeners() {
        const toggleBtn = document.getElementById('btnToggleMapView');
        if (toggleBtn && !toggleBtn.dataset.bound) {
            toggleBtn.dataset.bound = 'true';
            toggleBtn.addEventListener('click', function (e) {
                e.preventDefault();
                toggleMapView();
            });
        }

        // Invalidate map on window resize if map is open
        window.addEventListener('resize', function () {
            if (mapInstance && mapInitialized) {
                const mapViewWrapper = document.getElementById('surplusMapViewWrapper');
                if (mapViewWrapper && mapViewWrapper.style.display !== 'none') {
                    mapInstance.invalidateSize();
                }
            }
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', attachListeners);
    } else {
        attachListeners();
    }

})();
