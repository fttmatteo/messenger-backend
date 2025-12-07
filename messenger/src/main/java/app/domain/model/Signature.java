package app.domain.model;

import java.time.LocalDateTime;

public class Signature {
    private Long idSignature;
    private String signaturePath;
    private LocalDateTime uploadDate;
    private ServiceDelivery serviceDelivery;

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

    public ServiceDelivery getServiceDelivery() {
        return serviceDelivery;
    }

    public void setServiceDelivery(ServiceDelivery serviceDelivery) {
        this.serviceDelivery = serviceDelivery;
    }
}