package app.infrastructure.persistence.mapper;

import app.domain.model.Dealership;
import app.infrastructure.persistence.entities.DealershipEntity;
import org.springframework.stereotype.Component;

@Component
public class DealershipMapper {

    public Dealership toDomain(DealershipEntity entity) {
        if (entity == null)
            return null;
        Dealership model = new Dealership();
        model.setIdDealership(entity.getIdDealership());
        model.setName(entity.getName());
        model.setAddress(entity.getAddress());
        model.setPhone(entity.getPhone());
        model.setZone(entity.getZone());
        return model;
    }

    public DealershipEntity toEntity(Dealership domain) {
        if (domain == null)
            return null;
        DealershipEntity entity = new DealershipEntity();
        entity.setIdDealership(domain.getIdDealership());
        entity.setName(domain.getName());
        entity.setAddress(domain.getAddress());
        entity.setPhone(domain.getPhone());
        entity.setZone(domain.getZone());
        return entity;
    }
}