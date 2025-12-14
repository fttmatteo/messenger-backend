package app.infrastructure.persistence.mapper;

import app.domain.model.*;
import app.infrastructure.persistence.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

/**
 * Mapper de persistencia para convertir entre ServiceDelivery y
 * ServiceDeliveryEntity.
 * Maneja la conversión compleja incluyendo asociaciones con Placa,
 * Concesionario y Mensajero.
 */
@Component
public class ServiceDeliveryMapper {

    @Autowired
    private PlateMapper plateMapper;
    @Autowired
    private DealershipMapper dealershipMapper;
    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * Convierte un modelo de dominio ServiceDelivery a su entidad JPA
     * correspondiente.
     * 
     * Realiza una conversión compleja que incluye:
     * - Mapeo de la placa del vehículo (usando PlateMapper)
     * - Mapeo del concesionario (usando DealershipMapper)
     * - Mapeo del mensajero asignado (usando EmployeeMapper)
     * - Conversión de firma digital si existe
     * - Conversión de lista de fotos de evidencia
     * - Conversión de historial de cambios de estado con sus fotos asociadas
     * 
     * Todas las relaciones bidireccionales se configuran correctamente para
     * mantener la integridad referencial en JPA.
     * 
     * @param serviceDelivery El modelo de dominio a convertir (puede ser null)
     * @return La entidad JPA correspondiente con todas sus relaciones, o null si el
     *         parámetro es null
     */
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

        if (serviceDelivery.getSignature() != null) {
            SignatureEntity sigEntity = new SignatureEntity();
            sigEntity.setIdSignature(serviceDelivery.getSignature().getIdSignature());
            sigEntity.setSignaturePath(serviceDelivery.getSignature().getSignaturePath());
            sigEntity.setUploadDate(serviceDelivery.getSignature().getUploadDate());
            entity.setSignature(sigEntity);
        }

        if (serviceDelivery.getPhotos() != null) {
            entity.setPhotos(serviceDelivery.getPhotos().stream().map(p -> {
                PhotoEntity pEntity = new PhotoEntity();
                pEntity.setIdPhoto(p.getIdPhoto());
                pEntity.setPhotoPath(p.getPhotoPath());
                pEntity.setUploadDate(p.getUploadDate());
                pEntity.setPhotoType(p.getPhotoType());
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
                hEntity.setServiceDelivery(entity);
                if (h.getPhotos() != null) {
                    hEntity.setPhotos(h.getPhotos().stream().map(p -> {
                        PhotoEntity pEntity = new PhotoEntity();
                        pEntity.setIdPhoto(p.getIdPhoto());
                        pEntity.setPhotoPath(p.getPhotoPath());
                        pEntity.setUploadDate(p.getUploadDate());
                        pEntity.setPhotoType(p.getPhotoType());
                        pEntity.setStatusHistory(hEntity);
                        pEntity.setServiceDelivery(entity); // Maintain service link too? UpdateServiceDelivery logic
                                                            // will decide. Safest to set if possible, but might be
                                                            // redundant.
                        return pEntity;
                    }).collect(Collectors.toList()));
                }
                return hEntity;
            }).collect(Collectors.toList()));
        }

        return entity;
    }

    /**
     * Convierte una entidad JPA ServiceDeliveryEntity a modelo de dominio.
     * 
     * Reconstruye el objeto de dominio completo desde la base de datos, incluyendo:
     * - Datos de la placa del vehículo
     * - Información del concesionario de destino
     * - Datos del mensajero asignado
     * - Firma digital del responsable en concesionario
     * - Fotos de evidencia de la entrega
     * - Historial completo de cambios de estado
     * - Fotos asociadas a cada cambio de estado
     * 
     * Utiliza los mappers especializados (PlateMapper, DealershipMapper,
     * EmployeeMapper)
     * para convertir cada entidad relacionada a su correspondiente modelo de
     * dominio.
     * 
     * @param entity La entidad JPA a convertir (puede ser null)
     * @return El modelo de dominio completo con todas sus relaciones, o null si la
     *         entidad es null
     */
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
            serviceDelivery.setSignature(signature);
        }

        if (entity.getPhotos() != null) {
            serviceDelivery.setPhotos(entity.getPhotos().stream().map(p -> {
                Photo photo = new Photo();
                photo.setIdPhoto(p.getIdPhoto());
                photo.setPhotoPath(p.getPhotoPath());
                photo.setUploadDate(p.getUploadDate());
                photo.setPhotoType(p.getPhotoType());
                return photo;
            }).collect(Collectors.toList()));
        }

        if (entity.getHistory() != null) {
            serviceDelivery.setHistory(entity.getHistory().stream().map(h -> {
                StatusHistory history = new StatusHistory();
                history.setIdStatusHistory(h.getIdStatusHistory());
                history.setPreviousStatus(h.getPreviousStatus());
                history.setNewStatus(h.getNewStatus());
                history.setChangeDate(h.getChangeDate());
                history.setChangedBy(employeeMapper.toDomain(h.getChangedBy()));
                if (h.getPhotos() != null) {
                    history.setPhotos(h.getPhotos().stream().map(p -> {
                        Photo photo = new Photo();
                        photo.setIdPhoto(p.getIdPhoto());
                        photo.setPhotoPath(p.getPhotoPath());
                        photo.setUploadDate(p.getUploadDate());
                        photo.setPhotoType(p.getPhotoType());
                        return photo;
                    }).collect(Collectors.toList()));
                }
                return history;
            }).collect(Collectors.toList()));
        }

        serviceDelivery.setCreatedAt(entity.getCreatedAt());

        return serviceDelivery;
    }
}