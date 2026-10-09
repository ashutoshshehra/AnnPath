-- ====================================================================
-- ANNPATH — A PATH FROM SURPLUS TO NEED
-- GLOBAL FOOD SURPLUS REDISTRIBUTION PLATFORM
-- PRODUCTION-GRADE DATABASE SCHEMA & REFERENCE DATA
-- ====================================================================

CREATE DATABASE IF NOT EXISTS `annpath` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `annpath`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `rescue_bottlenecks`;
DROP TABLE IF EXISTS `impact_records`;
DROP TABLE IF EXISTS `audit_logs`;
DROP TABLE IF EXISTS `complaints`;
DROP TABLE IF EXISTS `feedback`;
DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `digital_rescue_receipts`;
DROP TABLE IF EXISTS `food_status_history`;
DROP TABLE IF EXISTS `pickup_assignment_history`;
DROP TABLE IF EXISTS `pickups`;
DROP TABLE IF EXISTS `food_requests`;
DROP TABLE IF EXISTS `ngo_requirements`;
DROP TABLE IF EXISTS `food_listings`;
DROP TABLE IF EXISTS `otp_verifications`;
DROP TABLE IF EXISTS `verification_documents`;
DROP TABLE IF EXISTS `ngo_verifications`;
DROP TABLE IF EXISTS `verification_rules`;
DROP TABLE IF EXISTS `volunteer_profiles`;
DROP TABLE IF EXISTS `ngo_profiles`;
DROP TABLE IF EXISTS `donor_profiles`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `food_categories`;
DROP TABLE IF EXISTS `measurement_units`;
DROP TABLE IF EXISTS `timezones`;
DROP TABLE IF EXISTS `languages`;
DROP TABLE IF EXISTS `regions`;
DROP TABLE IF EXISTS `countries`;

SET FOREIGN_KEY_CHECKS = 1;

