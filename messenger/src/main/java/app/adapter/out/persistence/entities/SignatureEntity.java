package app.adapter.out.persistence.entities;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa la tabla 'signatures'.
 */
@Entity
@Table(name = "signatures")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "signatures")
public class SignatureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_signature")
    private Long idSignature;

    @Column(name = "signature_path", nullable = false, length = 2048)
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