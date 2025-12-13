package app.adapter.in.rest.response;

import app.domain.model.enums.PhotoType;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para evidencias fotográficas.
 * 
 * Incluye la ruta de acceso, fecha de carga y tipo de foto (ENTREGA, FALLIDA,
 * etc).
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
