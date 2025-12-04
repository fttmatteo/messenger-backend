package app.domain.model;

import java.time.LocalDateTime;
import app.domain.model.enums.FileType;
import app.domain.model.enums.PhotoPurpose;

public class Photo {
    private Long id_photo;
    private String photo_path;
    private FileType file_type;
    private LocalDateTime upload_date;
    private Service service;
    private PhotoPurpose photo_purpose;

    public Long getId_photo() {
        return id_photo;
    }

    public void setId_photo(Long id_photo) {
        this.id_photo = id_photo;
    }

    public String getPhoto_path() {
        return photo_path;
    }

    public void setPhoto_path(String photo_path) {
        this.photo_path = photo_path;
    }

    public FileType getFile_type() {
        return file_type;
    }

    public void setFile_type(FileType file_type) {
        this.file_type = file_type;
    }

    public LocalDateTime getUpload_date() {
        return upload_date;
    }

    public void setUpload_date(LocalDateTime upload_date) {
        this.upload_date = upload_date;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public PhotoPurpose getPhoto_purpose() {
        return photo_purpose;
    }

    public void sethoto_purpose(PhotoPurpose photo_purpose) {
        this.photo_purpose = photo_purpose;
    }

}
