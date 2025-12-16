package app.adapter.in.rest.response;

/**
 * DTO para representar detalles específicos de errores de validación.
 * 
 * Se utiliza dentro de ErrorResponse para proporcionar información granular
 * sobre errores de validación de campos individuales.
 * 
 * Por ejemplo, si un formulario tiene múltiples campos inválidos, cada uno
 * tendrá su propio ErrorDetail con el nombre del campo y el mensaje específico
 * del error.
 * 
 * @see ErrorResponse
 */
public class ErrorDetail {

    /**
     * Nombre del campo que tiene el error de validación.
     * Por ejemplo: "document", "password", "email"
     */
    private String field;

    /**
     * Mensaje descriptivo del error de validación para este campo.
     * Por ejemplo: "la cédula no puede exceder 10 dígitos"
     */
    private String message;

    /**
     * Constructor por defecto requerido para deserialización JSON.
     */
    public ErrorDetail() {
    }

    /**
     * Constructor con todos los campos.
     * 
     * @param field   Nombre del campo con error
     * @param message Mensaje descriptivo del error
     */
    public ErrorDetail(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
