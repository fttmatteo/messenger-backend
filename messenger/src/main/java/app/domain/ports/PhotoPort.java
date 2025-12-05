package app.domain.ports;

import app.domain.model.Photo;

public interface PhotoPort {
    void save(Photo photo) throws Exception;
    Photo findById(Long idPhoto) throws Exception;
}   
