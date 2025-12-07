package app.adapter.in.validators;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import app.application.exceptions.InputsException;
import app.domain.model.enums.PlateType; // Asegúrate de importar esto

@Component
public class PlateValidator extends SimpleValidator {

    // Método que ya tenías (valida el formato general)
    public String plateNumberValidator(String value) throws InputsException {
        String plate = stringValidator("número de placa", value).toUpperCase();
        // Regex general que acepta los 3 formatos
        String regex = "^([A-Z]{3}\\s\\d{2}[A-Z]|[A-Z]{3}\\s\\d{3}|\\d{3}\\s[A-Z]{3})$";

        if (!plate.matches(regex)) {
            throw new InputsException(
                    "El formato de la placa no es válido. Formatos aceptados: 'ABC 123', 'ABC 12A', '123 ABC'");
        }
        return plate;
    }

    // Nuevo método: Identifica el tipo basándose en las reglas de formato
    public PlateType identifyPlateType(String plate) {
        if (plate.matches("^[A-Z]{3}\\s\\d{3}$")) {
            return PlateType.CAR;
        } else if (plate.matches("^[A-Z]{3}\\s\\d{2}[A-Z]$")) {
            return PlateType.MOTORCYCLE;
        } else if (plate.matches("^\\d{3}\\s[A-Z]{3}$")) {
            return PlateType.MOTORCAR;
        }
        // Este caso no debería alcanzarse si se llamó a plateNumberValidator antes,
        // pero por seguridad retornamos null o lanzamos excepción.
        return null;
    }

    // Método de validación de OCR (que añadimos en el paso anterior)
    public void validateOCRInput(MultipartFile image, Long idDealership) throws InputsException {
        notNullValidator("El concesionario", idDealership);
        fileValidator("La foto de la placa", image);
    }
}