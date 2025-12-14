package app.infrastructure.persistence.mapper;

import app.domain.model.Plate;
import app.infrastructure.persistence.entities.PlateEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper de persistencia para convertir entre Plate y PlateEntity.
 * Facilita la transformación de datos entre la capa de dominio y la base de
 * datos.
 */
@Component
public class PlateMapper {

    /**
     * Convierte un modelo de dominio Plate a su entidad JPA correspondiente.
     * 
     * Mapea número de placa, tipo de vehículo (CAR, MOTORCYCLE, MOTORCAR)
     * y fecha de carga para persistencia en base de datos.
     * 
     * @param domain El modelo de dominio a convertir (puede ser null)
     * @return La entidad JPA correspondiente, o null si el parámetro es null
     */
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

    /**
     * Convierte una entidad JPA PlateEntity a modelo de dominio.
     * 
     * Reconstruye el objeto de dominio desde la base de datos,
     * incluyendo número de placa detectado por OCR y tipo de vehículo.
     * 
     * @param entity La entidad JPA a convertir (puede ser null)
     * @return El modelo de dominio correspondiente, o null si la entidad es null
     */
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