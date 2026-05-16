package app.domain.services;

import org.springframework.stereotype.Service;
import app.domain.exception.BusinessException;
import app.domain.model.enums.PlateType;

/**
 * Servicio para reconocimiento y clasificación de chasis
 */
@Service
public class PlateRecognition {

    /**
     * Determina el tipo de vehículo basado en el formato del chasis.
     * En este proyecto, todos los seriales de chasis se clasifican como MOTORCYCLE.
     */
    public PlateType determinePlateType(String plateNumber) throws BusinessException {
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            throw new BusinessException("El número de chasis no puede estar vacío.");
        }

        String normalized = plateNumber.trim().toUpperCase().replaceAll("\\s+", "");

        if (normalized.length() >= 10 && normalized.length() <= 20) {
            return PlateType.MOTORCYCLE;
        }

        throw new BusinessException("El número de chasis no tiene una longitud válida (10-20): " + normalized);
    }

    /**
     * Formatea un chasis para su almacenamiento estandarizado.
     */
    public String formatPlateForStorage(String plateNumber, PlateType type) {
        return plateNumber.replaceAll("\\s+", "").toUpperCase();
    }
}