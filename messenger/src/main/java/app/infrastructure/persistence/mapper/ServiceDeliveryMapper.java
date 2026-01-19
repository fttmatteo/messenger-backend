package app.infrastructure.persistence.mapper;

import app.domain.model.*;
import app.infrastructure.persistence.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper de persistencia entre ServiceDelivery y ServiceDeliveryEntity.
 */
@Component
public class ServiceDeliveryMapper {

    @Autowired
    private PlateMapper plateMapper;
    @Autowired
    private DealershipMapper dealershipMapper;
    @Autowired
    private EmployeeMapper employeeMapper;

    public ServiceDeliveryEntity toEntity(ServiceDelivery serviceDelivery) {
        if (serviceDelivery == null)
            return null;

        ServiceDeliveryEntity entity = new ServiceDeliveryEntity();
        entity.setIdServiceDelivery(serviceDelivery.getIdServiceDelivery());
        entity.setPlate(plateMapper.toEntity(serviceDelivery.getPlate()));
        entity.setDealership(dealershipMapper.toEntity(serviceDelivery.getDealership()));
        entity.setMessenger(employeeMapper.toEntity(serviceDelivery.getMessenger()));
        entity.setCurrentStatus(serviceDelivery.getCurrentStatus());
        entity.setObservation(serviceDelivery.getObservation());
        entity.setCreatedAt(serviceDelivery.getCreatedAt());
        entity.setDeleted(serviceDelivery.isDeleted());
        entity.setDeletedAt(serviceDelivery.getDeletedAt());
        entity.setLockedAt(serviceDelivery.getLockedAt());

        Map<Long, PhotoEntity> existingPhotoCache = new HashMap<>();
        Map<Photo, PhotoEntity> newPhotoCache = new IdentityHashMap<>();
        Map<Long, SignatureEntity> existingSignatureCache = new HashMap<>();
        Map<Signature, SignatureEntity> newSignatureCache = new IdentityHashMap<>();

        if (serviceDelivery.getSignature() != null) {
            SignatureEntity sigEntity = getOrCreateSignatureEntity(serviceDelivery.getSignature(),
                    existingSignatureCache,
                    newSignatureCache);
            entity.setSignature(sigEntity);
        }

        if (serviceDelivery.getPhotos() != null) {
            entity.setPhotos(serviceDelivery.getPhotos().stream().map(p -> {
                PhotoEntity pEntity = getOrCreatePhotoEntity(p, existingPhotoCache, newPhotoCache);
                pEntity.setServiceDelivery(entity);
                return pEntity;
            }).collect(Collectors.toList()));
        }

        if (serviceDelivery.getHistory() != null) {
            entity.setHistory(serviceDelivery.getHistory().stream().map(h -> {
                StatusHistoryEntity hEntity = new StatusHistoryEntity();
                hEntity.setIdStatusHistory(h.getIdStatusHistory());
                hEntity.setPreviousStatus(h.getPreviousStatus());
                hEntity.setNewStatus(h.getNewStatus());
                hEntity.setChangeDate(h.getChangeDate());
                hEntity.setChangedBy(employeeMapper.toEntity(h.getChangedBy()));
                hEntity.setDeliveryLatitude(h.getDeliveryLatitude());
                hEntity.setDeliveryLongitude(h.getDeliveryLongitude());
                hEntity.setServiceDelivery(entity);
                hEntity.setObservation(h.getObservation());

                if (h.getSignature() != null) {
                    SignatureEntity sEntity = getOrCreateSignatureEntity(h.getSignature(), existingSignatureCache,
                            newSignatureCache);
                    hEntity.setSignature(sEntity);
                }

                if (h.getPhotos() != null) {
                    hEntity.setPhotos(h.getPhotos().stream().map(p -> {
                        PhotoEntity pEntity = getOrCreatePhotoEntity(p, existingPhotoCache, newPhotoCache);
                        pEntity.setStatusHistory(hEntity);
                        pEntity.setServiceDelivery(entity);
                        return pEntity;
                    }).collect(Collectors.toList()));
                }
                return hEntity;
            }).collect(Collectors.toList()));
        }

        return entity;
    }

    private SignatureEntity getOrCreateSignatureEntity(Signature s, Map<Long, SignatureEntity> existingCache,
            Map<Signature, SignatureEntity> newCache) {
        if (s.getIdSignature() != null) {
            if (existingCache.containsKey(s.getIdSignature())) {
                return existingCache.get(s.getIdSignature());
            }
            SignatureEntity entity = mapSignatureBasic(s);
            existingCache.put(s.getIdSignature(), entity);
            return entity;
        } else {
            if (newCache.containsKey(s)) {
                return newCache.get(s);
            }
            SignatureEntity entity = mapSignatureBasic(s);
            newCache.put(s, entity);
            return entity;
        }
    }

