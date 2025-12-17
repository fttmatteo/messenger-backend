package app.adapter.in.rest.mapper;

import app.adapter.in.rest.response.*;
import app.adapter.out.storage.GoogleCloudStorageAdapter;
import app.domain.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper para convertir entidades ServiceDelivery a DTOs de respuesta.
 * 
 * Transforma objetos de dominio ServiceDelivery a ServiceDeliveryResponse,
 * incluyendo mapeo anidado de referencias (placa, concesionario, mensajero,
 * historial).
 * 
 * Soporta tanto Google Cloud Storage (con URLs firmadas) como almacenamiento
 * local (retornando paths directos).
 */
@Component
public class ServiceDeliveryResponseMapper {

    @Autowired
    private EmployeeResponseMapper employeeMapper;
    @Autowired
    private DealershipResponseMapper dealershipMapper;

    // Opcional: solo disponible cuando app.storage.type=gcs
    @Autowired(required = false)
    private GoogleCloudStorageAdapter storageAdapter;

    /**
     * Genera una URL para acceder a un archivo.
     * 
     * Si GCS está disponible, genera una URL firmada temporal.
     * Si no, retorna el path tal cual (para almacenamiento local).
     */
    private String getFileUrl(String path) {
        if (storageAdapter != null && path != null) {
            try {
                return storageAdapter.regenerateSignedUrl(path);
            } catch (Exception e) {
                // Si falla la generación de URL firmada, usar path directo
                return path;
            }
        }
        return path;
    }

    /**
     * Convierte una entidad ServiceDelivery a ServiceDeliveryResponse.
     *
     * Mapea todos los campos relevantes, incluyendo estado actual, historial,
     * evidencias (fotos y firma), y entidades relacionadas (placa, mensajero,
     * concesionario).
     *
     * @param service Entidad ServiceDelivery de origen.
     * @return DTO ServiceDeliveryResponse completamente poblado o null si la
     *         entrada es nula.
     */
    public ServiceDeliveryResponse toResponse(ServiceDelivery service) {
        if (service == null) {
            return null;
        }

        ServiceDeliveryResponse response = new ServiceDeliveryResponse();
        response.setIdServiceDelivery(service.getIdServiceDelivery());
        response.setCurrentStatus(service.getCurrentStatus());
        response.setObservation(service.getObservation());
        response.setCreatedAt(service.getCreatedAt());

        if (service.getPlate() != null) {
            Plate plate = service.getPlate();
            response.setPlate(new ServiceDeliveryResponse.PlateResponse(
                    plate.getIdPlate(),
                    plate.getPlateNumber(),
                    plate.getPlateType()));
        }

        response.setDealership(dealershipMapper.toResponse(service.getDealership()));

        response.setMessenger(employeeMapper.toResponse(service.getMessenger()));

        if (service.getSignature() != null) {
            Signature sig = service.getSignature();
            String signedUrl = getFileUrl(sig.getSignaturePath());
            response.setSignature(new SignatureResponse(
                    sig.getIdSignature(),
                    signedUrl,
                    sig.getUploadDate()));
        }

        if (service.getPhotos() != null) {
            response.setPhotos(service.getPhotos().stream()
                    .map(p -> new PhotoResponse(
                            p.getIdPhoto(),
                            getFileUrl(p.getPhotoPath()),
                            p.getUploadDate(),
                            p.getPhotoType()))
                    .collect(Collectors.toList()));
        }

        if (service.getHistory() != null) {
            response.setHistory(service.getHistory().stream()
                    .map(h -> {
                        StatusHistoryResponse historyResponse = new StatusHistoryResponse(
                                h.getIdStatusHistory(),
                                h.getPreviousStatus(),
                                h.getNewStatus(),
                                h.getChangeDate(),
                                employeeMapper.toResponse(h.getChangedBy()));

                        if (h.getPhotos() != null) {
                            historyResponse.setPhotos(h.getPhotos().stream()
                                    .map(p -> new PhotoResponse(
                                            p.getIdPhoto(),
                                            getFileUrl(p.getPhotoPath()),
                                            p.getUploadDate(),
                                            p.getPhotoType()))
                                    .collect(Collectors.toList()));
                        }
                        return historyResponse;
                    })
                    .collect(Collectors.toList()));
        }

        return response;
    }
}
