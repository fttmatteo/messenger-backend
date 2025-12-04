package app.domain.model;

import java.time.LocalDateTime;
import app.domain.model.enums.FileType;

public class Photo {
    private Long id_photo;
    private String photo_path;
    private FileType fileType;
    private LocalDateTime upload_date;
    private Service service;

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

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
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

}
