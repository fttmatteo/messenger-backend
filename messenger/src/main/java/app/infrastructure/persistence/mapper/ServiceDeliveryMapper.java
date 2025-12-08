package app.infrastructure.persistence.mapper;

import app.domain.model.*;
import app.infrastructure.persistence.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class ServiceDeliveryMapper {

    @Autowired
    private PlateMapper plateMapper;
    @Autowired
    private DealershipMapper dealershipMapper;
    @Autowired
    private EmployeeMapper employeeMapper;

    public ServiceDelivery toDomain(ServiceDeliveryEntity entity) {
        if (entity == null)
            return null;

        ServiceDelivery domain = new ServiceDelivery();
        domain.setIdServiceDelivery(entity.getIdServiceDelivery());
        domain.setPlate(plateMapper.toDomain(entity.getPlate()));
        domain.setDealership(dealershipMapper.toDomain(entity.getDealership()));
        domain.setMessenger(employeeMapper.toDomain(entity.getMessenger()));
        domain.setCurrentStatus(entity.getCurrentStatus());
        domain.setObservation(entity.getObservation());

        if (entity.getSignature() != null) {
            Signature sig = new Signature();
            sig.setIdSignature(entity.getSignature().getIdSignature());
            sig.setSignaturePath(entity.getSignature().getSignaturePath());
            sig.setUploadDate(entity.getSignature().getUploadDate());
            domain.setSignature(sig);
        }

        if (entity.getPhotos() != null) {
            domain.setPhotos(entity.getPhotos().stream().map(p -> {
                Photo photo = new Photo();
                photo.setIdPhoto(p.getIdPhoto());
                photo.setPhotoPath(p.getPhotoPath());
                photo.setUploadDate(p.getUploadDate());
                photo.setPhotoType(p.getPhotoType());
                return photo;
            }).collect(Collectors.toList()));
        }

        if (entity.getHistory() != null) {
            domain.setHistory(entity.getHistory().stream().map(h -> {
                StatusHistory history = new StatusHistory();
                history.setIdStatusHistory(h.getIdStatusHistory());
                history.setPreviousStatus(h.getPreviousStatus());
                history.setNewStatus(h.getNewStatus());
                history.setChangeDate(h.getChangeDate());
                history.setChangedBy(employeeMapper.toDomain(h.getChangedBy()));
                return history;
            }).collect(Collectors.toList()));
        }

        return domain;
    }

    public ServiceDeliveryEntity toEntity(ServiceDelivery domain) {
        if (domain == null)
            return null;

        ServiceDeliveryEntity entity = new ServiceDeliveryEntity();
        entity.setIdServiceDelivery(domain.getIdServiceDelivery());
        entity.setPlate(plateMapper.toEntity(domain.getPlate()));
        entity.setDealership(dealershipMapper.toEntity(domain.getDealership()));
        entity.setMessenger(employeeMapper.toEntity(domain.getMessenger()));
        entity.setCurrentStatus(domain.getCurrentStatus());
        entity.setObservation(domain.getObservation());

        if (domain.getSignature() != null) {
            SignatureEntity sigEntity = new SignatureEntity();
            sigEntity.setIdSignature(domain.getSignature().getIdSignature());
            sigEntity.setSignaturePath(domain.getSignature().getSignaturePath());
            sigEntity.setUploadDate(domain.getSignature().getUploadDate());
            entity.setSignature(sigEntity);
        }

        if (domain.getPhotos() != null) {
            entity.setPhotos(domain.getPhotos().stream().map(p -> {
                PhotoEntity pEntity = new PhotoEntity();
                pEntity.setIdPhoto(p.getIdPhoto());
                pEntity.setPhotoPath(p.getPhotoPath());
                pEntity.setUploadDate(p.getUploadDate());
                pEntity.setPhotoType(p.getPhotoType());
                pEntity.setServiceDelivery(entity);
                return pEntity;
            }).collect(Collectors.toList()));
        }

        if (domain.getHistory() != null) {
            entity.setHistory(domain.getHistory().stream().map(h -> {
                StatusHistoryEntity hEntity = new StatusHistoryEntity();
                hEntity.setIdStatusHistory(h.getIdStatusHistory());
                hEntity.setPreviousStatus(h.getPreviousStatus());
                hEntity.setNewStatus(h.getNewStatus());
                hEntity.setChangeDate(h.getChangeDate());
                hEntity.setChangedBy(employeeMapper.toEntity(h.getChangedBy()));
                hEntity.setServiceDelivery(entity);
                return hEntity;
            }).collect(Collectors.toList()));
        }

        return entity;
    }
}