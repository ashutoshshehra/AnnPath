package org.annpath.service;

import org.annpath.config.AppConfig;
import org.annpath.config.Constants;
import org.annpath.dao.ConnectionPool;
import org.annpath.dao.DonorDao;
import org.annpath.dao.NgoDao;
import org.annpath.dao.UserDao;
import org.annpath.dao.VolunteerDao;
import org.annpath.exception.UnauthorizedException;
import org.annpath.exception.ValidationException;
import org.annpath.model.DonorProfile;
import org.annpath.model.NgoProfile;
import org.annpath.model.User;
import org.annpath.model.VolunteerProfile;
import org.annpath.security.InputSanitizer;
import org.annpath.security.PasswordHasher;

import java.sql.Connection;
import java.sql.SQLException;

public class AuthService {

    private final UserDao userDao = new UserDao();
    private final DonorDao donorDao = new DonorDao();
    private final NgoDao ngoDao = new NgoDao();
    private final VolunteerDao volunteerDao = new VolunteerDao();

    public void validateRegistrationInputs(String email, String phone, String phoneCountryCode, String password,
                                          String role, String lang, String timezone) throws ValidationException, SQLException {
        if (!InputSanitizer.isValidEmail(email)) {
            throw new ValidationException("Please provide a valid email address.");
        }
        if (!InputSanitizer.isValidPhone(phone)) {
            throw new ValidationException("Please provide a valid phone number.");
        }
        if (password == null || password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long.");
        }
        if (!Constants.ROLE_DONOR.equalsIgnoreCase(role) &&
            !Constants.ROLE_NGO.equalsIgnoreCase(role) &&
            !Constants.ROLE_VOLUNTEER.equalsIgnoreCase(role)) {
            throw new ValidationException("Invalid account role selected.");
        }

        // Check uniqueness
        if (userDao.findByEmail(email) != null) {
            throw new ValidationException("An account with this email address already exists.");
        }
    }

    /**
     * Register a new user and initialize role-specific profile inside a database transaction.
     */
    public User registerUser(String email, String phone, String phoneCountryCode, String password,
                             String role, String lang, String timezone) throws ValidationException, SQLException {
        // Validation
        validateRegistrationInputs(email, phone, phoneCountryCode, password, role, lang, timezone);

        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword(password, salt);

        User user = new User();
        user.setEmail(email.trim().toLowerCase());
        user.setPhoneNumber(phone.trim());
        user.setPhoneCountryCode(phoneCountryCode != null ? phoneCountryCode : "+91");
        user.setPasswordHash(hash);
        user.setPasswordSalt(salt);
        user.setRole(role.toUpperCase());
        user.setStatus("ACTIVE"); // For development convenience, activated upon creation; documents verified separately
        user.setPreferredLanguage(lang != null ? lang : "en");
        user.setPreferredTimezone(timezone != null ? timezone : "UTC");
        user.setEmailVerified(true);
        user.setPhoneVerified(true);

        Connection conn = ConnectionPool.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);
            Long userId = userDao.createUser(user, conn);
            user.setId(userId);

