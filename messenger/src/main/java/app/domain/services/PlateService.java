package app.domain.services;

import java.time.LocalDateTime;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.Plate;
import app.domain.model.enums.PlateType;
import app.domain.ports.PlatePort;

@Service
public class PlateService {

    @Autowired
    private PlatePort platePort;

    private static final Pattern CAR_PATTERN = Pattern.compile("^[A-Z]{3} ?\\d{3}$");
    private static final Pattern MOTO_PATTERN = Pattern.compile("^[A-Z]{3} ?\\d{2}[A-Z]$");
    private static final Pattern MOTORCAR_PATTERN = Pattern.compile("^\\d{3} ?[A-Z]{3}$");

    public Plate findOrRegister(String text) throws Exception {
        String normalized = text.trim().toUpperCase().replaceAll("\\s+", " ");

        Plate existing = platePort.findByPlateNumber(normalized);
        if (existing != null) {
            return existing;
        }

        Plate newPlate = new Plate();
        newPlate.setPlateNumber(normalized);
        newPlate.setPlateType(identifyType(normalized));
        newPlate.setUploadDate(LocalDateTime.now());
        return platePort.save(newPlate);
    }

    private PlateType identifyType(String plate) throws BusinessException {
        if (CAR_PATTERN.matcher(plate).matches())
            return PlateType.CAR;
        if (MOTO_PATTERN.matcher(plate).matches())
            return PlateType.MOTORCYCLE;
        if (MOTORCAR_PATTERN.matcher(plate).matches())
            return PlateType.MOTORCAR;

        throw new BusinessException("El formato de la placa '" + plate + "' no es válido.");
    }
}