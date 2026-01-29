package app.adapter.in.rest.response;

/**
 * Respuesta del endpoint de extracción de placa mediante OCR.
 * Permite previsualizar la placa detectada antes de crear el servicio.
 */
public class PlateExtractionResponse {

    private String plate;
    private boolean success;
    private String message;

    public PlateExtractionResponse() {
    }

    public PlateExtractionResponse(String plate, boolean success, String message) {
        this.plate = plate;
        this.success = success;
        this.message = message;
    }

    /**
     * Factory method para respuesta exitosa.
     */
    public static PlateExtractionResponse success(String plate) {
        return new PlateExtractionResponse(plate, true, "Placa detectada correctamente");
    }

    /**
     * Factory method para respuesta fallida.
     */
    public static PlateExtractionResponse failure(String message) {
        return new PlateExtractionResponse(null, false, message);
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
