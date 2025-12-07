package app.infrastructure.persistence.mapper;

import app.domain.model.Photo;
import app.domain.model.enums.FileType;
import app.domain.model.enums.PhotoType;
import app.infrastructure.persistence.entities.PhotoEntity;

public class PhotoMapper {
    public static PhotoEntity toEntity(Photo domain) {
        if (domain == null)
            return null;
        PhotoEntity entity = new PhotoEntity();
        entity.setIdPhoto(domain.getIdPhoto());
        entity.setPhotoPath(domain.getPhotoPath());
        entity.setUploadDate(domain.getUploadDate());
        if (domain.getFileType() != null)
            entity.setFileType(domain.getFileType().name());
        if (domain.getPhotoPurpose() != null)
            entity.setPhotoPurpose(domain.getPhotoPurpose().name());
        return entity;
    }

    public static Photo toDomain(PhotoEntity entity) {
        if (entity == null)
            return null;
        Photo photo = new Photo();
        photo.setIdPhoto(entity.getIdPhoto());
        photo.setPhotoPath(entity.getPhotoPath());
        photo.setUploadDate(entity.getUploadDate());
        if (entity.getFileType() != null)
            photo.setFileType(FileType.valueOf(entity.getFileType()));
        if (entity.getPhotoPurpose() != null)
            photo.setPhotoPurpose(PhotoType.valueOf(entity.getPhotoPurpose()));
        return photo;
    }
}