    private SignatureEntity mapSignatureBasic(Signature s) {
        SignatureEntity sigEntity = new SignatureEntity();
        sigEntity.setIdSignature(s.getIdSignature());
        sigEntity.setSignaturePath(s.getSignaturePath());
        sigEntity.setUploadDate(s.getUploadDate());
        sigEntity.setGifPath(s.getGifPath());
        return sigEntity;
    }

    private PhotoEntity getOrCreatePhotoEntity(Photo p, Map<Long, PhotoEntity> existingCache,
            Map<Photo, PhotoEntity> newCache) {
        if (p.getIdPhoto() != null) {
            if (existingCache.containsKey(p.getIdPhoto())) {
                return existingCache.get(p.getIdPhoto());
            }
            PhotoEntity entity = mapPhotoBasic(p);
            existingCache.put(p.getIdPhoto(), entity);
            return entity;
        } else {
            if (newCache.containsKey(p)) {
                return newCache.get(p);
            }
            PhotoEntity entity = mapPhotoBasic(p);
            newCache.put(p, entity);
            return entity;
        }
    }

    private PhotoEntity mapPhotoBasic(Photo p) {
        PhotoEntity pEntity = new PhotoEntity();
        pEntity.setIdPhoto(p.getIdPhoto());
        pEntity.setPhotoPath(p.getPhotoPath());
        pEntity.setUploadDate(p.getUploadDate());
        pEntity.setPhotoType(p.getPhotoType());
        return pEntity;
    }

    public ServiceDelivery toDomain(ServiceDeliveryEntity entity) {
        if (entity == null)
            return null;

        ServiceDelivery serviceDelivery = new ServiceDelivery();
        serviceDelivery.setIdServiceDelivery(entity.getIdServiceDelivery());
        serviceDelivery.setPlate(plateMapper.toDomain(entity.getPlate()));
        serviceDelivery.setDealership(dealershipMapper.toDomain(entity.getDealership()));
        serviceDelivery.setMessenger(employeeMapper.toDomain(entity.getMessenger()));
        serviceDelivery.setCurrentStatus(entity.getCurrentStatus());
        serviceDelivery.setObservation(entity.getObservation());

        if (entity.getSignature() != null) {
            Signature signature = new Signature();
            signature.setIdSignature(entity.getSignature().getIdSignature());
            signature.setSignaturePath(entity.getSignature().getSignaturePath());
            signature.setUploadDate(entity.getSignature().getUploadDate());
            signature.setGifPath(entity.getSignature().getGifPath());
            serviceDelivery.setSignature(signature);
        }

        if (entity.getPhotos() != null) {
            serviceDelivery.setPhotos(entity.getPhotos().stream()
                    .map(this::mapPhotoToDomain)
                    .collect(Collectors.toList()));
        }

        if (entity.getHistory() != null) {
            serviceDelivery.setHistory(entity.getHistory().stream().map(h -> {
                StatusHistory history = new StatusHistory();
                history.setIdStatusHistory(h.getIdStatusHistory());
                history.setPreviousStatus(h.getPreviousStatus());
                history.setNewStatus(h.getNewStatus());
                history.setChangeDate(h.getChangeDate());
                history.setChangedBy(employeeMapper.toDomain(h.getChangedBy()));
                history.setDeliveryLatitude(h.getDeliveryLatitude());
                history.setDeliveryLongitude(h.getDeliveryLongitude());
                history.setObservation(h.getObservation());

                if (h.getSignature() != null) {
                    Signature signature = new Signature();
                    signature.setIdSignature(h.getSignature().getIdSignature());
                    signature.setSignaturePath(h.getSignature().getSignaturePath());
                    signature.setUploadDate(h.getSignature().getUploadDate());
                    signature.setGifPath(h.getSignature().getGifPath());
                    history.setSignature(signature);
                }
                if (h.getPhotos() != null) {
                    history.setPhotos(h.getPhotos().stream()
                            .map(this::mapPhotoToDomain)
                            .collect(Collectors.toList()));
                }
                return history;
            }).collect(Collectors.toList()));
        }

        serviceDelivery.setCreatedAt(entity.getCreatedAt());
        serviceDelivery.setDeleted(entity.isDeleted());
        serviceDelivery.setDeletedAt(entity.getDeletedAt());
        serviceDelivery.setLockedAt(entity.getLockedAt());

        return serviceDelivery;
    }

    private Photo mapPhotoToDomain(PhotoEntity p) {
        Photo photo = new Photo();
        photo.setIdPhoto(p.getIdPhoto());
        photo.setPhotoPath(p.getPhotoPath());
        photo.setUploadDate(p.getUploadDate());
        photo.setPhotoType(p.getPhotoType());
        return photo;
    }
}