package app.domain.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.enums.PlateType;

import java.util.regex.Pattern;

/**
 * Servicio de dominio para reconocimiento y validación de placas vehiculares.
 * 
 * Determina automáticamente el tipo de vehículo basándose en el formato de la
 * placa:
 * 
 * Carro (CAR): Formato ABC 123
 * Motocicleta (MOTORCYCLE): Formato ABC 12A
 * Motocarro (MOTORCAR): Formato 123 ABC
 * 
 * Utiliza expresiones regulares (regex) para validar y clasificar placas,
 * normalizando el formato para almacenamiento consistente.
 */
@Service
public class PlateRecognition {

    private static final Logger logger = LoggerFactory.getLogger(PlateRecognition.class);

    private static final Pattern CAR_PATTERN = Pattern.compile("^[A-Z]{3}\\s*\\d{3}$");
    private static final Pattern MOTO_PATTERN = Pattern.compile("^[A-Z]{3}\\s*\\d{2}[A-Z]$");
    private static final Pattern MOTOCARRO_PATTERN = Pattern.compile("^\\d{3}\\s*[A-Z]{3}$");

    /**
     * Determina el tipo de vehículo basándose en el formato de la placa.
     * 
     * @param plateNumber Número de placa a analizar.
     * @return Tipo de placa (CAR, MOTORCYCLE, MOTORCAR).
     * @throws BusinessException Si el formato no coincide con ningún patrón
     *                           conocido.
     */
    public PlateType determinePlateType(String plateNumber) throws BusinessException {
        logger.debug("Determinando tipo de placa: {}", plateNumber);
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            throw new BusinessException("El número de placa no puede estar vacío.");
        }

        String normalizedPlate = plateNumber.trim().toUpperCase();

        if (CAR_PATTERN.matcher(normalizedPlate).matches()) {
            logger.debug("Placa identificada como CARRO: {}", normalizedPlate);
            return PlateType.CAR;
        } else if (MOTO_PATTERN.matcher(normalizedPlate).matches()) {
            logger.debug("Placa identificada como MOTO: {}", normalizedPlate);
            return PlateType.MOTORCYCLE;
        } else if (MOTOCARRO_PATTERN.matcher(normalizedPlate).matches()) {
            logger.debug("Placa identificada como MOTOCARRO: {}", normalizedPlate);
            return PlateType.MOTORCAR;
        } else {
            logger.warn("Formato de placa no reconocido: {}", normalizedPlate);
            throw new BusinessException("Formato de placa no reconocido: " + normalizedPlate +
                    ". Formatos válidos: ABC 123 (Carro), ABC 12A (Moto), 123 ABC (Motocarro).");
        }
    }

    /**
     * Formatea una placa para almacenamiento consistente.
     * 
     * @param plateNumber Número de placa sin formato.
     * @param type        Tipo de placa.
     * @return Placa formateada con espacio (ej: "ABC 123").
     */
    public String formatPlateForStorage(String plateNumber, PlateType type) {
        String clean = plateNumber.replaceAll("\\s+", "").toUpperCase();
        switch (type) {
            case CAR:
                return clean.substring(0, 3) + " " + clean.substring(3);
            case MOTORCYCLE:
                return clean.substring(0, 3) + " " + clean.substring(3);
            case MOTORCAR:
                return clean.substring(0, 3) + " " + clean.substring(3);
            default:
                return clean;
        }
    }
}