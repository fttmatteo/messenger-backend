package app.domain.services;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.model.Photo;
import app.domain.model.ServiceDelivery;
import app.domain.model.Signature;
import app.domain.model.enums.PhotoType;
import app.domain.ports.PhotoPort;
import app.domain.ports.SignaturePort;

@Service
public class EvidenceService {

    @Autowired
    private PhotoPort photoPort;
    @Autowired
    private SignaturePort signaturePort;

    public void savePhoto(ServiceDelivery service, String path, PhotoType type) {
        if (path == null || path.isEmpty())
            return;

        Photo photo = new Photo();
        photo.setPhotoPath(path);
        photo.setPhotoType(type);
        photo.setUploadDate(LocalDateTime.now());
        photo.setServiceDelivery(service);
        photoPort.save(photo);
    }

    public void saveSignature(ServiceDelivery service, String path) {
        if (path == null || path.isEmpty())
            return;

        Signature signature = new Signature();
        signature.setSignaturePath(path);
        signature.setUploadDate(LocalDateTime.now());
        signature.setServiceDelivery(service);
        signaturePort.save(signature);
    }
}