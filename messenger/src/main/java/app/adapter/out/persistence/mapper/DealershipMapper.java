package app.adapter.out.persistence.mapper;

import app.domain.model.Dealership;
import app.adapter.out.persistence.entities.DealershipEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper de persistencia entre Dealership y DealershipEntity.
 */
@Component
public class DealershipMapper {

    public DealershipEntity toEntity(Dealership dealership) {
        if (dealership == null)
            return null;
        DealershipEntity entity = new DealershipEntity();
        entity.setIdDealership(dealership.getIdDealership());
        entity.setUuid(dealership.getUuid());
        entity.setName(dealership.getName());
        entity.setAddress(dealership.getAddress());
        entity.setPhone(dealership.getPhone());
        entity.setZone(dealership.getZone());
        entity.setLatitude(dealership.getLatitude());
        entity.setLongitude(dealership.getLongitude());
        entity.setIsGeolocated(dealership.getIsGeolocated());
        entity.setWhatsappPin(dealership.getWhatsappPin());
        return entity;
    }

    public Dealership toDomain(DealershipEntity entity) {
        if (entity == null)
            return null;
        Dealership dealership = new Dealership();
        dealership.setIdDealership(entity.getIdDealership());
        dealership.setUuid(entity.getUuid());
        dealership.setName(entity.getName());
        dealership.setAddress(entity.getAddress());
        dealership.setPhone(entity.getPhone());
        dealership.setZone(entity.getZone());
        dealership.setLatitude(entity.getLatitude());
        dealership.setLongitude(entity.getLongitude());
        dealership.setIsGeolocated(entity.getIsGeolocated());
        dealership.setWhatsappPin(entity.getWhatsappPin());
        return dealership;
    }
}