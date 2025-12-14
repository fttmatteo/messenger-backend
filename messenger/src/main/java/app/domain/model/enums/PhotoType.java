package app.domain.model.enums;

/**
 * Enumeración de tipos de fotografías en el sistema.
 * 
 * PLATE_DETECTION: Foto inicial de la placa para detección OCR
 * EVIDENCE: Foto de evidencia de entrega o visita
 */
public enum PhotoType {
    /**
     * Foto inicial tomada a la placa para el proceso de detección OCR.
     */
    PLATE_DETECTION,

    /**
     * Foto de evidencia que respalda una entrega, visita o estado del servicio.
     */
    EVIDENCE
}