package app.domain.services;

import app.domain.model.Photo;
import app.domain.ports.PhotoPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreatePhoto {

    @Autowired
    private PhotoPort photoPort;

    public void create(Photo photo) throws Exception {
        photoPort.save(photo);
    }
}