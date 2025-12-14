package app.infrastructure.persistence.mapper;

import app.domain.model.Dealership;
import app.infrastructure.persistence.entities.DealershipEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper de persistencia para convertir entre Dealership y DealershipEntity.
 * Facilita la transformación de datos entre la capa de dominio y la base de
 * datos.
 */
@Component
public class DealershipMapper {

    /**
     * Convierte un modelo de dominio Dealership a su entidad JPA correspondiente.
     * 
     * Mapea todos los campos del dealership incluyendo coordenadas geográficas
     * y estado de geolocalización para persistencia en base de datos.
     * 
     * @param dealership El modelo de dominio a convertir (puede ser null)
     * @return La entidad JPA correspondiente, o null si el parámetro es null
     */
    public DealershipEntity toEntity(Dealership dealership) {
        if (dealership == null)
            return null;
        DealershipEntity entity = new DealershipEntity();
        entity.setIdDealership(dealership.getIdDealership());
        entity.setName(dealership.getName());
        entity.setAddress(dealership.getAddress());
        entity.setPhone(dealership.getPhone());
        entity.setZone(dealership.getZone());
        entity.setLatitude(dealership.getLatitude());
        entity.setLongitude(dealership.getLongitude());
        entity.setIsGeolocated(dealership.getIsGeolocated());
        return entity;
    }

    /**
     * Convierte una entidad JPA DealershipEntity a modelo de dominio.
     * 
     * Reconstruye el objeto de dominio completo desde la base de datos,
     * incluyendo todos los datos de ubicación y contacto.
     * 
     * @param entity La entidad JPA a convertir (puede ser null)
     * @return El modelo de dominio correspondiente, o null si la entidad es null
     */
    public Dealership toDomain(DealershipEntity entity) {
        if (entity == null)
            return null;
        Dealership dealership = new Dealership();
        dealership.setIdDealership(entity.getIdDealership());
        dealership.setName(entity.getName());
        dealership.setAddress(entity.getAddress());
        dealership.setPhone(entity.getPhone());
        dealership.setZone(entity.getZone());
        dealership.setLatitude(entity.getLatitude());
        dealership.setLongitude(entity.getLongitude());
        dealership.setIsGeolocated(entity.getIsGeolocated());
        return dealership;
    }
}