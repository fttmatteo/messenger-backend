package app.application.usecase;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.model.Photo;
import app.domain.model.ServiceDelivery;
import app.domain.model.Signature;
import app.domain.model.enums.Status;
import app.domain.ports.OcrPort;
import app.domain.ports.StoragePort;
import app.domain.services.CreateServiceDelivery;
import app.domain.services.DeleteServiceDelivery;
import app.domain.services.SearchServiceDelivery;
import app.domain.services.UpdateServiceDelivery;

/**
 * Caso de uso de aplicación para gestionar servicios de entrega.
 * 
 * Orquesta operaciones complejas de servicios de entrega incluyendo:
 * Creación con detección OCR automática de placas
 * Creación con entrada manual de placas
 * Actualización de estados con gestión de archivos (firmas y fotos)
 * Consultas por múltiples criterios
 * Manejo transaccional con rollback automático en caso de error
 * 
 * Gestiona el almacenamiento de archivos y limpieza automática si falla la
 * operación.
 */
@Service
public class ServiceDeliveryUseCase {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Autowired
    private CreateServiceDelivery createService;
    @Autowired
    private UpdateServiceDelivery updateService;
    @Autowired
    private SearchServiceDelivery searchService;
    @Autowired
    private DeleteServiceDelivery deleteService;
    @Autowired
    private StoragePort storagePort;
    @Autowired
    private OcrPort ocrPort;

    /**
     * Crea un servicio de entrega a partir de una imagen de placa.
     * 
     * Este método realiza las siguientes acciones:
     * - Extrae el texto (placa) de la imagen usando OCR.
     * - Guarda la imagen de la detección en el almacenamiento.
     * - Crea el registro del servicio en la base de datos.
     * - Si ocurre un error, intenta eliminar la imagen guardada (compensación).
     * 
     * @param imageFile         El archivo de imagen que contiene la placa.
     * @param dealershipId      El ID del concesionario asociado.
     * @param messengerDocument El documento del mensajero asignado.
     * @throws Exception Si falla el OCR, el almacenamiento o la creación del
     *                   servicio.
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void createServiceFromImage(File imageFile, Long dealershipId, Long messengerDocument) throws Exception {
        String extractedText = ocrPort.extractText(imageFile);
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String fileName = extractedText + "_ASSIGNED_" + timestamp;

        String savedPath = storagePort.save(imageFile, "detections", fileName);

        try {
            createService.create(extractedText, savedPath, dealershipId, messengerDocument);
        } catch (Exception e) {
            try {
                java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(savedPath));
            } catch (Exception deleteError) {
                System.err.println("No se pudo eliminar la imagen: " + deleteError.getMessage());
            }
            throw e;
        }
    }

    /**
     * Crea un servicio de entrega con una placa ingresada manualmente.
     * 
     * Similar a la creación por imagen, pero usa un número de placa proporcionado
     * explícitamente. Aún así guarda la imagen como evidencia de la asignación.
     * 
     * @param imageFile         Imagen de evidencia (opcional o requerida según
     *                          reglas).
     * @param manualPlateNumber El número de placa ingresado manualmente.
     * @param dealershipId      El ID del concesionario.
     * @param messengerDocument El documento del mensajero.
     * @throws Exception Si falla el almacenamiento o la creación.
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void createServiceWithManualPlate(File imageFile, String manualPlateNumber, Long dealershipId,
            Long messengerDocument) throws Exception {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String fileName = manualPlateNumber + "_ASSIGNED_" + timestamp;

        String savedPath = storagePort.save(imageFile, "detections", fileName);

        try {
            createService.create(manualPlateNumber, savedPath, dealershipId, messengerDocument);
        } catch (Exception e) {
            try {
                java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(savedPath));
            } catch (Exception deleteError) {
                System.err.println("No se pudo eliminar la imagen: " + deleteError.getMessage());
            }
            throw e;
        }
    }

    /**
     * Actualiza el estado de un servicio (versión simple con objetos de dominio).
     * 
     * @param serviceId    ID del servicio.
     * @param newStatus    Nuevo estado.
     * @param observation  Observaciones (opcional).
     * @param signature    Objeto Signature (si existe).
     * @param photos       Lista de objetos Photo (si existen).
     * @param userDocument Documento del usuario que realiza la acción.
     * @throws Exception Si falla la actualización.
     */
    public void updateStatus(Long serviceId, Status newStatus, String observation,
            Signature signature, List<Photo> photos, Long userDocument) throws Exception {
        updateService.updateStatus(serviceId, newStatus, observation, signature, photos, userDocument);
    }

