package app.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA para firmas archivadas.
 */
@Entity
@Table(name = "deleted_signatures")
public class DeletedSignatureEntity {

    @Id
    @Column(name = "id_signature")
    private Long idSignature;

    @Column(name = "service_delivery_id", nullable = false)
    private Long serviceDeliveryId;

    @Column(name = "signature_path", nullable = false, length = 500)
    private String signaturePath;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Constructors
    public DeletedSignatureEntity() {
    }

    // Getters and Setters
    public Long getIdSignature() {
        return idSignature;
    }

    public void setIdSignature(Long idSignature) {
        this.idSignature = idSignature;
    }

    public Long getServiceDeliveryId() {
        return serviceDeliveryId;
    }

    public void setServiceDeliveryId(Long serviceDeliveryId) {
        this.serviceDeliveryId = serviceDeliveryId;
    }

    public String getSignaturePath() {
        return signaturePath;
    }

    public void setSignaturePath(String signaturePath) {
        this.signaturePath = signaturePath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
