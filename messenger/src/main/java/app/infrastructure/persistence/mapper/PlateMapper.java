package app.infrastructure.persistence.mapper;

import app.domain.model.Plate;
import app.domain.model.enums.PlateType;
import app.infrastructure.persistence.entities.PlateEntity;

public class PlateMapper {

    public static PlateEntity toEntity(Plate plate) {
        if (plate == null)
            return null;
        PlateEntity entity = new PlateEntity();
        entity.setIdPlate(plate.getIdPlate());
        entity.setPlateNumber(plate.getPlateNumber());
        entity.setUploadDate(plate.getUploadDate());
        if (plate.getPlateType() != null) {
            entity.setPlateType(plate.getPlateType().name());
        }
        return entity;
    }

    public static Plate toDomain(PlateEntity entity) {
        if (entity == null)
            return null;
        Plate plate = new Plate();
        plate.setIdPlate(entity.getIdPlate());
        plate.setPlateNumber(entity.getPlateNumber());
        plate.setUploadDate(entity.getUploadDate());
        if (entity.getPlateType() != null) {
            plate.setPlateType(PlateType.valueOf(entity.getPlateType()));
        }
        return plate;
    }
}