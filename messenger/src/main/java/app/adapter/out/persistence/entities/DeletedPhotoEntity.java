package app.adapter.out.persistence.entities;

import app.domain.model.enums.PhotoType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA para fotos archivadas.
 */
@Entity
@Table(name = "deleted_photos")
public class DeletedPhotoEntity {

    @Id
    @Column(name = "id_photo")
    private Long idPhoto;

    @Column(name = "service_delivery_id", nullable = false)
    private Long serviceDeliveryId;

    @Column(name = "status_history_id")
    private Long statusHistoryId;

    @Column(name = "photo_path", nullable = false, length = 500)
    private String photoPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "photo_type", nullable = false)
    private PhotoType photoType;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    public DeletedPhotoEntity() {
    }

    public Long getIdPhoto() {
        return idPhoto;
    }

    public void setIdPhoto(Long idPhoto) {
        this.idPhoto = idPhoto;
    }

    public Long getServiceDeliveryId() {
        return serviceDeliveryId;
    }

    public void setServiceDeliveryId(Long serviceDeliveryId) {
        this.serviceDeliveryId = serviceDeliveryId;
    }

    public Long getStatusHistoryId() {
        return statusHistoryId;
    }

    public void setStatusHistoryId(Long statusHistoryId) {
        this.statusHistoryId = statusHistoryId;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public PhotoType getPhotoType() {
        return photoType;
    }

    public void setPhotoType(PhotoType photoType) {
        this.photoType = photoType;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}
