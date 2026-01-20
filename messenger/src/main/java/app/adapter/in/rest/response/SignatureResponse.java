package app.adapter.in.rest.response;

import java.time.LocalDateTime;

/**
 * DTO de respuesta con datos de firma digital.
 */
public class SignatureResponse {
    private Long idSignature;
    private String signaturePath;
    private LocalDateTime uploadDate;
    private String gifPath;

    public SignatureResponse() {
    }

    public SignatureResponse(Long idSignature, String signaturePath, LocalDateTime uploadDate) {
        this.idSignature = idSignature;
        this.signaturePath = signaturePath;
        this.uploadDate = uploadDate;
    }

    public SignatureResponse(Long idSignature, String signaturePath, LocalDateTime uploadDate, String gifPath) {
        this.idSignature = idSignature;
        this.signaturePath = signaturePath;
        this.uploadDate = uploadDate;
        this.gifPath = gifPath;
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

    public String getGifPath() {
        return gifPath;
    }

    public void setGifPath(String gifPath) {
        this.gifPath = gifPath;
    }
}