            // Initialize minimal role profile
            if (Constants.ROLE_DONOR.equalsIgnoreCase(role)) {
                DonorProfile dp = new DonorProfile();
                dp.setUserId(userId);
                dp.setDonorType("INDIVIDUAL");
                dp.setContactPerson("New Donor");
                dp.setAddressLine1("Pending Address Update");
                dp.setCity("City");
                dp.setStateRegion("Region");
                dp.setPostalCode("000000");
                dp.setCountryCode("IN");
                dp.setLatitude(null);
                dp.setLongitude(null);
                donorDao.save(dp, conn);
            } else if (Constants.ROLE_NGO.equalsIgnoreCase(role)) {
                NgoProfile np = new NgoProfile();
                np.setUserId(userId);
                np.setOrganizationName("Pending Organization Setup");
                np.setRegistrationNumber("PENDING");
                np.setRegistrationAuthority("Pending");
                np.setEstablishedYear(2020);
                np.setRepresentativeName("Representative");
                np.setRepresentativeDesignation("Director");
                np.setAddressLine1("Pending Address Update");
                np.setCity("City");
                np.setStateRegion("Region");
                np.setPostalCode("000000");
                np.setCountryCode("IN");
                np.setLatitude(null);
                np.setLongitude(null);
                np.setServiceRadiusKm(new java.math.BigDecimal("50.00"));
                np.setMaxDailyCapacityKg(new java.math.BigDecimal("500.00"));
                np.setVerificationStatus("VERIFIED");
                ngoDao.save(np, conn);
            } else if (Constants.ROLE_VOLUNTEER.equalsIgnoreCase(role)) {
                VolunteerProfile vp = new VolunteerProfile();
                vp.setUserId(userId);
                vp.setFirstName("New");
                vp.setLastName("Volunteer");
                vp.setAddressLine1("Pending Address Update");
                vp.setCity("City");
                vp.setStateRegion("Region");
                vp.setPostalCode("000000");
                vp.setCountryCode("IN");
                vp.setLatitude(null);
                vp.setLongitude(null);
                vp.setMaxTravelDistanceKm(new java.math.BigDecimal("10.00"));
                vp.setVehicleType("MOTORCYCLE");
                vp.setMaxConcurrentAssignments(2);
                vp.setAvailabilityStatus("AVAILABLE");
                volunteerDao.save(vp, conn);
            }

            conn.commit();
            return user;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Authenticate user with password verification and brute-force lockout protection.
     */
    public User authenticate(String email, String candidatePassword) throws UnauthorizedException, ValidationException, SQLException {
        if (email == null || email.trim().isEmpty() || candidatePassword == null || candidatePassword.isEmpty()) {
            throw new ValidationException("Please enter your email and password.");
        }

        User user = userDao.findByEmail(email.trim());
        if (user == null) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        if (user.isAccountLocked()) {
            throw new UnauthorizedException("Account is temporarily locked due to multiple failed login attempts. Please try again later.");
        }

        boolean valid = PasswordHasher.verifyPassword(candidatePassword, user.getPasswordSalt(), user.getPasswordHash());
        if (!valid) {
            int lockoutMins = AppConfig.getInt("security.accountLockoutMinutes", 15);
            userDao.recordFailedLogin(user.getId(), user.getFailedLoginAttempts(), lockoutMins);
            throw new UnauthorizedException("Invalid email or password.");
        }

        // Reset failed login attempts on successful authentication
        if (user.getFailedLoginAttempts() > 0) {
            userDao.resetFailedLogin(user.getId());
        }

        return user;
    }

    public void updatePassword(Long userId, String newPassword) throws ValidationException, SQLException {
        if (newPassword == null || newPassword.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long.");
        }
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword(newPassword, salt);
        userDao.updatePassword(userId, hash, salt);
    }

    public void resetPassword(String email, String otpCode, String newPassword, String confirmPassword) throws ValidationException, SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email address is required.");
        }
        if (otpCode == null || otpCode.trim().isEmpty()) {
            throw new ValidationException("Verification code is required.");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new ValidationException("New password must be at least 8 characters long.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match.");
        }

        User user = userDao.findByEmail(email.trim().toLowerCase());
        if (user == null) {
            throw new ValidationException("No account found with this email address.");
        }

        OtpService otpService = new OtpService();
        boolean valid = false;
        try {
            valid = otpService.verifyOtp(email.trim().toLowerCase(), "PASSWORD_RESET", otpCode);
        } catch (Exception e) {
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) {
                String fullPhone = (user.getPhoneCountryCode() != null ? user.getPhoneCountryCode() : "+91") + user.getPhoneNumber().trim();
                valid = otpService.verifyPhoneOtp(fullPhone, "PASSWORD_RESET", otpCode);
            } else {
                throw e;
            }
        }
        if (!valid) {
            throw new ValidationException("Invalid or expired verification code.");
        }

        updatePassword(user.getId(), newPassword);
    }
}
