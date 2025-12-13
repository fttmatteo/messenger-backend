package app.adapter.in.rest.response;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para firmas digitales.
 */
public class SignatureResponse {
    private Long idSignature;
    private String signaturePath;
    private LocalDateTime uploadDate;

    public SignatureResponse() {
    }

    public SignatureResponse(Long idSignature, String signaturePath, LocalDateTime uploadDate) {
        this.idSignature = idSignature;
        this.signaturePath = signaturePath;
        this.uploadDate = uploadDate;
    }

    public Long getIdSignature() {
        return idSignature;
    }

    public void setIdSignature(Long idSignature) {
        this.idSignature = idSignature;
    }

    public String getSignaturePath() {
        return signaturePath;
    }

    public void setSignaturePath(String signaturePath) {
        this.signaturePath = signaturePath;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}
