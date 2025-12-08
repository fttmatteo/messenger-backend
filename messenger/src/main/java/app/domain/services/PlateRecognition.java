package app.domain.services;

import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.enums.PlateType;

import java.util.regex.Pattern;

@Service
public class PlateRecognition {

    // Carro: 3 letras + (espacio opcional) + 3 números
    private static final Pattern CAR_PATTERN = Pattern.compile("^[A-Z]{3}\\s*\\d{3}$");
    // Moto: 3 letras + (espacio opcional) + 2 números + 1 letra
    private static final Pattern MOTO_PATTERN = Pattern.compile("^[A-Z]{3}\\s*\\d{2}[A-Z]$");
    // Motocarro: 3 números + (espacio opcional) + 3 letras
    private static final Pattern MOTOCARRO_PATTERN = Pattern.compile("^\\d{3}\\s*[A-Z]{3}$");

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
            throw new BusinessException("Formato de placa no reconocido: " + normalizedPlate +
                    ". Formatos válidos: ABC 123 (Carro), ABC 12A (Moto), 123 ABC (Motocarro).");
        }
    }

    public String formatPlateForStorage(String plateNumber, PlateType type) {
        String clean = plateNumber.replaceAll("\\s+", "").toUpperCase();
        switch (type) {
            case CAR: // ABC123 -> ABC 123
                return clean.substring(0, 3) + " " + clean.substring(3);
            case MOTORCYCLE: // ABC12A -> ABC 12A
                return clean.substring(0, 3) + " " + clean.substring(3);
            case MOTORCAR: // 123ABC -> 123 ABC
                return clean.substring(0, 3) + " " + clean.substring(3);
            default:
                return clean;
        }
    }
}