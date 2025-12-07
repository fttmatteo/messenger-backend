package app.domain.ports;

import app.domain.model.Photo;
import java.util.List;

public interface PhotoPort {
    Photo save(Photo photo);

    Photo update(Photo photo);

    void deleteById(Long idPhoto);

    Photo findById(Long idPhoto);

    List<Photo> findAll();

    List<Photo> findByServiceDeliveryId(Long serviceDeliveryId);
}