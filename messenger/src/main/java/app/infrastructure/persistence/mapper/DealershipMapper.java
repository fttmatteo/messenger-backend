package app.infrastructure.persistence.mapper;

import app.domain.model.Dealership;
import app.infrastructure.persistence.entities.DealershipEntity;

public class DealershipMapper {

    public static DealershipEntity toEntity(Dealership dealership) {
        if (dealership == null)
            return null;
        DealershipEntity entity = new DealershipEntity();
        entity.setIdDealership(dealership.getIdDealership());
        entity.setName(dealership.getName());
        entity.setAddress(dealership.getAddress());
        entity.setPhone(dealership.getPhone());
        entity.setZone(dealership.getZone());
        return entity;
    }

    public static Dealership toDomain(DealershipEntity entity) {
        if (entity == null)
            return null;
        Dealership dealership = new Dealership();
        dealership.setIdDealership(entity.getIdDealership());
        dealership.setName(entity.getName());
        dealership.setAddress(entity.getAddress());
        dealership.setPhone(entity.getPhone());
        dealership.setZone(entity.getZone());
        return dealership;
    }
}