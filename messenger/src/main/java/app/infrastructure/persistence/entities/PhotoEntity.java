package app.infrastructure.persistence.entities;

import app.domain.model.enums.PhotoType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa la tabla 'photos'.
 * 
 * Almacena metadatos de las evidencias fotográficas asociadas a servicios
 * de entrega. Las fotos pueden ser de detección de placa o evidencias de
 * estado.
 * 
 * Relaciones:
 * - Una foto pertenece a un ServiceDelivery (relación N:1)
 * - Una foto puede estar asociada a un StatusHistory específico (relación N:1)
 */
@Entity
@Table(name = "photos")
public class PhotoEntity {

    /** Identificador único de la foto (clave primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_photo")
    private Long idPhoto;

    /** Ruta del archivo de foto en el sistema de almacenamiento. */
    @Column(name = "photo_path", nullable = false, length = 2048)
    private String photoPath;

    /** Fecha y hora en que la foto fue capturada/subida. */
    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    /** Tipo de foto (PLATE_DETECTION o EVIDENCE). */
    @Enumerated(EnumType.STRING)
    @Column(name = "photo_type")
    private PhotoType photoType;

    /** Servicio de entrega al que pertenece esta foto. */
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

    /** Historial de estado al que pertenece esta foto (si aplica). */
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