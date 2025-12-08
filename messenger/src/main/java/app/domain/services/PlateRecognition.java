package app.domain.services;

import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.enums.PlateType;

import java.util.regex.Pattern;

@Service
public class PlateRecognition {

    // Regex para Carro: 3 letras, espacio opcional, 3 números
    private static final Pattern CAR_PATTERN = Pattern.compile("^[A-Z]{3}\\s*\\d{3}$");
    // Regex para Moto: 3 letras, espacio opcional, 2 números, 1 letra
    private static final Pattern MOTO_PATTERN = Pattern.compile("^[A-Z]{3}\\s*\\d{2}[A-Z]$");
    // Regex para Motocarro: 3 números, espacio opcional, 3 letras
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
            throw new BusinessException("Formato de placa no reconocido: " + normalizedPlate);
        }
    }
}