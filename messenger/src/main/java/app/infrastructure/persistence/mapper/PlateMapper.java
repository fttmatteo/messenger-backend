package app.infrastructure.persistence.mapper;

import app.domain.model.Plate;
import app.infrastructure.persistence.entities.PlateEntity;
import org.springframework.stereotype.Component;

@Component
public class PlateMapper {

    public PlateEntity toEntity(Plate domain) {
        if (domain == null)
            return null;
        PlateEntity entity = new PlateEntity();
        entity.setIdPlate(domain.getIdPlate());
        entity.setPlateNumber(domain.getPlateNumber());
        entity.setPlateType(domain.getPlateType());
        entity.setUploadDate(domain.getUploadDate());
        return entity;
    }

    public Plate toDomain(PlateEntity entity) {
        if (entity == null)
            return null;
        Plate model = new Plate();
        model.setIdPlate(entity.getIdPlate());
        model.setPlateNumber(entity.getPlateNumber());
        model.setPlateType(entity.getPlateType());
        model.setUploadDate(entity.getUploadDate());
        return model;
    }

}