-- --------------------------------------------------------------------
-- 1. COUNTRIES & REGIONS
-- --------------------------------------------------------------------
CREATE TABLE `countries` (
    `country_code` VARCHAR(2) NOT NULL,
    `country_name` VARCHAR(100) NOT NULL,
    `phone_code` VARCHAR(10) NOT NULL,
    `default_currency` VARCHAR(3) NOT NULL,
    `default_timezone` VARCHAR(50) NOT NULL,
    `distance_unit` ENUM('KM', 'MI') NOT NULL DEFAULT 'KM',
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (`country_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `regions` (
    `id` INT AUTO_INCREMENT NOT NULL,
    `country_code` VARCHAR(2) NOT NULL,
    `region_code` VARCHAR(10) NOT NULL,
    `region_name` VARCHAR(100) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_regions_country` FOREIGN KEY (`country_code`) REFERENCES `countries`(`country_code`) ON DELETE CASCADE,
    INDEX `idx_reg_country` (`country_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 2. LANGUAGES & TIMEZONES
-- --------------------------------------------------------------------
CREATE TABLE `languages` (
    `lang_code` VARCHAR(5) NOT NULL,
    `lang_name` VARCHAR(50) NOT NULL,
    `native_name` VARCHAR(50) NOT NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (`lang_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `timezones` (
    `tz_name` VARCHAR(50) NOT NULL,
    `utc_offset_minutes` INT NOT NULL,
    `country_code` VARCHAR(2) NOT NULL,
    PRIMARY KEY (`tz_name`),
    CONSTRAINT `fk_timezones_country` FOREIGN KEY (`country_code`) REFERENCES `countries`(`country_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 3. MEASUREMENT UNITS
-- --------------------------------------------------------------------
CREATE TABLE `measurement_units` (
    `id` INT AUTO_INCREMENT NOT NULL,
    `unit_code` VARCHAR(20) NOT NULL,
    `unit_name` VARCHAR(50) NOT NULL,
    `unit_type` ENUM('WEIGHT', 'VOLUME', 'COUNT') NOT NULL,
    `conversion_factor_to_kg` DECIMAL(10,4) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_unit_code` (`unit_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 4. FOOD CATEGORIES
-- --------------------------------------------------------------------
CREATE TABLE `food_categories` (
    `id` INT AUTO_INCREMENT NOT NULL,
    `category_code` VARCHAR(50) NOT NULL,
    `category_name` VARCHAR(100) NOT NULL,
    `is_perishable` BOOLEAN NOT NULL DEFAULT TRUE,
    `default_shelf_life_hours` INT NOT NULL,
    `default_storage_condition` ENUM('ROOM_TEMP', 'REFRIGERATED', 'FROZEN', 'WARM_HEATED') NOT NULL,
    `urgency_weight` DECIMAL(3,2) NOT NULL DEFAULT 1.00,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_cat_code` (`category_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 5. VERIFICATION RULES
-- --------------------------------------------------------------------
CREATE TABLE `verification_rules` (
    `id` INT AUTO_INCREMENT NOT NULL,
    `country_code` VARCHAR(2) NOT NULL,
    `rule_code` VARCHAR(50) NOT NULL,
    `rule_name` VARCHAR(150) NOT NULL,
    `min_established_years` INT NOT NULL DEFAULT 1,
    `requires_org_document` BOOLEAN NOT NULL DEFAULT TRUE,
    `requires_registration_doc` BOOLEAN NOT NULL DEFAULT TRUE,
    `requires_address_proof` BOOLEAN NOT NULL DEFAULT TRUE,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_rule_country` (`country_code`, `rule_code`),
    CONSTRAINT `fk_vrules_country` FOREIGN KEY (`country_code`) REFERENCES `countries`(`country_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 6. USERS
-- --------------------------------------------------------------------
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT NOT NULL,
    `email` VARCHAR(191) NOT NULL,
    `phone_number` VARCHAR(30) NOT NULL,
    `phone_country_code` VARCHAR(10) NOT NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `password_salt` VARCHAR(64) NOT NULL,
    `role` ENUM('DONOR', 'NGO', 'VOLUNTEER') NOT NULL,
    `status` ENUM('ACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION') NOT NULL DEFAULT 'PENDING_VERIFICATION',
    `preferred_language` VARCHAR(5) NOT NULL DEFAULT 'en',
    `preferred_timezone` VARCHAR(50) NOT NULL DEFAULT 'UTC',
    `email_verified` BOOLEAN NOT NULL DEFAULT FALSE,
    `phone_verified` BOOLEAN NOT NULL DEFAULT FALSE,
    `failed_login_attempts` INT NOT NULL DEFAULT 0,
    `locked_until` DATETIME NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_user_email` (`email`),
    INDEX `idx_user_role` (`role`),
    INDEX `idx_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 7. DONOR PROFILES
-- --------------------------------------------------------------------
CREATE TABLE `donor_profiles` (
    `user_id` BIGINT NOT NULL,
    `donor_type` ENUM('INDIVIDUAL', 'RESTAURANT', 'HOTEL', 'SUPERMARKET', 'CORPORATE_CAFETERIA', 'EVENT_CATERER') NOT NULL,
    `organization_name` VARCHAR(150) NULL,
    `contact_person` VARCHAR(100) NOT NULL,
    `address_line1` VARCHAR(255) NOT NULL,
    `address_line2` VARCHAR(255) NULL,
    `city` VARCHAR(100) NOT NULL,
    `state_region` VARCHAR(100) NOT NULL,
    `postal_code` VARCHAR(20) NOT NULL,
    `country_code` VARCHAR(2) NOT NULL,
    `latitude` DECIMAL(10,8) NULL,
    `longitude` DECIMAL(11,8) NULL,
    `default_pickup_instructions` TEXT NULL,
    PRIMARY KEY (`user_id`),
    CONSTRAINT `fk_dp_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_dp_country` FOREIGN KEY (`country_code`) REFERENCES `countries`(`country_code`),
    INDEX `idx_donor_geo` (`latitude`, `longitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 8. NGO PROFILES
-- --------------------------------------------------------------------
CREATE TABLE `ngo_profiles` (
    `user_id` BIGINT NOT NULL,
    `organization_name` VARCHAR(200) NOT NULL,
    `registration_number` VARCHAR(100) NOT NULL,
    `registration_authority` VARCHAR(150) NOT NULL,
    `established_year` INT NOT NULL,
    `representative_name` VARCHAR(100) NOT NULL,
    `representative_designation` VARCHAR(100) NOT NULL,
    `website` VARCHAR(255) NULL,
    `address_line1` VARCHAR(255) NOT NULL,
    `address_line2` VARCHAR(255) NULL,
    `city` VARCHAR(100) NOT NULL,
    `state_region` VARCHAR(100) NOT NULL,
    `postal_code` VARCHAR(20) NOT NULL,
    `country_code` VARCHAR(2) NOT NULL,
    `latitude` DECIMAL(10,8) NULL,
    `longitude` DECIMAL(11,8) NULL,
    `service_radius_km` DECIMAL(6,2) NOT NULL DEFAULT 15.00,
    `verification_status` ENUM('PENDING_VERIFICATION', 'VERIFIED', 'REJECTED', 'SUSPENDED') NOT NULL DEFAULT 'PENDING_VERIFICATION',
    `verified_at` DATETIME NULL,
    `rejection_reason` TEXT NULL,
    `max_daily_capacity_kg` DECIMAL(10,2) NOT NULL DEFAULT 500.00,
    PRIMARY KEY (`user_id`),
    CONSTRAINT `fk_np_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_np_country` FOREIGN KEY (`country_code`) REFERENCES `countries`(`country_code`),
    INDEX `idx_ngo_geo` (`latitude`, `longitude`),
    INDEX `idx_ngo_status` (`verification_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 9. VOLUNTEER PROFILES
-- --------------------------------------------------------------------
CREATE TABLE `volunteer_profiles` (
    `user_id` BIGINT NOT NULL,
    `first_name` VARCHAR(50) NOT NULL,
    `last_name` VARCHAR(50) NOT NULL,
    `address_line1` VARCHAR(255) NOT NULL,
    `city` VARCHAR(100) NOT NULL,
    `state_region` VARCHAR(100) NOT NULL,
    `postal_code` VARCHAR(20) NOT NULL,
    `country_code` VARCHAR(2) NOT NULL,
    `latitude` DECIMAL(10,8) NULL,
    `longitude` DECIMAL(11,8) NULL,
    `max_travel_distance_km` DECIMAL(5,2) NOT NULL DEFAULT 10.00,
    `vehicle_type` ENUM('NONE_WALKING', 'BICYCLE', 'MOTORCYCLE', 'CAR', 'VAN', 'TRUCK') NOT NULL DEFAULT 'MOTORCYCLE',
    `availability_status` ENUM('AVAILABLE', 'BUSY', 'OFFLINE') NOT NULL DEFAULT 'OFFLINE',
    `active_assignments_count` INT NOT NULL DEFAULT 0,
    `max_concurrent_assignments` INT NOT NULL DEFAULT 2,
    `emergency_contact_phone` VARCHAR(30) NULL,
    PRIMARY KEY (`user_id`),
    CONSTRAINT `fk_vp_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_vp_country` FOREIGN KEY (`country_code`) REFERENCES `countries`(`country_code`),
    INDEX `idx_vol_geo` (`latitude`, `longitude`),
    INDEX `idx_vol_avail` (`availability_status`, `active_assignments_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 10. NGO VERIFICATIONS
-- --------------------------------------------------------------------
CREATE TABLE `ngo_verifications` (
    `id` BIGINT AUTO_INCREMENT NOT NULL,
    `ngo_user_id` BIGINT NOT NULL,
    `rule_set_version` VARCHAR(20) NOT NULL,
    `evaluated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `score` DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    `status` ENUM('VERIFIED', 'REJECTED', 'PENDING') NOT NULL,
    `failure_reasons_json` TEXT NULL,
    `automated_flags_json` TEXT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_nv_ngo` FOREIGN KEY (`ngo_user_id`) REFERENCES `ngo_profiles`(`user_id`) ON DELETE CASCADE,
    INDEX `idx_nv_ngo` (`ngo_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 11. VERIFICATION DOCUMENTS
-- --------------------------------------------------------------------
CREATE TABLE `verification_documents` (
    `id` BIGINT AUTO_INCREMENT NOT NULL,
    `ngo_user_id` BIGINT NOT NULL,
    `document_type` ENUM('REGISTRATION_DOCUMENT', 'ORGANIZATION_DOCUMENT', 'ADDRESS_PROOF', 'IDENTITY_DOCUMENT', 'OTHER_SUPPORTING_DOCUMENT') NOT NULL,
    `original_file_name` VARCHAR(255) NOT NULL,
    `stored_file_name` VARCHAR(255) NOT NULL,
    `file_path` VARCHAR(500) NOT NULL,
    `file_size_bytes` BIGINT NOT NULL,
    `mime_type` VARCHAR(100) NOT NULL,
    `file_hash_sha256` VARCHAR(64) NOT NULL,
    `uploaded_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_vd_ngo` FOREIGN KEY (`ngo_user_id`) REFERENCES `ngo_profiles`(`user_id`) ON DELETE CASCADE,
    INDEX `idx_vdoc_ngo` (`ngo_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 12. OTP VERIFICATIONS
-- --------------------------------------------------------------------
CREATE TABLE `otp_verifications` (
    `id` BIGINT AUTO_INCREMENT NOT NULL,
    `user_id` BIGINT NULL,
    `identifier` VARCHAR(191) NOT NULL,
    `channel` ENUM('EMAIL', 'SMS') NOT NULL,
    `purpose` ENUM('REGISTRATION', 'LOGIN', 'PASSWORD_RESET', 'PICKUP_CONFIRMATION', 'DELIVERY_CONFIRMATION') NOT NULL,
    `otp_hash` VARCHAR(255) NOT NULL,
    `otp_salt` VARCHAR(64) NOT NULL,
    `attempts_count` INT NOT NULL DEFAULT 0,
    `max_attempts` INT NOT NULL DEFAULT 3,
    `expires_at` DATETIME NOT NULL,
    `verified_at` DATETIME NULL,
    `cooldown_until` DATETIME NOT NULL,
    `is_invalidated` BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_otp_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    INDEX `idx_otp_lookup` (`identifier`, `purpose`, `is_invalidated`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 13. FOOD LISTINGS
-- --------------------------------------------------------------------
CREATE TABLE `food_listings` (
    `id` BIGINT AUTO_INCREMENT NOT NULL,
    `rescue_code` VARCHAR(32) NULL UNIQUE,
    `donor_user_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `food_category_id` INT NOT NULL,
    `dietary_type` ENUM('VEGETARIAN', 'VEGAN', 'NON_VEGETARIAN', 'HALAL', 'KOSHER', 'GLUTEN_FREE', 'DAIRY_FREE') NOT NULL,
    `total_quantity` DECIMAL(10,2) NOT NULL,
    `remaining_quantity` DECIMAL(10,2) NOT NULL,
    `reserved_quantity` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `unit_id` INT NOT NULL,
    `storage_condition` ENUM('ROOM_TEMP', 'REFRIGERATED', 'FROZEN', 'WARM_HEATED') NOT NULL,
    `packaging_type` VARCHAR(100) NOT NULL,
    `description` TEXT NOT NULL,
    `ingredients` TEXT NULL,
    `allergens` VARCHAR(255) NULL,
    `preparation_time` DATETIME NOT NULL,
    `available_from` DATETIME NOT NULL,
    `available_until` DATETIME NOT NULL,
    `urgency_status` ENUM('FRESH', 'EXPIRING_SOON', 'CRITICAL', 'EXPIRED') NOT NULL DEFAULT 'FRESH',
    `listing_status` ENUM('DONATED', 'MATCHED', 'REQUESTED', 'APPROVED', 'VOLUNTEER_ASSIGNED', 'NGO_SELF_PICKUP_ASSIGNED', 'PICKED_UP', 'DELIVERED', 'RECEIVED', 'DISTRIBUTED', 'REJECTED', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'DONATED',
    `pickup_address` VARCHAR(255) NOT NULL,
    `pickup_city` VARCHAR(100) NOT NULL,
    `pickup_region` VARCHAR(100) NOT NULL,
    `pickup_postal_code` VARCHAR(20) NOT NULL,
    `pickup_country_code` VARCHAR(2) NOT NULL,
    `pickup_latitude` DECIMAL(10,8) NULL,
    `pickup_longitude` DECIMAL(11,8) NULL,
    `pickup_instructions` TEXT NULL,
    `contact_name` VARCHAR(100) NOT NULL,
    `contact_phone` VARCHAR(30) NOT NULL,
    `food_image_path` VARCHAR(500) NULL,
    `priority_score` DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    `priority_explanation` VARCHAR(500) NULL,
    `version` INT NOT NULL DEFAULT 1,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_fl_donor` FOREIGN KEY (`donor_user_id`) REFERENCES `users`(`id`),
    CONSTRAINT `fk_fl_category` FOREIGN KEY (`food_category_id`) REFERENCES `food_categories`(`id`),
    CONSTRAINT `fk_fl_unit` FOREIGN KEY (`unit_id`) REFERENCES `measurement_units`(`id`),
    CONSTRAINT `fk_fl_country` FOREIGN KEY (`pickup_country_code`) REFERENCES `countries`(`country_code`),
    INDEX `idx_listing_geo` (`pickup_latitude`, `pickup_longitude`),
    INDEX `idx_listing_status_urgency` (`listing_status`, `urgency_status`, `available_until`),
    INDEX `idx_listing_donor` (`donor_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 14. NGO REQUIREMENTS
-- --------------------------------------------------------------------
CREATE TABLE `ngo_requirements` (
    `id` BIGINT AUTO_INCREMENT NOT NULL,
    `ngo_user_id` BIGINT NOT NULL,
    `title` VARCHAR(150) NOT NULL,
    `food_category_id` INT NOT NULL,
    `dietary_type` ENUM('ANY', 'VEGETARIAN', 'VEGAN', 'NON_VEGETARIAN', 'HALAL', 'KOSHER') NOT NULL DEFAULT 'ANY',
    `min_quantity` DECIMAL(10,2) NOT NULL,
    `max_quantity` DECIMAL(10,2) NOT NULL,
    `unit_id` INT NOT NULL,
    `is_recurring` BOOLEAN NOT NULL DEFAULT FALSE,
    `priority_level` ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') NOT NULL DEFAULT 'MEDIUM',
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_nr_ngo` FOREIGN KEY (`ngo_user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_nr_cat` FOREIGN KEY (`food_category_id`) REFERENCES `food_categories`(`id`),
    CONSTRAINT `fk_nr_unit` FOREIGN KEY (`unit_id`) REFERENCES `measurement_units`(`id`),
    INDEX `idx_req_ngo` (`ngo_user_id`, `is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 15. FOOD REQUESTS
-- --------------------------------------------------------------------
CREATE TABLE `food_requests` (
    `id` BIGINT AUTO_INCREMENT NOT NULL,
    `food_listing_id` BIGINT NOT NULL,
    `ngo_user_id` BIGINT NOT NULL,
    `requested_quantity` DECIMAL(10,2) NOT NULL,
    `unit_id` INT NOT NULL,
    `request_notes` TEXT NULL,
    `status` ENUM('REQUESTED', 'APPROVED', 'REJECTED', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'REQUESTED',
    `donor_response_notes` TEXT NULL,
    `responded_at` DATETIME NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_fr_listing` FOREIGN KEY (`food_listing_id`) REFERENCES `food_listings`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_fr_ngo` FOREIGN KEY (`ngo_user_id`) REFERENCES `users`(`id`),
    CONSTRAINT `fk_fr_unit` FOREIGN KEY (`unit_id`) REFERENCES `measurement_units`(`id`),
    INDEX `idx_req_listing` (`food_listing_id`),
    INDEX `idx_req_ngo` (`ngo_user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 16. PICKUPS
-- --------------------------------------------------------------------
CREATE TABLE `pickups` (
    `id` BIGINT AUTO_INCREMENT NOT NULL,
    `food_listing_id` BIGINT NOT NULL,
    `food_request_id` BIGINT NOT NULL,
    `donor_user_id` BIGINT NOT NULL,
    `ngo_user_id` BIGINT NOT NULL,
    `pickup_method` ENUM('VOLUNTEER', 'NGO_SELF_PICKUP') NOT NULL DEFAULT 'VOLUNTEER',
    `volunteer_user_id` BIGINT NULL,
    `self_pickup_person_name` VARCHAR(100) NULL,
    `self_pickup_person_phone` VARCHAR(30) NULL,
    `self_pickup_id_type` VARCHAR(50) NULL,
    `self_pickup_id_number` VARCHAR(100) NULL,
    `pickup_window_start` DATETIME NOT NULL,
    `pickup_window_end` DATETIME NOT NULL,
    `pickup_otp_hash` VARCHAR(255) NOT NULL,
    `delivery_otp_hash` VARCHAR(255) NOT NULL,
    `pickup_notes` TEXT NULL,
    `delivery_notes` TEXT NULL,
    `proof_image_path` VARCHAR(500) NULL,
    `status` ENUM('ASSIGNED', 'REASSIGNING', 'VOLUNTEER_UNAVAILABLE', 'NGO_SELF_PICKUP_ASSIGNED', 'PICKED_UP', 'DELIVERED', 'RECEIVED', 'DISTRIBUTED', 'PICKUP_FAILED', 'DELIVERY_FAILED', 'CANCELLED') NOT NULL DEFAULT 'ASSIGNED',
    `assigned_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `actual_picked_up_at` DATETIME NULL,
    `actual_delivered_at` DATETIME NULL,
    `ngo_received_at` DATETIME NULL,
    `ngo_distributed_at` DATETIME NULL,
    `version` INT NOT NULL DEFAULT 1,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_pickup_request` (`food_request_id`),
    CONSTRAINT `fk_pk_listing` FOREIGN KEY (`food_listing_id`) REFERENCES `food_listings`(`id`),
    CONSTRAINT `fk_pk_request` FOREIGN KEY (`food_request_id`) REFERENCES `food_requests`(`id`),
    CONSTRAINT `fk_pk_donor` FOREIGN KEY (`donor_user_id`) REFERENCES `users`(`id`),
    CONSTRAINT `fk_pk_ngo` FOREIGN KEY (`ngo_user_id`) REFERENCES `users`(`id`),
    CONSTRAINT `fk_pk_vol` FOREIGN KEY (`volunteer_user_id`) REFERENCES `users`(`id`),
    INDEX `idx_pickup_vol` (`volunteer_user_id`, `status`),
    INDEX `idx_pickup_ngo` (`ngo_user_id`, `status`),
    INDEX `idx_pickup_listing` (`food_listing_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 17. PICKUP ASSIGNMENT HISTORY
-- --------------------------------------------------------------------
CREATE TABLE `pickup_assignment_history` (
    `id` BIGINT AUTO_INCREMENT NOT NULL,
    `pickup_id` BIGINT NOT NULL,
    `volunteer_user_id` BIGINT NOT NULL,
    `assigned_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `responded_at` DATETIME NULL,
    `response_status` ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'TIMEOUT_EXPIRED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    `rejection_reason` VARCHAR(255) NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_pah_pickup` FOREIGN KEY (`pickup_id`) REFERENCES `pickups`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_pah_vol` FOREIGN KEY (`volunteer_user_id`) REFERENCES `users`(`id`),
    INDEX `idx_pah_pickup` (`pickup_id`),
    INDEX `idx_pah_vol` (`volunteer_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 18. FOOD STATUS HISTORY
-- --------------------------------------------------------------------
CREATE TABLE `food_status_history` (
    `id` BIGINT AUTO_INCREMENT NOT NULL,
    `food_listing_id` BIGINT NOT NULL,
    `pickup_id` BIGINT NULL,
    `previous_status` VARCHAR(50) NOT NULL,
    `new_status` VARCHAR(50) NOT NULL,
    `actor_user_id` BIGINT NULL,
    `actor_role` VARCHAR(20) NOT NULL,
    `transition_reason` VARCHAR(255) NOT NULL,
    `transition_metadata_json` TEXT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_fsh_listing` FOREIGN KEY (`food_listing_id`) REFERENCES `food_listings`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_fsh_pickup` FOREIGN KEY (`pickup_id`) REFERENCES `pickups`(`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_fsh_actor` FOREIGN KEY (`actor_user_id`) REFERENCES `users`(`id`) ON DELETE SET NULL,
    INDEX `idx_fsh_listing` (`food_listing_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 19. DIGITAL RESCUE RECEIPTS
-- --------------------------------------------------------------------
CREATE TABLE `digital_rescue_receipts` (
    `id` BIGINT AUTO_INCREMENT NOT NULL,
    `receipt_number` VARCHAR(50) NOT NULL,
    `food_listing_id` BIGINT NOT NULL,
    `pickup_id` BIGINT NOT NULL,
    `donor_user_id` BIGINT NOT NULL,
    `ngo_user_id` BIGINT NOT NULL,
    `volunteer_user_id` BIGINT NULL,
    `rescued_quantity` DECIMAL(10,2) NOT NULL,
    `unit_name` VARCHAR(50) NOT NULL,
    `food_category_name` VARCHAR(100) NOT NULL,
    `pickup_timestamp` DATETIME NOT NULL,
    `delivery_timestamp` DATETIME NOT NULL,
    `distribution_timestamp` DATETIME NOT NULL,
    `distance_covered_km` DECIMAL(8,2) NOT NULL DEFAULT 0.00,
    `verification_hash_sha256` VARCHAR(64) NOT NULL,
    `issued_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_rec_number` (`receipt_number`),
    UNIQUE KEY `uq_rec_pickup` (`pickup_id`),
    CONSTRAINT `fk_drr_listing` FOREIGN KEY (`food_listing_id`) REFERENCES `food_listings`(`id`),
    CONSTRAINT `fk_drr_pickup` FOREIGN KEY (`pickup_id`) REFERENCES `pickups`(`id`),
    CONSTRAINT `fk_drr_donor` FOREIGN KEY (`donor_user_id`) REFERENCES `users`(`id`),
    CONSTRAINT `fk_drr_ngo` FOREIGN KEY (`ngo_user_id`) REFERENCES `users`(`id`),
    CONSTRAINT `fk_drr_vol` FOREIGN KEY (`volunteer_user_id`) REFERENCES `users`(`id`),
    INDEX `idx_rec_number` (`receipt_number`),
    INDEX `idx_rec_listing` (`food_listing_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 20. NOTIFICATIONS
-- --------------------------------------------------------------------
CREATE TABLE `notifications` (
    `id` BIGINT AUTO_INCREMENT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `notification_type` VARCHAR(50) NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `message` TEXT NOT NULL,
    `reference_type` VARCHAR(50) NULL,
    `reference_id` BIGINT NULL,
    `channel` ENUM('IN_APP', 'EMAIL', 'SMS') NOT NULL DEFAULT 'IN_APP',
    `is_read` BOOLEAN NOT NULL DEFAULT FALSE,
    `sent_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `read_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_notif_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    INDEX `idx_notif_user` (`user_id`, `is_read`, `sent_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 21. FEEDBACK
-- --------------------------------------------------------------------
CREATE TABLE `feedback` (
    `id` BIGINT AUTO_INCREMENT NOT NULL,
    `food_listing_id` BIGINT NOT NULL,
    `pickup_id` BIGINT NOT NULL,
    `submitter_user_id` BIGINT NOT NULL,
    `target_user_id` BIGINT NULL,
    `rating_stars` INT NOT NULL,
    `comment` TEXT NULL,
    `feedback_type` ENUM('DONOR_RATING', 'NGO_RATING', 'VOLUNTEER_RATING', 'FOOD_QUALITY') NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_fb_listing` FOREIGN KEY (`food_listing_id`) REFERENCES `food_listings`(`id`),
    CONSTRAINT `fk_fb_pickup` FOREIGN KEY (`pickup_id`) REFERENCES `pickups`(`id`),
    CONSTRAINT `fk_fb_submitter` FOREIGN KEY (`submitter_user_id`) REFERENCES `users`(`id`),
    CONSTRAINT `fk_fb_target` FOREIGN KEY (`target_user_id`) REFERENCES `users`(`id`),
    CONSTRAINT `chk_rating_range` CHECK (`rating_stars` BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 22. COMPLAINTS
-- --------------------------------------------------------------------
CREATE TABLE `complaints` (
    `id` BIGINT AUTO_INCREMENT NOT NULL,
    `reporter_user_id` BIGINT NOT NULL,
    `reported_user_id` BIGINT NULL,
    `food_listing_id` BIGINT NULL,
    `pickup_id` BIGINT NULL,
    `category` ENUM('HYGIENE', 'MISREPRESENTATION', 'NO_SHOW', 'SPOILED_FOOD', 'BEHAVIOR', 'OTHER') NOT NULL,
    `description` TEXT NOT NULL,
    `status` ENUM('OPEN', 'UNDER_REVIEW', 'RESOLVED', 'DISMISSED') NOT NULL DEFAULT 'OPEN',
    `resolution_notes` TEXT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_comp_reporter` FOREIGN KEY (`reporter_user_id`) REFERENCES `users`(`id`),
    CONSTRAINT `fk_comp_reported` FOREIGN KEY (`reported_user_id`) REFERENCES `users`(`id`),
    CONSTRAINT `fk_comp_listing` FOREIGN KEY (`food_listing_id`) REFERENCES `food_listings`(`id`),
    CONSTRAINT `fk_comp_pickup` FOREIGN KEY (`pickup_id`) REFERENCES `pickups`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 23. AUDIT LOGS
-- --------------------------------------------------------------------
CREATE TABLE `audit_logs` (
    `id` BIGINT AUTO_INCREMENT NOT NULL,
    `user_id` BIGINT NULL,
    `user_role` VARCHAR(20) NULL,
    `event_type` VARCHAR(100) NOT NULL,
    `ip_address` VARCHAR(45) NOT NULL,
    `user_agent` VARCHAR(255) NULL,
    `action_description` VARCHAR(500) NOT NULL,
    `affected_entity` VARCHAR(50) NOT NULL,
    `entity_id` BIGINT NULL,
    `status` ENUM('SUCCESS', 'FAILURE') NOT NULL,
    `details_json` TEXT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_al_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE SET NULL,
    INDEX `idx_audit_event` (`event_type`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 24. IMPACT RECORDS
-- --------------------------------------------------------------------
CREATE TABLE `impact_records` (
    `id` BIGINT AUTO_INCREMENT NOT NULL,
    `food_listing_id` BIGINT NOT NULL,
    `pickup_id` BIGINT NOT NULL,
    `ngo_user_id` BIGINT NOT NULL,
    `donor_user_id` BIGINT NOT NULL,
    `volunteer_user_id` BIGINT NULL,
    `rescued_quantity_kg` DECIMAL(10,2) NOT NULL,
    `estimated_people_fed` INT NOT NULL,
    `co2_emissions_prevented_kg` DECIMAL(10,2) NOT NULL,
    `recorded_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_impact_pickup` (`pickup_id`),
    CONSTRAINT `fk_ir_listing` FOREIGN KEY (`food_listing_id`) REFERENCES `food_listings`(`id`),
    CONSTRAINT `fk_ir_pickup` FOREIGN KEY (`pickup_id`) REFERENCES `pickups`(`id`),
    CONSTRAINT `fk_ir_ngo` FOREIGN KEY (`ngo_user_id`) REFERENCES `users`(`id`),
    CONSTRAINT `fk_ir_donor` FOREIGN KEY (`donor_user_id`) REFERENCES `users`(`id`),
    CONSTRAINT `fk_ir_vol` FOREIGN KEY (`volunteer_user_id`) REFERENCES `users`(`id`),
    INDEX `idx_impact_date` (`recorded_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- 25. RESCUE BOTTLENECKS
-- --------------------------------------------------------------------
CREATE TABLE `rescue_bottlenecks` (
    `id` BIGINT AUTO_INCREMENT NOT NULL,
    `bottleneck_type` VARCHAR(50) NOT NULL,
    `severity` ENUM('INFO', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL,
    `food_listing_id` BIGINT NULL,
    `pickup_id` BIGINT NULL,
    `ngo_user_id` BIGINT NULL,
    `volunteer_user_id` BIGINT NULL,
    `description` VARCHAR(500) NOT NULL,
    `status` ENUM('DETECTED', 'ACTIVE', 'RESOLVED') NOT NULL DEFAULT 'ACTIVE',
    `detected_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `resolved_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_rb_listing` FOREIGN KEY (`food_listing_id`) REFERENCES `food_listings`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_rb_pickup` FOREIGN KEY (`pickup_id`) REFERENCES `pickups`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_rb_ngo` FOREIGN KEY (`ngo_user_id`) REFERENCES `users`(`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_rb_vol` FOREIGN KEY (`volunteer_user_id`) REFERENCES `users`(`id`) ON DELETE SET NULL,
    INDEX `idx_rb_status` (`status`, `severity`),
    INDEX `idx_rb_type` (`bottleneck_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- GLOBAL REFERENCE DATA SEEDING (PRODUCTION-READY REFERENCE ONLY)
-- (No fake production users, NGOs, or donations)
-- ====================================================================

-- Countries
INSERT INTO `countries` (`country_code`, `country_name`, `phone_code`, `default_currency`, `default_timezone`, `distance_unit`, `is_active`) VALUES
('IN', 'India', '+91', 'INR', 'Asia/Kolkata', 'KM', TRUE),
('US', 'United States', '+1', 'USD', 'America/New_York', 'MI', TRUE),
('GB', 'United Kingdom', '+44', 'GBP', 'Europe/London', 'MI', TRUE),
('CA', 'Canada', '+1', 'CAD', 'America/Toronto', 'KM', TRUE),
('AU', 'Australia', '+61', 'AUD', 'Australia/Sydney', 'KM', TRUE),
('DE', 'Germany', '+49', 'EUR', 'Europe/Berlin', 'KM', TRUE),
('FR', 'France', '+33', 'EUR', 'Europe/Paris', 'KM', TRUE),
('SG', 'Singapore', '+65', 'SGD', 'Asia/Singapore', 'KM', TRUE);

-- Regions
INSERT INTO `regions` (`country_code`, `region_code`, `region_name`) VALUES
('IN', 'MH', 'Maharashtra'),
('IN', 'DL', 'Delhi'),
('IN', 'KA', 'Karnataka'),
('IN', 'TN', 'Tamil Nadu'),
('IN', 'TG', 'Telangana'),
('IN', 'UP', 'Uttar Pradesh'),
('US', 'NY', 'New York'),
('US', 'CA', 'California'),
('US', 'TX', 'Texas'),
('US', 'IL', 'Illinois'),
('GB', 'ENG', 'England'),
('GB', 'SCT', 'Scotland'),
('CA', 'ON', 'Ontario'),
('CA', 'BC', 'British Columbia'),
('AU', 'NSW', 'New South Wales'),
('AU', 'VIC', 'Victoria');

-- Languages
INSERT INTO `languages` (`lang_code`, `lang_name`, `native_name`, `is_active`) VALUES
('en', 'English', 'English', TRUE),
('hi', 'Hindi', 'हिन्दी', TRUE),
('es', 'Spanish', 'Español', TRUE),
('fr', 'French', 'Français', TRUE);

-- Timezones
INSERT INTO `timezones` (`tz_name`, `utc_offset_minutes`, `country_code`) VALUES
('Asia/Kolkata', 330, 'IN'),
('America/New_York', -300, 'US'),
('America/Chicago', -360, 'US'),
('America/Denver', -420, 'US'),
('America/Los_Angeles', -480, 'US'),
('Europe/London', 0, 'GB'),
('America/Toronto', -300, 'CA'),
('America/Vancouver', -480, 'CA'),
('Australia/Sydney', 600, 'AU'),
('Australia/Melbourne', 600, 'AU'),
('Europe/Berlin', 60, 'DE'),
('Europe/Paris', 60, 'FR'),
('Asia/Singapore', 480, 'SG'),
('UTC', 0, 'GB');

-- Measurement Units
INSERT INTO `measurement_units` (`unit_code`, `unit_name`, `unit_type`, `conversion_factor_to_kg`) VALUES
('KG', 'Kilogram', 'WEIGHT', 1.0000),
('G', 'Gram', 'WEIGHT', 0.0010),
('L', 'Liter', 'VOLUME', 1.0000),
('ML', 'Milliliter', 'VOLUME', 0.0010),
('PORTIONS', 'Meal Portions', 'COUNT', 0.4200),
('PACKETS', 'Packets', 'COUNT', 0.3500),
('BOXES', 'Boxes', 'COUNT', 5.0000),
('CRATES', 'Crates', 'COUNT', 15.0000);

-- Food Categories
INSERT INTO `food_categories` (`category_code`, `category_name`, `is_perishable`, `default_shelf_life_hours`, `default_storage_condition`, `urgency_weight`) VALUES
('COOKED_MEALS', 'Prepared Cooked Meals', TRUE, 4, 'WARM_HEATED', 1.50),
('BAKED_GOODS', 'Baked Goods & Breads', TRUE, 24, 'ROOM_TEMP', 1.20),
('DAIRY', 'Dairy & Refrigerated Items', TRUE, 12, 'REFRIGERATED', 1.30),
('FRESH_PRODUCE', 'Fresh Produce & Fruits', TRUE, 48, 'ROOM_TEMP', 1.10),
('PACKAGED_NONPERISHABLE', 'Packaged & Dry Groceries', FALSE, 720, 'ROOM_TEMP', 0.70),
('BEVERAGES', 'Beverages & Juices', TRUE, 12, 'REFRIGERATED', 1.00),
('FROZEN_FOODS', 'Frozen Foods', TRUE, 48, 'FROZEN', 1.10);

-- Verification Rules
INSERT INTO `verification_rules` (`country_code`, `rule_code`, `rule_name`, `min_established_years`, `requires_org_document`, `requires_registration_doc`, `requires_address_proof`, `is_active`) VALUES
('IN', 'IN_NGO_STD', 'India Standard NGO Compliance Framework', 1, TRUE, TRUE, TRUE, TRUE),
('US', 'US_NGO_STD', 'US Recognized Non-Profit Public Charity Standard', 1, TRUE, TRUE, TRUE, TRUE),
('GB', 'GB_CHARITY_STD', 'UK Registered Charity Standard (Charity Commission)', 1, FALSE, TRUE, TRUE, TRUE),
('CA', 'CA_CRA_CHARITY', 'Canada Registered Charity Standard (CRA)', 1, TRUE, TRUE, TRUE, TRUE),
('AU', 'AU_ACNC_STD', 'Australia ACNC Registered Charity Standard', 1, TRUE, TRUE, TRUE, TRUE);
