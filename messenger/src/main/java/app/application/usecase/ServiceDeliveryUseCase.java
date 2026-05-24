package app.application.usecase;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.domain.model.Photo;
import app.domain.model.ServiceDelivery;
import app.domain.model.Signature;
import app.domain.model.enums.Status;

import app.domain.ports.StoragePort;
import app.application.usecase.delivery.CreateServiceDelivery;
import app.application.usecase.delivery.DeleteServiceDelivery;
import app.application.usecase.delivery.SearchServiceDelivery;
import app.application.usecase.delivery.UpdateServiceDelivery;


/**
 * Caso de uso principal para gestión de servicios de entrega.
 * 
 * Reglas de negocio implementadas:
 * - Creación automática con estado ASSIGNED al mensajero autenticado
 * - Mensajero: solo puede usar PENDING, DELIVERED, RETURNED
 * - Admin: solo puede usar CANCELED, RESOLVED y reasignar mensajero
 * - Eliminación → Papelera (soft delete) con borrado definitivo a los 60 días
 */
@Service
public class ServiceDeliveryUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDeliveryUseCase.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final CreateServiceDelivery createService;
    private final UpdateServiceDelivery updateService;
    private final SearchServiceDelivery searchService;
    private final DeleteServiceDelivery deleteService;
    private final StoragePort storagePort;

    public ServiceDeliveryUseCase(
            CreateServiceDelivery createService,
            UpdateServiceDelivery updateService,
            SearchServiceDelivery searchService,
            DeleteServiceDelivery deleteService,
            StoragePort storagePort) {
        this.createService = createService;
        this.updateService = updateService;
        this.searchService = searchService;
        this.deleteService = deleteService;
        this.storagePort = storagePort;
    }



    /**
     * Crea un servicio utilizando un número de chasis ingresado manualmente.
     */
    @Caching(evict = {
        @CacheEvict(value = "services", allEntries = true),
        @CacheEvict(value = "service-details", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)

    public ServiceDelivery createServiceWithManualPlate(String manualPlateNumber, Long dealershipId,
            Long originDealershipId, Long messengerId, Double latitude, Double longitude) throws Exception {
        
        try {
            ServiceDelivery service = createService.create(manualPlateNumber, dealershipId, originDealershipId,
                    messengerId, latitude, longitude);

            return service;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Actualiza el estado de un servicio existente (Sobrecarga compatible).
     */
    public ServiceDelivery updateStatus(Long serviceId, Status newStatus, String observation,
            Signature signature, List<Photo> photos, Long userId) throws Exception {
        return updateStatus(serviceId, newStatus, observation, signature, photos, userId, null, null);
    }

    /**
     * Actualiza el estado de un servicio existente.
     */
    @Caching(evict = {
        @CacheEvict(value = "services", allEntries = true),
        @CacheEvict(value = "service-details", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)

    public ServiceDelivery updateStatus(Long serviceId, Status newStatus, String observation,
            Signature signature, List<Photo> photos, Long userId, Double latitude, Double longitude) throws Exception {
        return updateService.updateStatus(serviceId, newStatus, observation, signature, photos, userId, latitude,
                longitude);
    }

    /**
     * Actualiza el estado de un servicio incluyendo la carga de archivos (fotos,
     * firmas).
     */
    @Caching(evict = {
        @CacheEvict(value = "services", allEntries = true),
        @CacheEvict(value = "service-details", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public ServiceDelivery updateStatusWithFiles(Long serviceId, Status newStatus, String observation,
            File signatureFile, List<File> photoFiles, Long userId, Double latitude,
            Double longitude)
            throws Exception {

        ServiceDelivery service = searchService.findById(serviceId);
        if (service == null) {
            throw new RuntimeException("Servicio no encontrado.");
        }
        String plateNumber = service.getPlate().getPlateNumber();
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);

        List<String> savedPaths = new ArrayList<>();

        Signature signature = null;
        if (signatureFile != null) {
            String signatureFileName = "signature_" + plateNumber + "_" + newStatus.name() + "_" + timestamp;
            String path = storagePort.save(signatureFile, "signatures", signatureFileName);
            savedPaths.add(path);
            signature = new Signature();
            signature.setSignaturePath(path);
        }


        List<Photo> photos = new ArrayList<>();
        if (photoFiles != null && !photoFiles.isEmpty()) {
            int count = 1;
            for (File f : photoFiles) {
                String evidenceFileName = "evidence_" + plateNumber + "_" + newStatus.name() + "_" + timestamp + "_"
                        + count;
                String path = storagePort.save(f, "evidence", evidenceFileName);
                savedPaths.add(path);
                Photo p = new Photo();
                p.setPhotoPath(path);
                p.setPhotoType(app.domain.model.enums.PhotoType.EVIDENCE);
                photos.add(p);
                count++;
            }
        }

        try {
            ServiceDelivery updated = updateService.updateStatus(serviceId, newStatus, observation, signature, photos,
                    userId, latitude, longitude);
            return updated;
        } catch (Exception e) {
            cleanupFiles(savedPaths);
            throw e;
        }
    }

    /**
     * Reasigna un servicio cancelado a un nuevo mensajero (solo admin).
     */
    @Caching(evict = {
        @CacheEvict(value = "services", allEntries = true),
        @CacheEvict(value = "service-details", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)

    public ServiceDelivery reassignMessenger(Long serviceId, Long newMessengerId, Long adminUserId) throws Exception {
        return updateService.reassignMessenger(serviceId, newMessengerId, adminUserId);
    }

    /**
     * Busca un servicio por su ID (versión de solo lectura).
     */
    @Cacheable(value = "service-details", key = "'id:' + #id")
    @Transactional(readOnly = true)
    public ServiceDelivery findById(Long id) throws Exception {
        return searchService.findById(id);
    }

    /**
     * Busca un servicio por su UUID público.
     */
    @Cacheable(value = "service-details", key = "'uuid:' + #uuid")
    @Transactional(readOnly = true)
    public ServiceDelivery findByUuid(String uuid) throws Exception {
        return searchService.findByUuid(uuid);
    }

    /**
     * Busca un servicio por su UUID público incluyendo eliminados.
     */
    @Transactional(readOnly = true)
    public ServiceDelivery findByUuidIncludingDeleted(String uuid) throws Exception {
        return searchService.findByUuidIncludingDeleted(uuid);
    }

    /**
     * Recupera todos los servicios con paginación, ordenamiento y filtro de estado.
     */
    @Transactional(readOnly = true)
    public Page<ServiceDelivery> findAllPaginated(int page, int size, String sortBy, String sortDirection,
            String search, List<Status> statuses) {
        String mappedSortBy = mapSortField(sortBy);
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, mappedSortBy));
        return searchService.findAllPaginated(search, false, statuses, pageable);
    }

    /**
     * Recupera servicios de un mensajero específico con paginación, ordenamiento y
     * filtro de estado.
     */
    @Transactional(readOnly = true)
    public Page<ServiceDelivery> findByMessengerPaginated(Long messengerId, int page, int size, String sortBy,
            String sortDirection, String search, List<Status> statuses) {
        String mappedSortBy = mapSortField(sortBy);
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, mappedSortBy));
        return searchService.findByMessengerPaginated(messengerId, search, false, statuses, pageable);
    }

    /**
     * Mapea los campos de ordenamiento del frontend a las rutas de propiedades de
     * la
     * entidad JPA para evitar errores internos del servidor (500).
     */
    private String mapSortField(String sortBy) {
        if (sortBy == null)
            return "createdAt";

        return switch (sortBy) {
            case "plateNumber" -> "plate.plateNumber";
            case "dealershipName" -> "dealership.name";
            case "messengerName" -> "messenger.fullName";
            case "currentStatus" -> "currentStatus";
            default -> sortBy;
        };
    }

    /**
     * Mueve un servicio a la papelera (método simple sin auditoría de usuario
     * explícito).
     */
    public void deleteById(Long id) throws Exception {
        deleteService.deleteById(id);
    }

    /**
     * Mueve un servicio a la papelera (Soft Delete).
     */
    @Caching(evict = {
        @CacheEvict(value = "services", allEntries = true),
        @CacheEvict(value = "service-details", allEntries = true)
    })

    public void deleteById(Long id, Long userId) throws Exception {
        deleteService.deleteById(id, userId);
    }

    /**
     * Recupera todos los servicios que han sido movidos a la papelera con paginación.
     */
    @Transactional(readOnly = true)
    public Page<ServiceDelivery> findDeleted(Pageable pageable) {
        return searchService.findDeleted(pageable);
    }

    /**
     * Restaura un servicio previamente eliminado de la papelera.
     */
    @Caching(evict = {
        @CacheEvict(value = "services", allEntries = true),
        @CacheEvict(value = "service-details", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)

    public ServiceDelivery restore(Long id, Long userId) throws Exception {
        return deleteService.restore(id, userId);
    }

    /**
     * Vacía la papelera eliminando permanentemente todos los servicios marcados
     * como eliminados.
     */
    @Caching(evict = {
        @CacheEvict(value = "services", allEntries = true),
        @CacheEvict(value = "service-details", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)

    public int emptyTrash(Long userId) {
        return deleteService.emptyTrash();
    }

    /**
     * Elimina permanentemente un servicio específico de la papelera.
     */
    @Caching(evict = {
        @CacheEvict(value = "services", allEntries = true),
        @CacheEvict(value = "service-details", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)

    public void permanentDeleteById(Long id, Long userId) throws Exception {
        deleteService.archiveService(id);
    }

    private void cleanupFiles(String... paths) {
        for (String path : paths) {
            try {
                Files.deleteIfExists(Paths.get(path));
            } catch (Exception e) {
                logger.warn("Error al intentar limpiar un archivo temporal.");
            }
        }
    }

    private void cleanupFiles(List<String> paths) {
        cleanupFiles(paths.toArray(new String[0]));
    }
}