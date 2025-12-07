package app.application.usecase;

import app.adapter.in.builder.PlateBuilder;
import app.adapter.in.builder.ServiceBuilder;
import app.adapter.in.validators.PlateValidator;
import app.domain.model.Dealership;
import app.domain.model.Employee;
import app.domain.model.Plate;
import app.domain.ports.DealershipPort;
import app.domain.ports.EmployeePort;
import app.domain.ports.OcrPort;
import app.domain.services.CreatePlate;
import app.domain.services.CreateService;
import app.application.exceptions.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Component
public class MessengerUseCase {

    @Autowired
    private CreatePlate createPlateService;
    @Autowired
    private CreateService createServiceDomain;
    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private DealershipPort dealershipPort;
    @Autowired
    private OcrPort ocrPort;
    @Autowired
    private PlateBuilder plateBuilder;
    @Autowired
    private ServiceBuilder serviceBuilder;
    @Autowired
    private PlateValidator plateValidator;

    public Plate processAndCreatePlateService(MultipartFile image, Long idDealership, String username)
            throws Exception {

        // 1. Validar entrada (Validator)
        plateValidator.validateOCRInput(image, idDealership);

        // 2. Extraer texto (Port Infra)
        String ocrRawText;
        try (InputStream is = image.getInputStream()) {
            ocrRawText = ocrPort.extractText(is);
        }

        // 3. Limpieza (Helper o inline)
        String cleanText = ocrRawText.toUpperCase();

        // 4. Construir Placa (Builder) - Internamente usa el regex del Validator
        Plate plate = plateBuilder.build(cleanText, idDealership);

        // 5. Guardar Placa (Domain Service)
        createPlateService.create(plate);

        // 6. Preparar datos para el Servicio
        Dealership dealership = dealershipPort.findById(idDealership);
        if (dealership == null) {
            throw new BusinessException("El concesionario no existe");
        }

        Employee messengerProbe = new Employee();
        messengerProbe.setUserName(username);
        Employee fullMessenger = employeePort.findByUserName(messengerProbe);

        // 7. Construir Servicio (ServiceBuilder) - AQUÍ ESTÁ EL CAMBIO CLAVE
        app.domain.model.Service service = serviceBuilder.build(plate, fullMessenger, dealership);

        // 8. Guardar Servicio (Domain Service)
        createServiceDomain.create(service);

        return plate;
    }
}