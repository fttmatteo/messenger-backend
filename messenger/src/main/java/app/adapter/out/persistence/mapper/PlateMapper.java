package app.adapter.out.persistence.mapper;

import app.domain.model.Plate;
import app.adapter.out.persistence.entities.PlateEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper de persistencia entre Plate y PlateEntity.
 */
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