    /**
     * Actualiza el estado de un servicio procesando archivos adjuntos (firmas y
     * fotos).
     * 
     * Este método gestiona la carga de archivos al almacenamiento antes de
     * actualizar el estado.
     * Si la actualización del estado falla, intenta eliminar los archivos subidos
     * para mantener consistencia.
     * 
     * @param serviceId     ID del servicio.
     * @param newStatus     Nuevo estado.
     * @param observation   Observaciones.
     * @param signatureFile Archivo de imagen de la firma (opcional).
     * @param photoFiles    Lista de archivos de fotos de evidencia (opcional).
     * @param userDocument  Documento del usuario.
     * @throws Exception Si falla la carga de archivos o la actualización del
     *                   estado.
     */
    public void updateStatusWithFiles(Long serviceId, Status newStatus, String observation,
            File signatureFile, List<File> photoFiles, Long userDocument) throws Exception {

        ServiceDelivery service = searchService.findById(serviceId);
        if (service == null) {
            throw new Exception("Servicio no encontrado con ID: " + serviceId);
        }
        String plateNumber = service.getPlate().getPlateNumber();

        String timestamp = LocalDateTime.now().format(DATE_FORMAT);

        List<String> savedPaths = new java.util.ArrayList<>();

        Signature signature = null;
        if (signatureFile != null) {
            String signatureFileName = "signature_" + plateNumber + "_" + newStatus.name() + "_" + timestamp;
            String path = storagePort.save(signatureFile, "signatures", signatureFileName);
            savedPaths.add(path);
            signature = new Signature();
            signature.setSignaturePath(path);
        }

        List<Photo> photos = new java.util.ArrayList<>();
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
            updateService.updateStatus(serviceId, newStatus, observation, signature, photos, userDocument);
        } catch (Exception e) {
            for (String path : savedPaths) {
                try {
                    java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(path));
                } catch (Exception deleteError) {
                    System.err.println("No se pudo eliminar archivo: " + deleteError.getMessage());
                }
            }
            throw e;
        }
    }

    /**
     * Busca un servicio por su ID.
     * 
     * @param id ID del servicio.
     * @return El servicio encontrado.
     * @throws Exception Si no se encuentra.
     */
    public ServiceDelivery findById(Long id) throws Exception {
        return searchService.findById(id);
    }

    /**
     * Obtiene todos los servicios.
     * 
     * @return Lista completa de servicios.
     */
    public List<ServiceDelivery> findAll() {
        return searchService.findAll();
    }

    /**
     * Busca servicios asignados a un mensajero.
     * 
     * @param messengerId ID o documento del mensajero.
     * @return Lista de servicios del mensajero.
     */
    public List<ServiceDelivery> findByMessenger(Long messengerId) {
        return searchService.findByMessenger(messengerId);
    }

    /**
     * Busca servicios asociados a una placa vehicular.
     * 
     * @param plateNumber Número de placa.
     * @return Lista de servicios asociados.
     */
    public List<ServiceDelivery> findByPlate(String plateNumber) {
        return searchService.findByPlate(plateNumber);
    }

    /**
     * Busca servicios de un concesionario específico.
     * 
     * @param dealershipId ID del concesionario.
     * @return Lista de servicios del concesionario.
     */
    public List<ServiceDelivery> findByDealership(Long dealershipId) {
        return searchService.findByDealership(dealershipId);
    }

    /**
     * Busca servicios por su estado actual.
     * 
     * @param status Estado a consultar (ej. PENDING, DELIVERED).
     * @return Lista de servicios en ese estado.
     */
    public List<ServiceDelivery> findByStatus(Status status) {
        return searchService.findByStatus(status);
    }

    /**
     * Elimina un servicio por su ID.
     * 
     * @param id ID del servicio a eliminar.
     * @throws Exception Si no se puede eliminar (ej. tiene restricciones de
     *                   negocio).
     */
    public void deleteById(Long id) throws Exception {
        deleteService.deleteById(id);
    }
}