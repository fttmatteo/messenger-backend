package app.domain.services;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.application.exceptions.BusinessException;
import app.domain.model.Dealership;
import app.domain.model.Employee;
import app.domain.model.Plate;
import app.domain.model.ServiceDelivery;
import app.domain.model.StatusHistory;
import app.domain.model.enums.PlateType;
import app.domain.model.enums.Status;
import app.domain.ports.DealershipPort;
import app.domain.ports.EmployeePort;
import app.domain.ports.ServiceDeliveryPort;

@Service
public class CreateServiceDelivery {

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;
    @Autowired
    private DealershipPort dealershipPort;
    @Autowired
    private EmployeePort employeePort;

    // Moto: ABC 12A (3 letras, espacio, 2 números, 1 letra)
    private static final Pattern MOTO_PATTERN = Pattern.compile("^[A-Za-z]{3}\\s\\d{2}[A-Za-z]$");
    // Carro: ABC 123 (3 letras, espacio, 3 números)
    private static final Pattern CAR_PATTERN = Pattern.compile("^[A-Za-z]{3}\\s\\d{3}$");
    // Motocarro: 123 ABC (3 números, espacio, 3 letras)
    private static final Pattern MOTORCAR_PATTERN = Pattern.compile("^\\d{3}\\s[A-Za-z]{3}$");

    public void create(String plateNumber, Long dealershipId, Long messengerDocument) throws Exception {
        // 1. Validar Mensajero
        Employee messenger = employeePort.findByDocument(messengerDocument);
        if (messenger == null) {
            throw new BusinessException("El mensajero no existe");
        }
        // 2. Validar Concesionario
        Dealership dealership = dealershipPort.findById(dealershipId);
        if (dealership == null) {
            throw new BusinessException("El concesionario no existe");
        }
        // 3. Crear y clasificar la Placa
        Plate plate = new Plate();
        plate.setPlateNumber(plateNumber.toUpperCase());
        plate.setUploadDate(LocalDateTime.now());
        plate.setPlateType(identifyPlateType(plateNumber.toUpperCase()));
        // 4. Crear el Servicio
        ServiceDelivery service = new ServiceDelivery();
        service.setPlate(plate);
        service.setDealership(dealership);
        service.setMessenger(messenger);
        service.setCurrentStatus(Status.ASSIGNED); // Estado inicial automático
        // 5. Registrar historial inicial
        StatusHistory history = new StatusHistory();
        history.setNewStatus(Status.ASSIGNED);
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(messenger); // El mensajero que la ingresa
        // Nota: previousStatus es null porque es nuevo
        service.addHistory(history);
        // 6. Guardar todo el agregado
        serviceDeliveryPort.save(service);
    }

}