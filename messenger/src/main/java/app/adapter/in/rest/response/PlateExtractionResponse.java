package app.adapter.in.rest.response;

/**
 * Respuesta del endpoint de extracción de placa mediante OCR.
 * Permite previsualizar la placa detectada antes de crear el servicio.
 */
public class PlateExtractionResponse {

    private String plate;
    private boolean success;
    private String message;
    private Double score;

    public PlateExtractionResponse() {
    }

    public PlateExtractionResponse(String plate, boolean success, String message, Double score) {
        this.plate = plate;
        this.success = success;
        this.message = message;
        this.score = score;
    }

    /**
     * Factory method para respuesta exitosa.
     */
    public static PlateExtractionResponse success(String plate, Double score) {
        return new PlateExtractionResponse(plate, true, "Chasis detectado correctamente", score);
    }

    /**
     * Factory method para respuesta fallida.
     */
    public static PlateExtractionResponse failure(String message) {
        return new PlateExtractionResponse(null, false, message, null);
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

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
