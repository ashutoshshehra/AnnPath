package org.annpath.model;

import java.io.Serializable;

public class VerificationRule implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String countryCode;
    private String ruleCode;
    private String ruleName;
    private int minEstablishedYears;
    private boolean requiresOrgDocument;
    private boolean requiresRegistrationDoc;
    private boolean requiresAddressProof;
    private boolean active;

    public VerificationRule() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public int getMinEstablishedYears() { return minEstablishedYears; }
    public void setMinEstablishedYears(int minEstablishedYears) { this.minEstablishedYears = minEstablishedYears; }

    public boolean isRequiresOrgDocument() { return requiresOrgDocument; }
    public void setRequiresOrgDocument(boolean requiresOrgDocument) { this.requiresOrgDocument = requiresOrgDocument; }
    public boolean isRequiresTaxCertificate() { return requiresOrgDocument; }
    public void setRequiresTaxCertificate(boolean requiresTaxCertificate) { this.requiresOrgDocument = requiresTaxCertificate; }

    public boolean isRequiresRegistrationDoc() { return requiresRegistrationDoc; }
    public void setRequiresRegistrationDoc(boolean requiresRegistrationDoc) { this.requiresRegistrationDoc = requiresRegistrationDoc; }

    public boolean isRequiresAddressProof() { return requiresAddressProof; }
    public void setRequiresAddressProof(boolean requiresAddressProof) { this.requiresAddressProof = requiresAddressProof; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
