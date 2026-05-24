package app.application.usecase.delivery;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.exception.BusinessException;
import app.domain.model.Dealership;
import app.domain.model.Employee;
import app.domain.model.Plate;
import app.domain.model.ServiceDelivery;
import app.domain.model.StatusHistory;
import app.domain.model.enums.Status;
import app.domain.ports.DealershipPort;
import app.domain.ports.EmployeePort;
import app.domain.ports.PlatePort;
import app.domain.ports.ServiceDeliveryPort;
import org.springframework.context.ApplicationEventPublisher;
import app.domain.events.PlateStatusChangedEvent;
import app.domain.util.LogSanitizer;
import app.domain.services.PlateRecognition;

/**
 * Servicio para crear nuevos servicios de entrega con reconocimiento de placa.
 */
@Service
public class CreateServiceDeliveryUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CreateServiceDeliveryUseCase.class);

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
    @Autowired
    private app.domain.ports.TrackingPort trackingPort;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Crea un nuevo servicio de entrega, asocia el chasis (creándolo si no existe)
     * y asigna el servicio al transportista y concesionario indicados.
     */
    public ServiceDelivery create(String plateNumber, Long dealershipId, Long originDealershipId,
            Long messengerId, Double latitude, Double longitude)
            throws Exception {

        String maskedPlate = LogSanitizer.maskPlate(plateNumber);
        logger.info("Iniciando creación de servicio de entrega para chasis: {}", maskedPlate);

        Employee messenger = employeePort.findById(messengerId);
        if (messenger == null) {
            logger.warn("Fallo al crear servicio para chasis {}: mensajero no existe.", maskedPlate);
            throw new BusinessException("El mensajero no existe.");
        }

        Dealership dealership = dealershipPort.findById(dealershipId);
        if (dealership == null) {
            logger.warn("Fallo al crear servicio para chasis {}: concesionario destino no existe.", maskedPlate);
            throw new BusinessException("El concesionario indicado no existe.");
        }

        if (originDealershipId == null) {
            logger.warn("Fallo al crear servicio para chasis {}: Concesionario de origen es nulo", maskedPlate);
            throw new BusinessException("El concesionario de origen es obligatorio.");
        }
        Dealership originDealership = dealershipPort.findById(originDealershipId);
        if (originDealership == null) {
            logger.warn("Fallo al crear servicio para chasis {}: concesionario origen no existe.", maskedPlate);
            throw new BusinessException("El concesionario de origen indicado no existe.");
        }
        if (originDealershipId.equals(dealershipId)) {
            logger.warn("Fallo al crear servicio para chasis {}: concesionario origen igual al destino.", maskedPlate);
            throw new BusinessException("El concesionario de origen no puede ser el mismo que el destino.");
        }

        String normalizedPlate = plateNumber.trim().toUpperCase();

        var existingServices = serviceDeliveryPort.findAllPaginated(normalizedPlate, false, null,
                org.springframework.data.domain.PageRequest.of(0, 1));
        if (existingServices.getTotalElements() > 0) {
            logger.warn("Fallo al crear servicio: Chasis {} ya tiene un servicio registrado en el sistema", maskedPlate);
            throw new BusinessException(
                    "El chasis ya tiene un servicio registrado en el sistema.");
        }

        Plate plate = platePort.findByPlateNumber(normalizedPlate);
        if (plate == null) {
            plate = new Plate();
            plate.setPlateNumber(normalizedPlate);
            plate.setPlateType(plateRecognition.determinePlateType(normalizedPlate));
            plate.setUploadDate(LocalDateTime.now());
            platePort.save(plate);
            logger.debug("Chasis {} creada con tipo: {}", maskedPlate, plate.getPlateType());
        }

        ServiceDelivery service = new ServiceDelivery();
        service.setPlate(plate);
        service.setDealership(dealership);
        service.setOriginDealership(originDealership);
        service.setMessenger(messenger);
        service.setCurrentStatus(Status.ASSIGNED);
        service.setObservation(null);

        StatusHistory history = new StatusHistory();
        history.setPreviousStatus(null);
        history.setNewStatus(Status.ASSIGNED);
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(messenger);
        history.setDeliveryLatitude(latitude);
        history.setDeliveryLongitude(longitude);

        service.addHistory(history);

        ServiceDelivery saved = serviceDeliveryPort.save(service);

        if (latitude != null && longitude != null) {
            app.domain.model.TrackingHistory tracking = new app.domain.model.TrackingHistory();
            tracking.setMessengerId(messenger.getIdEmployee());
            tracking.setServiceDeliveryId(saved.getIdServiceDelivery());

            app.domain.model.Location location = new app.domain.model.Location(
                    latitude,
                    longitude,
                    LocalDateTime.now(),
                    0.0);
            tracking.setLocation(location);

            tracking.setRecordedAt(LocalDateTime.now());
            tracking.setSource(app.domain.model.enums.TrackingSource.MANUAL);
            trackingPort.saveTrackingHistory(tracking);
            logger.debug("Ubicación de seguimiento registrada para chasis {}", maskedPlate);
        }

        eventPublisher.publishEvent(new PlateStatusChangedEvent(saved, null, Status.ASSIGNED));

        logger.info("Servicio de entrega creado exitosamente para chasis {}.", maskedPlate);

        return saved;
    }
}