-- ====================================================================
-- ANNPATH — DEVELOPMENT & INTEGRATION TEST SEED DATA
-- (NOT FOR PRODUCTION USE — ONLY FOR STAGING/TESTING)
-- ====================================================================

USE `annpath`;

-- Password hash below corresponds to 'Password123!' with salt 'dGVzdHNhbHQxMjM0NTY3OA=='
-- Generated via PBKDF2-HMAC-SHA512 (100,000 iterations)
-- Hash: 43a6fb98d41e7399587da4815a5ce2e307374092b3a88a70678d22d25081f887b4742e6f4773c886111be0dfc71285223c72bcf92b8d002f23249fa6b21699f9

-- 1. Test Donor User
INSERT INTO `users` (`id`, `email`, `phone_number`, `phone_country_code`, `password_hash`, `password_salt`, `role`, `status`, `preferred_language`, `preferred_timezone`, `email_verified`, `phone_verified`) 
VALUES (1, 'donor@annpath.org', '9876543210', '+91', '43a6fb98d41e7399587da4815a5ce2e307374092b3a88a70678d22d25081f887b4742e6f4773c886111be0dfc71285223c72bcf92b8d002f23249fa6b21699f9', 'dGVzdHNhbHQxMjM0NTY3OA==', 'DONOR', 'ACTIVE', 'en', 'Asia/Kolkata', TRUE, TRUE)
ON DUPLICATE KEY UPDATE `email` = VALUES(`email`);

INSERT INTO `donor_profiles` (`user_id`, `donor_type`, `organization_name`, `contact_person`, `address_line1`, `city`, `state_region`, `postal_code`, `country_code`, `latitude`, `longitude`, `default_pickup_instructions`)
VALUES (1, 'RESTAURANT', 'Grand Harvest Bistro', 'Rajesh Sharma', '124 Linking Road, Bandra West', 'Mumbai', 'Maharashtra', '400050', 'IN', 19.05960000, 72.82950000, 'Pickup at back delivery gate between 2 PM and 4 PM')
ON DUPLICATE KEY UPDATE `organization_name` = VALUES(`organization_name`);

-- 2. Test NGO User
INSERT INTO `users` (`id`, `email`, `phone_number`, `phone_country_code`, `password_hash`, `password_salt`, `role`, `status`, `preferred_language`, `preferred_timezone`, `email_verified`, `phone_verified`)
VALUES (2, 'ngo@annpath.org', '9876543211', '+91', '43a6fb98d41e7399587da4815a5ce2e307374092b3a88a70678d22d25081f887b4742e6f4773c886111be0dfc71285223c72bcf92b8d002f23249fa6b21699f9', 'dGVzdHNhbHQxMjM0NTY3OA==', 'NGO', 'ACTIVE', 'en', 'Asia/Kolkata', TRUE, TRUE)
ON DUPLICATE KEY UPDATE `email` = VALUES(`email`);

INSERT INTO `ngo_profiles` (`user_id`, `organization_name`, `registration_number`, `registration_authority`, `established_year`, `representative_name`, `representative_designation`, `website`, `address_line1`, `city`, `state_region`, `postal_code`, `country_code`, `latitude`, `longitude`, `service_radius_km`, `verification_status`, `verified_at`, `max_daily_capacity_kg`)
VALUES (2, 'Mumbai Food Relief Foundation', 'NGO-MH-2018-9981', 'Registrar of Societies Mumbai', 2018, 'Sunita Deshmukh', 'Director', 'https://mumbaifoodrelief.example.org', '56 Hill Road, Bandra West', 'Mumbai', 'Maharashtra', '400050', 'IN', 19.05500000, 72.83100000, 20.00, 'VERIFIED', NOW(), 1000.00)
ON DUPLICATE KEY UPDATE `organization_name` = VALUES(`organization_name`);

-- 3. Test Volunteer User
INSERT INTO `users` (`id`, `email`, `phone_number`, `phone_country_code`, `password_hash`, `password_salt`, `role`, `status`, `preferred_language`, `preferred_timezone`, `email_verified`, `phone_verified`)
VALUES (3, 'volunteer@annpath.org', '9876543212', '+91', '43a6fb98d41e7399587da4815a5ce2e307374092b3a88a70678d22d25081f887b4742e6f4773c886111be0dfc71285223c72bcf92b8d002f23249fa6b21699f9', 'dGVzdHNhbHQxMjM0NTY3OA==', 'VOLUNTEER', 'ACTIVE', 'en', 'Asia/Kolkata', TRUE, TRUE)
ON DUPLICATE KEY UPDATE `email` = VALUES(`email`);

