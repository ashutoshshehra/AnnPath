package org.annpath.service;

import org.annpath.dao.NgoDao;
import org.annpath.dao.ReferenceDataDao;
import org.annpath.model.NgoProfile;
import org.annpath.model.VerificationDocument;
import org.annpath.model.VerificationRule;

import java.sql.SQLException;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class VerificationService {

    private static final Logger LOGGER = Logger.getLogger(VerificationService.class.getName());
    private final NgoDao ngoDao = new NgoDao();
    private final ReferenceDataDao refDao = new ReferenceDataDao();

    /**
     * Automated, country-rule-driven NGO verification evaluation.
     */
    public boolean evaluateNgoVerification(Long ngoUserId) throws SQLException {
        NgoProfile profile = ngoDao.findByUserId(ngoUserId);
        if (profile == null) return false;

        String countryCode = profile.getCountryCode() != null ? profile.getCountryCode() : "IN";
        VerificationRule rule = refDao.getVerificationRuleForCountry(countryCode);

        // If no rule exists, apply generic standard rule
        int minYears = (rule != null) ? rule.getMinEstablishedYears() : 1;
        boolean reqRegDoc = (rule != null) ? rule.isRequiresRegistrationDoc() : true;
        boolean reqTax = (rule != null) ? rule.isRequiresTaxCertificate() : false;
        boolean reqAddress = (rule != null) ? rule.isRequiresAddressProof() : true;

        List<VerificationDocument> docs = ngoDao.getDocumentsByNgoId(ngoUserId);
        Set<String> uploadedTypes = new HashSet<>();
        for (VerificationDocument d : docs) {
            uploadedTypes.add(d.getDocumentType());
        }

        int currentYear = Year.now().getValue();
        int age = currentYear - profile.getEstablishedYear();

        StringBuilder reasons = new StringBuilder();
        boolean eligible = true;

        if (age < minYears) {
            eligible = false;
            reasons.append("Organization must be established for at least ").append(minYears).append(" year(s). ");
        }

        if (reqRegDoc && !uploadedTypes.contains("REGISTRATION_CERTIFICATE")) {
            eligible = false;
            reasons.append("Missing mandatory Registration Certificate. ");
        }

        if (reqTax && (!uploadedTypes.contains("ORGANIZATION_DOCUMENT") && !uploadedTypes.contains("REGISTRATION_DOCUMENT"))) {
            eligible = false;
            reasons.append("Missing mandatory Non-Profit Organization Status / Verification document. ");
        }

        if (reqAddress && !uploadedTypes.contains("ADDRESS_PROOF")) {
            eligible = false;
            reasons.append("Missing proof of operational address. ");
        }

        if (eligible) {
            ngoDao.updateVerificationStatus(ngoUserId, "VERIFIED", null);
            LOGGER.info("NGO #" + ngoUserId + " successfully verified under rule: " + (rule != null ? rule.getRuleCode() : "GENERIC_STD"));
            return true;
        } else {
            ngoDao.updateVerificationStatus(ngoUserId, "PENDING_VERIFICATION", reasons.toString());
            LOGGER.info("NGO #" + ngoUserId + " verification pending: " + reasons.toString());
            return false;
        }
    }
}
