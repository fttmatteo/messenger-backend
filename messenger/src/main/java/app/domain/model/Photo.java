package app.domain.model;

import java.time.LocalDateTime;
import app.domain.model.enums.FileType;
import app.domain.model.enums.PhotoType;

public class Photo {
    private Long idPhoto;
    private String photoPath;
    private FileType fileType;
    private LocalDateTime uploadDate;
    private Service service;
    private PhotoType photoPurpose;

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

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public PhotoType getPhotoPurpose() {
        return photoPurpose;
    }

    public void setPhotoPurpose(PhotoType photoPurpose) {
        this.photoPurpose = photoPurpose;
    }

}
