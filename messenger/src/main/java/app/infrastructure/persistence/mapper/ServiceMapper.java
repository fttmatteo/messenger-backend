package app.infrastructure.persistence.mapper;

import app.domain.model.Service;
import app.domain.model.enums.Status;
import app.infrastructure.persistence.entities.ServiceEntity;

public class ServiceMapper {

    public static ServiceEntity toEntity(Service domain) {
        if (domain == null)
            return null;
        ServiceEntity entity = new ServiceEntity();
        entity.setIdService(domain.getIdService());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().name() : null);
        entity.setObservation(domain.getObservation());
        entity.setPlate(PlateMapper.toEntity(domain.getPlate()));
        entity.setEmployee(EmployeeMapper.toEntity(domain.getEmployee()));
        entity.setDealership(DealershipMapper.toEntity(domain.getDealership()));
        entity.setSignature(SignatureMapper.toEntity(domain.getSignature()));
        entity.setVisitPhoto(PhotoMapper.toEntity(domain.getVisitPhoto()));
        entity.setAssignedDate(domain.getAssignedDate());
        entity.setDeliveredDate(domain.getDeliveredDate());
        entity.setPendingDate(domain.getPendingDate());
        entity.setFailedDate(domain.getFailedDate());
        entity.setReturnedDate(domain.getReturnedDate());
        entity.setCanceledDate(domain.getCanceledDate());
        entity.setObservedDate(domain.getObservedDate());
        entity.setResolvedDate(domain.getResolvedDate());
        return entity;
    }

    public static Service toDomain(ServiceEntity entity) {
        if (entity == null)
            return null;
        Service service = new Service();
        service.setIdService(entity.getIdService());
        if (entity.getStatus() != null)
            service.setStatus(Status.valueOf(entity.getStatus()));
        service.setObservation(entity.getObservation());
        service.setPlate(PlateMapper.toDomain(entity.getPlate()));
        service.setEmployee(EmployeeMapper.toDomain(entity.getEmployee()));
        service.setDealership(DealershipMapper.toDomain(entity.getDealership()));
        service.setSignature(SignatureMapper.toDomain(entity.getSignature()));
        service.setVisitPhoto(PhotoMapper.toDomain(entity.getVisitPhoto()));
        service.setAssignedDate(entity.getAssignedDate());
        service.setDeliveredDate(entity.getDeliveredDate());
        return service;
    }
}