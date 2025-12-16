package app.domain.services;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.Dealership;
import app.domain.model.Employee;
import app.domain.model.Photo;
import app.domain.model.Plate;
import app.domain.model.ServiceDelivery;
import app.domain.model.StatusHistory;
import app.domain.model.enums.Status;
import app.domain.ports.DealershipPort;
import app.domain.ports.EmployeePort;
import app.domain.ports.PlatePort;
import app.domain.ports.ServiceDeliveryPort;

/**
 * Servicio de dominio para crear nuevos servicios de entrega.
 * 
 * Orquesta el proceso completo de creación de un servicio de entrega:
 * 
 * Validación de existencia del mensajero asignado
 * Validación de existencia del concesionario destino
 * Normalización y registro de la placa vehicular
 * Determinación automática del tipo de placa (carro, moto, motocarro)
 * Asociación de foto de detección si está disponible
 * Inicialización del servicio en estado ASSIGNED
 * Creación del primer registro en el historial de estados
 * 
 * Si la placa no existe previamente, se crea automáticamente en el sistema.
 */
@Service
public class CreateServiceDelivery {

    private static final Logger logger = LoggerFactory.getLogger(CreateServiceDelivery.class);

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;
    @Autowired
    private PlatePort platePort;
    @Autowired
    private DealershipPort dealershipPort;
    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private PlateRecognition plateRecognition;

    /**
     * Crea un nuevo servicio de entrega.
     * 
     * Valida mensajero y concesionario, determina tipo de placa, crea la placa si
     * no existe,
     * e inicializa el servicio en estado ASSIGNED con su primer registro de
     * historial.
     * 
     * @param plateNumber       Número de placa vehicular.
     * @param photoPath         Ruta de la foto de detección de placa (opcional).
     * @param dealershipId      ID del concesionario destino.
     * @param messengerDocument Documento del mensajero asignado.
     * @throws Exception Si el mensajero o concesionario no existen, o si el formato
     *                   de placa es inválido.
     */
    public void create(String plateNumber, String photoPath, Long dealershipId, Long messengerDocument)
            throws Exception {
        logger.info("Creando servicio de entrega: placa={}, concesionario={}, mensajero={}",
                plateNumber, dealershipId, messengerDocument);

        Employee messenger = employeePort.findByDocument(messengerDocument);
        if (messenger == null) {
            throw new BusinessException("El mensajero no existe.");
        }

        Dealership dealership = dealershipPort.findById(dealershipId);
        if (dealership == null) {
            throw new BusinessException("El concesionario indicado no existe.");
        }

        String normalizedPlate = plateNumber.trim().toUpperCase();

        Plate plate = platePort.findByPlateNumber(normalizedPlate);
        if (plate == null) {
            plate = new Plate();
            plate.setPlateNumber(normalizedPlate);
            plate.setPlateType(plateRecognition.determinePlateType(normalizedPlate));
            plate.setUploadDate(LocalDateTime.now());
            platePort.save(plate);
            logger.debug("Nueva placa registrada: {} ({})", normalizedPlate, plate.getPlateType());
        }

        ServiceDelivery service = new ServiceDelivery();
        service.setPlate(plate);
        service.setDealership(dealership);
        service.setMessenger(messenger);
        service.setCurrentStatus(Status.ASSIGNED);
        service.setObservation(null);

        if (photoPath != null) {
            Photo detectionPhoto = new Photo();
            detectionPhoto.setPhotoPath(photoPath);
            detectionPhoto.setPhotoType(app.domain.model.enums.PhotoType.PLATE_DETECTION);
            detectionPhoto.setUploadDate(LocalDateTime.now());
            service.addPhoto(detectionPhoto);
        }

        StatusHistory history = new StatusHistory();
        history.setPreviousStatus(null);
        history.setNewStatus(Status.ASSIGNED);
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(messenger);

        service.addHistory(history);

        serviceDeliveryPort.save(service);
        logger.info("Servicio creado exitosamente: placa={}, concesionario={}, mensajero={}",
                normalizedPlate, dealership.getName(), messenger.getFullName());
    }
}