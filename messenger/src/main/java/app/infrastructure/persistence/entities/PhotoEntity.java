package app.infrastructure.persistence.entities;

import app.domain.model.enums.PhotoType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa la tabla 'photos'.
 * Almacena metadatos de las evidencias fotográficas asociadas a servicios.
 */
@Entity
@Table(name = "photos")
public class PhotoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_photo")
    private Long idPhoto;

    @Column(name = "photo_path", nullable = false)
    private String photoPath;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "photo_type")
    private PhotoType photoType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_delivery_id")
    private ServiceDeliveryEntity serviceDelivery;

    public Long getIdPhoto() {
        return idPhoto;
    }

    public void setIdPhoto(Long idPhoto) {
        this.idPhoto = idPhoto;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public PhotoType getPhotoType() {
        return photoType;
    }

    public void setPhotoType(PhotoType photoType) {
        this.photoType = photoType;
    }

    public ServiceDeliveryEntity getServiceDelivery() {
        return serviceDelivery;
    }

    public void setServiceDelivery(ServiceDeliveryEntity serviceDelivery) {
        this.serviceDelivery = serviceDelivery;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_history_id")
    private StatusHistoryEntity statusHistory;

    public StatusHistoryEntity getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(StatusHistoryEntity statusHistory) {
        this.statusHistory = statusHistory;
    }
}