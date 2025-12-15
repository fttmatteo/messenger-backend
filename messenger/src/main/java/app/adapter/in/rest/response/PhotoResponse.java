package app.adapter.in.rest.response;

import app.domain.model.enums.PhotoType;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) de respuesta para evidencias fotográficas de
 * servicios de entrega.
 * 
 * Este objeto representa una fotografía asociada a un servicio de entrega,
 * utilizada como evidencia del estado del servicio (entrega exitosa, fallida,
 * etc.).
 * 
 * Campos incluidos:
 * - idPhoto: Identificador único de la fotografía
 * - photoPath: Ruta de acceso al archivo de imagen almacenado
 * - uploadDate: Fecha y hora de carga de la fotografía
 * - photoType: Tipo de evidencia (ENTREGA, FALLIDA, CANCELADA, etc.)
 * 
 * Las fotografías se almacenan en el sistema de archivos o en Google Cloud
 * Storage.
 * 
 * @see app.adapter.in.rest.controllers.ServiceDeliveryController
 * @see app.domain.model.Photo
 * @see app.domain.model.enums.PhotoType
 */
public class PhotoResponse {
    private Long idPhoto;
    private String photoPath;
    private LocalDateTime uploadDate;
    private PhotoType photoType;

    public PhotoResponse() {
    }

    public PhotoResponse(Long idPhoto, String photoPath, LocalDateTime uploadDate, PhotoType photoType) {
        this.idPhoto = idPhoto;
        this.photoPath = photoPath;
        this.uploadDate = uploadDate;
        this.photoType = photoType;
    }

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
