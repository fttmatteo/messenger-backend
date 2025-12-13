package app.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa la tabla 'signatures'.
 * Almacena la ruta y fecha de las firmas digitales capturadas.
 */
@Entity
@Table(name = "signatures")
public class SignatureEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_signature")
    private Long idSignature;

    @Column(name = "signature_path", nullable = false)
    private String signaturePath;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

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