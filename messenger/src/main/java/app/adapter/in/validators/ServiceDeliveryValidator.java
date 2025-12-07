package app.adapter.in.validators;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import app.application.exceptions.BusinessException;
import app.domain.model.Photo;
import app.domain.model.Signature;
import app.domain.model.enums.PlateType;
import app.domain.model.enums.Status;

@Component
public class ServiceDeliveryValidator extends SimpleValidator {

    // Patrones Regex
    private static final Pattern MOTO_PATTERN = Pattern.compile("^[A-Za-z]{3}\\s\\d{2}[A-Za-z]$");
    private static final Pattern CAR_PATTERN = Pattern.compile("^[A-Za-z]{3}\\s\\d{3}$");
    private static final Pattern MOTORCAR_PATTERN = Pattern.compile("^\\d{3}\\s[A-Za-z]{3}$");

    /**
     * Valida el formato de la placa y determina su tipo.
     */
    public PlateType identifyPlateType(String plate) throws BusinessException {
        if (plate == null) {
            throw new BusinessException("La placa no puede ser nula");
        }
        
        // Normalizamos a mayúsculas por si acaso
        String normalizedPlate = plate.toUpperCase();

        if (MOTO_PATTERN.matcher(normalizedPlate).matches()) {
            return PlateType.MOTORCYCLE;
        } else if (CAR_PATTERN.matcher(normalizedPlate).matches()) {
            return PlateType.CAR;
        } else if (MOTORCAR_PATTERN.matcher(normalizedPlate).matches()) {
            return PlateType.MOTORCAR;
        } else {
            throw new BusinessException("Formato de placa inválido: " + plate);
        }
    }

    /**
     * Valida los requisitos obligatorios según el estado al que se quiere cambiar.
     */
    public void validateStatusRequirements(Status status, Signature signature, Photo photo, String observation) throws BusinessException {
        if (status == Status.DELIVERED) {
            if (signature == null) {
                throw new BusinessException("Firma obligatoria para entregar.");
            }
        }
        
        if (status == Status.PENDING || status == Status.FAILED || status == Status.RETURNED) {
            if (signature == null || photo == null || observation == null || observation.trim().isEmpty()) {
                throw new BusinessException("Firma, Foto y Observación son obligatorias para reportar novedad (Pendiente, Fallido o Devuelto).");
            }
        }
    }
}