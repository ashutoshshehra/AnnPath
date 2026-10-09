/**
 * AnnPath — Rescue Intelligence Platform Client Scripts
 * Enterprise Client-Side Controllers & Real-Time Components
 */
document.addEventListener('DOMContentLoaded', () => {

    /* ====================================================================
       1. TAB SWITCHING CONTROLLER
       ==================================================================== */
    const initTabs = () => {
        const tabButtons = document.querySelectorAll('.tab-btn');
        tabButtons.forEach(button => {
            button.addEventListener('click', () => {
                const targetId = button.getAttribute('data-tab');
                const tabContainer = button.closest('.dashboard-wrapper') || button.closest('.tabs-parent') || document;

                const parentTabGroup = button.closest('.dashboard-tabs') || button.closest('.tabs');
                if (parentTabGroup) {
                    parentTabGroup.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
                }
                button.classList.add('active');

                const tabPanes = tabContainer.querySelectorAll('.tab-pane');
                tabPanes.forEach(pane => {
                    if (pane.id === targetId) {
                        pane.style.display = 'block';
                    } else {
                        pane.style.display = 'none';
                    }
                });
            });
        });
    };
    initTabs();

    /* ====================================================================
       2. LIVE RESCUE CLOCK COUNTDOWN TICKER
       ==================================================================== */
    const initRescueClocks = () => {
        const clocks = document.querySelectorAll('[data-expires-at]');
        if (clocks.length === 0) return;

        const updateAllClocks = () => {
            const now = new Date().getTime();
            clocks.forEach(clock => {
                const targetStr = clock.getAttribute('data-expires-at');
                if (!targetStr) return;

                // Parse server/DB timestamp safely
                let targetDate = new Date(targetStr.replace(' ', 'T')).getTime();
                if (isNaN(targetDate)) {
                    targetDate = new Date(targetStr).getTime();
                }
                if (isNaN(targetDate)) return;

                const diffMs = targetDate - now;

                // Format elements
                const displayEl = clock.querySelector('.clock-display') || clock;

                if (diffMs <= 0) {
                    clock.className = 'rescue-clock clock-expired';
                    displayEl.innerText = '⚠️ EXPIRED';
                    return;
                }

                const totalSeconds = Math.floor(diffMs / 1000);
                const hours = Math.floor(totalSeconds / 3600);
                const minutes = Math.floor((totalSeconds % 3600) / 60);
                const seconds = totalSeconds % 60;

                const pad = (n) => String(n).padStart(2, '0');
                const formatted = `${pad(hours)}h ${pad(minutes)}m ${pad(seconds)}s`;

                // Update text
                displayEl.innerText = `⏱ ${formatted} remaining`;

                // Update urgency state dynamically
                if (hours < 2) {
                    clock.className = 'rescue-clock clock-critical';
                } else if (hours < 6) {
                    clock.className = 'rescue-clock clock-expiring';
                } else {
                    clock.className = 'rescue-clock clock-normal';
                }
            });
        };

        updateAllClocks();
        setInterval(updateAllClocks, 1000);
    };
    initRescueClocks();

    /* ====================================================================
       3. CURRENT LOCATION & GEOCODING ENGINE
       ==================================================================== */
    const initCurrentLocationEngine = () => {
        const geoButtons = document.querySelectorAll('#btnDetectLocation, .btn-detect-location, [data-action="detect-current-location"]');
        geoButtons.forEach(geoBtn => {
            if (geoBtn.dataset.geoAttached) return;
            geoBtn.dataset.geoAttached = 'true';
            geoBtn.addEventListener('click', (e) => {
                e.preventDefault();
                const form = geoBtn.closest('form') || document;
                const statusBadge = form.querySelector('#locationStatus') || form.querySelector('.location-status-badge');

                if (!navigator.geolocation) {
                    alert('Geolocation is not supported by your current browser.');
                    return;
                }

                const originalBtnText = geoBtn.innerText;
                geoBtn.disabled = true;
                geoBtn.innerText = '📍 Detecting Location...';
                if (statusBadge) {
                    statusBadge.innerText = '⏳ Acquiring live GPS signal...';
                    statusBadge.style.color = 'var(--primary)';
                }

                navigator.geolocation.getCurrentPosition(
                    async (position) => {
                        const lat = position.coords.latitude.toFixed(7);
                        const lng = position.coords.longitude.toFixed(7);

                        if (window.AnnPathLocation && window.AnnPathLocation.reverseGeocodeAndFill) {
                            await window.AnnPathLocation.reverseGeocodeAndFill(lat, lng, form, statusBadge);
                        } else {
                            const latInput = form.querySelector('#pickupLatitude') || form.querySelector('#latitude');
                            const lngInput = form.querySelector('#pickupLongitude') || form.querySelector('#longitude');
                            if (latInput) latInput.value = lat;
                            if (lngInput) lngInput.value = lng;
                        }

                        geoBtn.disabled = false;
                        geoBtn.innerText = '✓ Location Captured';
                        geoBtn.classList.remove('btn-outline');
                        geoBtn.classList.add('btn-primary');

                        setTimeout(() => {
                            geoBtn.innerText = originalBtnText;
                            geoBtn.classList.remove('btn-primary');
                            geoBtn.classList.add('btn-outline');
                        }, 4000);
                    },
                    (error) => {
                        geoBtn.disabled = false;
                        geoBtn.innerText = originalBtnText;
                        if (statusBadge) {
                            statusBadge.innerText = '⚠️ Could not detect GPS. Please enter street address.';
                            statusBadge.style.color = '#ef4444';
                        }
                        alert('Location access was denied or unavailable. Please enter your address details manually.');
                    },
                    { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
                );
            });
        });
    };
    initCurrentLocationEngine();

    /* ====================================================================
       4. PROGRESSIVE 5-STEP DONATION WIZARD
       ==================================================================== */
    const initDonationWizard = () => {
        const wizardForm = document.getElementById('donationWizardForm');
        if (!wizardForm) return;

        let currentStep = 1;
        const totalSteps = 5;

        const showStep = (step) => {
            for (let i = 1; i <= totalSteps; i++) {
                const pane = document.getElementById(`wizardStep${i}`);
                const tab = document.getElementById(`stepTab${i}`);
                if (pane) pane.classList.toggle('active', i === step);
                if (tab) {
                    tab.classList.toggle('active', i === step);
                    tab.classList.toggle('completed', i < step);
                }
            }
            currentStep = step;
            window.scrollTo({ top: wizardForm.offsetTop - 80, behavior: 'smooth' });

            // Generate review summary on step 5
            if (step === 5) {
                populateWizardReview();
            }
        };

        const validateStep = (step) => {
            const currentPane = document.getElementById(`wizardStep${step}`);
            if (!currentPane) return true;
            const requiredInputs = currentPane.querySelectorAll('[required]');
            for (const input of requiredInputs) {
                if (!input.checkValidity()) {
                    input.reportValidity();
                    return false;
                }
            }
            return true;
        };

        const populateWizardReview = () => {
            const setVal = (id, val) => {
                const el = document.getElementById(id);
                if (el) el.innerText = val || '—';
            };

            const title = wizardForm.querySelector('#title')?.value;
            const catSelect = wizardForm.querySelector('#foodCategoryId');
            const catName = catSelect ? catSelect.options[catSelect.selectedIndex]?.text : '';
            const qty = wizardForm.querySelector('#totalQuantity')?.value;
            const unitSelect = wizardForm.querySelector('#unitId');
            const unitName = unitSelect ? unitSelect.options[unitSelect.selectedIndex]?.text : '';

            const prep = wizardForm.querySelector('#preparationTime')?.value;
            const until = wizardForm.querySelector('#availableUntil')?.value;
            const dietary = wizardForm.querySelector('#dietaryType')?.value;
            const storage = wizardForm.querySelector('#storageCondition')?.value;
            const packaging = wizardForm.querySelector('#packagingType')?.value;
            const address = wizardForm.querySelector('#pickupAddress')?.value;
            const city = wizardForm.querySelector('#pickupCity')?.value;
            const contact = wizardForm.querySelector('#contactName')?.value;
            const phone = wizardForm.querySelector('#contactPhone')?.value;

            setVal('reviewTitle', title);
            setVal('reviewCategory', catName);
            setVal('reviewQuantity', `${qty} ${unitName}`);
            setVal('reviewPrepared', prep ? prep.replace('T', ' ') : '');
            setVal('reviewUntil', until ? until.replace('T', ' ') : '');
            setVal('reviewDietary', dietary);
            setVal('reviewStorage', `${storage} • ${packaging}`);
            setVal('reviewPickup', `${address}, ${city}`);
            setVal('reviewContact', `${contact} (${phone})`);
        };

        // Next / Prev Button Listeners
        wizardForm.querySelectorAll('[data-wizard-action]').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                const action = btn.getAttribute('data-wizard-action');
                if (action === 'next') {
                    if (validateStep(currentStep) && currentStep < totalSteps) {
                        showStep(currentStep + 1);
                    }
                } else if (action === 'prev') {
                    if (currentStep > 1) {
                        showStep(currentStep - 1);
                    }
                }
            });
        });

        // Tab click jumping (only to visited steps)
        wizardForm.querySelectorAll('.wizard-step-tab').forEach(tab => {
            tab.addEventListener('click', () => {
                const targetStep = parseInt(tab.getAttribute('data-step') || '1', 10);
                if (targetStep < currentStep || validateStep(currentStep)) {
                    showStep(targetStep);
                }
            });
        });
    };
    initDonationWizard();

    /* ====================================================================
       5. INTERACTIVE CHIP SELECTOR (Forms & Requirements)
       ==================================================================== */
    const initChipSelectors = () => {
        const chipOptions = document.querySelectorAll('.chip-option');
        chipOptions.forEach(option => {
            const input = option.querySelector('input[type="radio"], input[type="checkbox"]');
            if (!input) return;

            // Sync initial visual state
            if (input.checked) {
                option.classList.add('selected');
            } else {
                option.classList.remove('selected');
            }

            input.addEventListener('change', () => {
                if (input.type === 'radio') {
                    const group = option.closest('.chip-selector-grid') || option.parentElement;
                    if (group) {
                        group.querySelectorAll('.chip-option').forEach(o => {
                            const oInput = o.querySelector('input[type="radio"]');
                            if (oInput && oInput.name === input.name) {
                                o.classList.toggle('selected', oInput.checked);
                            }
                        });
                    }
                } else {
                    option.classList.toggle('selected', input.checked);
                }
            });
        });
    };
    initChipSelectors();

    /* ====================================================================
       6. EXPLAINABLE MATCH EXPANDER ("Why This Match?")
       ==================================================================== */
    const initMatchExplainers = () => {
        const toggleButtons = document.querySelectorAll('[data-toggle-match]');
        toggleButtons.forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                const targetId = btn.getAttribute('data-toggle-match');
                const panel = document.getElementById(targetId);
                if (panel) {
                    const isHidden = panel.style.display === 'none' || !panel.style.display;
                    panel.style.display = isHidden ? 'block' : 'none';
                    btn.innerText = isHidden ? '▴ Hide Match Quality Breakdown' : '▾ Why Recommended? (Match Breakdown)';
                }
            });
        });
    };
    initMatchExplainers();

    /* ====================================================================
       7. CONFIRMATION DIALOGS
       ==================================================================== */
    const confirmButtons = document.querySelectorAll('[data-confirm]');
    confirmButtons.forEach(btn => {
        btn.addEventListener('click', (e) => {
            const msg = btn.getAttribute('data-confirm') || 'Are you sure you want to proceed?';
            if (!confirm(msg)) {
                e.preventDefault();
            }
        });
    });
});
