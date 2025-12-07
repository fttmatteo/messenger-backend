package app.adapter.in.builder;

import app.domain.model.Photo;
import app.domain.model.enums.FileType;
import app.domain.model.enums.PhotoType;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class PhotoBuilder {

    public Photo build(String photoUrl, PhotoType purpose) {
        if (photoUrl == null || photoUrl.trim().isEmpty()) {
            return null;
        }
        Photo photo = new Photo();
        photo.setPhotoPath(photoUrl);
        photo.setUploadDate(LocalDateTime.now());
        photo.setFileType(FileType.PHOTO);
        photo.setPhotoPurpose(purpose);
        return photo;
    }
}