INSERT INTO `volunteer_profiles` (`user_id`, `first_name`, `last_name`, `address_line1`, `city`, `state_region`, `postal_code`, `country_code`, `latitude`, `longitude`, `max_travel_distance_km`, `vehicle_type`, `availability_status`, `active_assignments_count`, `max_concurrent_assignments`, `emergency_contact_phone`)
VALUES (3, 'Amit', 'Verma', '88 Turner Road, Bandra West', 'Mumbai', 'Maharashtra', '400050', 'IN', 19.06100000, 72.83400000, 15.00, 'MOTORCYCLE', 'AVAILABLE', 0, 3, '9876543299')
ON DUPLICATE KEY UPDATE `first_name` = VALUES(`first_name`);

-- 4. Active Operational Surplus Listings
INSERT INTO `food_listings` (`id`, `donor_user_id`, `title`, `rescue_code`, `food_category_id`, `dietary_type`, `total_quantity`, `remaining_quantity`, `reserved_quantity`, `unit_id`, `storage_condition`, `packaging_type`, `description`, `preparation_time`, `available_from`, `available_until`, `urgency_status`, `listing_status`, `pickup_address`, `pickup_city`, `pickup_region`, `pickup_postal_code`, `pickup_country_code`, `pickup_latitude`, `pickup_longitude`, `contact_name`, `contact_phone`, `priority_score`, `priority_explanation`)
VALUES 
(101, 1, 'Fresh Paneer Veg Pulao & Dal Tadka', 'ANP-2026-000101', 1, 'VEGETARIAN', 45.00, 45.00, 0.00, 1, 'WARM_HEATED', 'Insulated Catering Containers', 'Freshly prepared vegetarian pulao and yellow dal from corporate banquet.', NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 3 HOUR), 'CRITICAL', 'DONATED', '124 Linking Road, Bandra West', 'Mumbai', 'Maharashtra', '400050', 'IN', 19.05960000, 72.82950000, 'Chef Rajesh', '9876543210', 94.50, 'Cooked meal within 3-hour expiry threshold.')
ON DUPLICATE KEY UPDATE `remaining_quantity` = VALUES(`remaining_quantity`);

INSERT INTO `food_listings` (`id`, `donor_user_id`, `title`, `rescue_code`, `food_category_id`, `dietary_type`, `total_quantity`, `remaining_quantity`, `reserved_quantity`, `unit_id`, `storage_condition`, `packaging_type`, `description`, `preparation_time`, `available_from`, `available_until`, `urgency_status`, `listing_status`, `pickup_address`, `pickup_city`, `pickup_region`, `pickup_postal_code`, `pickup_country_code`, `pickup_latitude`, `pickup_longitude`, `contact_name`, `contact_phone`, `priority_score`, `priority_explanation`)
VALUES 
(102, 1, 'Artisan Sourdough Breads & Croissants', 'ANP-2026-000102', 3, 'VEGETARIAN', 28.00, 28.00, 0.00, 1, 'ROOM_TEMP', 'Sealed Paper Crates', 'Fresh morning bakery surplus packed in food-grade sealed bakery bags.', NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 6 HOUR), 'EXPIRING_SOON', 'DONATED', '124 Linking Road, Bandra West', 'Mumbai', 'Maharashtra', '400050', 'IN', 19.05600000, 72.83100000, 'Chef Rajesh', '9876543210', 86.00, 'Fresh bakery surplus expiring today.')
ON DUPLICATE KEY UPDATE `remaining_quantity` = VALUES(`remaining_quantity`);

-- 5. Operational Pickup & Impact Record (Integration Test Mission)
INSERT INTO `food_requests` (`id`, `food_listing_id`, `ngo_user_id`, `requested_quantity`, `request_status`, `delivery_method`, `notes`)
VALUES (101, 101, 2, 15.00, 'APPROVED', 'VOLUNTEER', 'Requested for community kitchen dinner service.')
ON DUPLICATE KEY UPDATE `requested_quantity` = VALUES(`requested_quantity`);

INSERT INTO `pickups` (`id`, `food_listing_id`, `food_request_id`, `donor_user_id`, `ngo_user_id`, `pickup_method`, `volunteer_user_id`, `pickup_window_start`, `pickup_window_end`, `pickup_otp_hash`, `delivery_otp_hash`, `status`, `actual_picked_up_at`, `actual_delivered_at`, `ngo_received_at`, `ngo_distributed_at`)
VALUES (101, 101, 101, 1, 2, 'VOLUNTEER', 3, DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'DISTRIBUTED', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 30 MINUTE))
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);

INSERT INTO `impact_records` (`id`, `food_listing_id`, `pickup_id`, `ngo_user_id`, `donor_user_id`, `volunteer_user_id`, `rescued_quantity_kg`, `estimated_people_fed`, `co2_emissions_prevented_kg`, `recorded_at`)
VALUES (1, 101, 101, 2, 1, 3, 15.00, 36, 37.50, DATE_SUB(NOW(), INTERVAL 30 MINUTE))
ON DUPLICATE KEY UPDATE `rescued_quantity_kg` = VALUES(`rescued_quantity_kg`);
