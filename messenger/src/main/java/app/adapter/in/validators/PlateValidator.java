package app.adapter.in.validators;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import app.application.exceptions.InputsException;
import app.domain.model.enums.PlateType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PlateValidator extends SimpleValidator {

    public String plateNumberValidator(String value) throws InputsException {
        // 1. Limpieza básica (Quitar espacios y todo lo que no sea alfanumérico)
        String cleanInput = stringValidator("número de placa", value)
                .toUpperCase()
                .replaceAll("[^A-Z0-9]", "");

        System.out.println("DEBUG - Texto limpio del OCR: '" + cleanInput + "'");

        // 2. --- CAMBIO CRÍTICO: PATRÓN DE BÚSQUEDA ---
        // Definimos los 3 formatos válidos:
        // A: AAA11A (Motos nuevas)
        // B: AAA111 (Carros)
        // C: 111AAA (Motos antiguas/vehículos especiales)
        String regexPattern = "([A-Z]{3}\\d{2}[A-Z]|[A-Z]{3}\\d{3}|\\d{3}[A-Z]{3})";

        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(cleanInput);

        // 3. Buscamos SI EXISTE la placa dentro del texto basura
        if (matcher.find()) {
            // matcher.group(0) devuelve lo que encontró que coincide con la placa
            String foundPlate = matcher.group(0);
            System.out.println("DEBUG - Placa encontrada: " + foundPlate);
            return foundPlate;
        } else {
            // Si después de buscar no encontramos nada parecido a una placa
            throw new InputsException(
                    "No se pudo detectar una placa válida en la imagen. Texto leído: " + cleanInput);
        }
    }

    public PlateType identifyPlateType(String value) {
        // 1. Limpiamos la entrada igual que en el validador principal
        String cleanPlate = value.toUpperCase().replaceAll("[^A-Z0-9]", "");

        // 2. Evaluamos los tipos SIN esperar espacios
        // CARRO: 3 letras y 3 números (Ej: ABC123)
        if (cleanPlate.matches("^[A-Z]{3}\\d{3}$")) {
            return PlateType.CAR;
        }
        // MOTO: 3 letras, 2 números, 1 letra (Ej: IVC86G)
        else if (cleanPlate.matches("^[A-Z]{3}\\d{2}[A-Z]$")) {
            return PlateType.MOTORCYCLE;
        }
        // CARRO ANTIGUO / OTROS: 3 números, 3 letras (Ej: 123ABC)
        else if (cleanPlate.matches("^\\d{3}[A-Z]{3}$")) {
            return PlateType.MOTORCAR;
        }

        return null; // Si llega aquí, lanzará el error de base de datos, pero ahora debería entrar
                     // en los if.
    }

    public void validateOCRInput(MultipartFile image, Long idDealership) throws InputsException {
        notNullValidator("El concesionario", idDealership);
        fileValidator("La foto de la placa", image);
    }

    public void validateCreatePlate(String plateNumber, Long idDealership) throws InputsException {
        plateNumberValidator(plateNumber);
        notNullValidator("El id del concesionario", idDealership);
    }
}