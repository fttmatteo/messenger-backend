package app.domain.services;

import org.springframework.stereotype.Service; // Spring es tolerable en servicios de dominio en este estilo
import app.application.exceptions.BusinessException;
import app.domain.model.enums.PlateType;

import java.util.regex.Pattern;

@Service
public class PlateRecognition {

    // MEJORA: Usamos \\s* para indicar "cero o más espacios".
    // Esto permite reconocer "ABC 123" (correcto) y "ABC123" (OCR pegado)

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

        // Normalización: quitamos ruido y espacios para validar el formato "crudo" si
        // queremos,
        // o confiamos en el Regex flexible.
        // Para asegurar consistencia, convertimos a mayúsculas.
        String normalizedPlate = plateNumber.trim().toUpperCase();

        if (CAR_PATTERN.matcher(normalizedPlate).matches()) {
            return PlateType.CAR;
        } else if (MOTO_PATTERN.matcher(normalizedPlate).matches()) {
            return PlateType.MOTORCYCLE;
        } else if (MOTOCARRO_PATTERN.matcher(normalizedPlate).matches()) {
            return PlateType.MOTORCAR;
        } else {
            // Mensaje de error detallado
            throw new BusinessException("Formato de placa no reconocido: " + normalizedPlate +
                    ". Formatos válidos: ABC 123 (Carro), ABC 12A (Moto), 123 ABC (Motocarro).");
        }
    }

    /**
     * Opcional: Método para formatear la placa visualmente antes de guardarla en
     * BD.
     * Convierte "ABC123" en "ABC 123".
     */
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