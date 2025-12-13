package app.domain.model;

import app.domain.model.enums.PhotoType;
import java.time.LocalDateTime;

/**
 * Modelo de dominio que representa una fotografía de evidencia de entrega.
 * 
 * <p>
 * Las fotos se utilizan como evidencia visual del proceso de entrega, pudiendo
 * ser:
 * </p>
 * <ul>
 * <li><strong>DETECTION:</strong> Foto de la placa vehicular detectada por
 * OCR</li>
 * <li><strong>EVIDENCE:</strong> Foto probatoria de la entrega realizada</li>
 * </ul>
 * 
 * <p>
 * Cada foto se almacena en el sistema de archivos y se referencia mediante su
 * ruta.
 * </p>
 * 
 * @see PhotoType
 * @see ServiceDelivery
 */
public class Photo {
    private Long idPhoto;
    private String photoPath;
    private LocalDateTime uploadDate;
    private PhotoType photoType;

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
}