package org.annpath.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class VerificationDocument implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long ngoUserId;
    private String documentType; // REGISTRATION_CERTIFICATE, REGISTRATION_DOCUMENT, ORGANIZATION_DOCUMENT, ADDRESS_PROOF, IDENTITY_DOCUMENT, ANNUAL_REPORT, OTHER_SUPPORTING_DOCUMENT
    private String originalFileName;
    private String storedFileName;
    private String filePath;
    private long fileSizeBytes;
    private String mimeType;
    private String fileHashSha256;
    private Timestamp uploadedAt;

    public VerificationDocument() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getNgoUserId() { return ngoUserId; }
    public void setNgoUserId(Long ngoUserId) { this.ngoUserId = ngoUserId; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String storedFileName) { this.storedFileName = storedFileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getFileHashSha256() { return fileHashSha256; }
    public void setFileHashSha256(String fileHashSha256) { this.fileHashSha256 = fileHashSha256; }

    public Timestamp getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Timestamp uploadedAt) { this.uploadedAt = uploadedAt; }
}
