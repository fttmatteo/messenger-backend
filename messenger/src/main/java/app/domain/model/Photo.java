// Archivo: app/domain/model/Photo.java
package app.domain.model;

import app.domain.model.enums.PhotoType;
import java.time.LocalDateTime;

public class Photo {
    private Long idPhoto;
    private String photoPath;
    private LocalDateTime uploadDate;
    private PhotoType photoType;
    // Eliminamos la referencia circular 'ServiceDelivery' del modelo de dominio puro
    // para evitar ciclos infinitos, a menos que sea estrictamente necesario para navegación.
    // Generalmente, se accede a la foto DESDE el servicio.

    public Long getIdPhoto() { return idPhoto; }
    public void setIdPhoto(Long idPhoto) { this.idPhoto = idPhoto; }
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
    public PhotoType getPhotoType() { return photoType; }
    public void setPhotoType(PhotoType photoType) { this.photoType = photoType; }
}