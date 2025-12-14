package app.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa la tabla 'signatures'.
 * 
 * Almacena la ruta y metadatos de las firmas digitales capturadas
 * como evidencia de recepción de servicios.
 * 
 * Relaciones:
 * - Una firma pertenece a un único ServiceDelivery (relación 1:1)
 */
@Entity
@Table(name = "signatures")
public class SignatureEntity {
    /** Identificador único de la firma (clave primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_signature")
    private Long idSignature;

    /** Ruta del archivo de firma en el sistema de almacenamiento. */
    @Column(name = "signature_path", nullable = false)
    private String signaturePath;

    /** Fecha y hora en que la firma fue capturada. */
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