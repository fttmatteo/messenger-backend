package app.domain.services;

import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.enums.PlateType;

import java.util.regex.Pattern;

@Service
public class PlateRecognitionService {

    // Regex para Carro: ABC 123 (3 letras, espacio, 3 números)
    private static final Pattern CAR_PATTERN = Pattern.compile("^[A-Z]{3} \\d{3}$");
    // Regex para Moto: ABC 12A (3 letras, espacio, 2 números, 1 letra)
    private static final Pattern MOTO_PATTERN = Pattern.compile("^[A-Z]{3} \\d{2}[A-Z]$");
    // Regex para Motocarro: 123 ABC (3 números, espacio, 3 letras)
    private static final Pattern MOTOCARRO_PATTERN = Pattern.compile("^\\d{3} [A-Z]{3}$");

    public PlateType determinePlateType(String plateNumber) throws BusinessException {
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            throw new BusinessException("El número de placa no puede estar vacío.");
        }

        String normalizedPlate = plateNumber.trim().toUpperCase();

        if (CAR_PATTERN.matcher(normalizedPlate).matches()) {
            return PlateType.CAR;
        } else if (MOTO_PATTERN.matcher(normalizedPlate).matches()) {
            return PlateType.MOTORCYCLE;
        } else if (MOTOCARRO_PATTERN.matcher(normalizedPlate).matches()) {
            return PlateType.MOTORCAR;
        } else {
            throw new BusinessException("Formato de placa no reconocido: " + normalizedPlate);
        }
    }
}