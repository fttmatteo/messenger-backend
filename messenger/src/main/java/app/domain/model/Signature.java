// Archivo: app/domain/model/Signature.java
package app.domain.model;

import java.time.LocalDateTime;

public class Signature {
    private Long idSignature;
    private String signaturePath;
    private LocalDateTime uploadDate;

    public Long getIdSignature() { return idSignature; }
    public void setIdSignature(Long idSignature) { this.idSignature = idSignature; }
    public String getSignaturePath() { return signaturePath; }
    public void setSignaturePath(String signaturePath) { this.signaturePath = signaturePath; }
    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
}