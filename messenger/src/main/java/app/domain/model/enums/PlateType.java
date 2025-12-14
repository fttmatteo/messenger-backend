package app.domain.model.enums;

/**
 * Enumeración de tipos de placas vehiculares soportadas por el sistema OCR.
 * 
 * Cada tipo tiene un formato específico reconocido automáticamente:
 * 
 * CAR: Automóvil - Formato ABC 123
 * MOTORCYCLE: Motocicleta - Formato ABC 12A
 * MOTORCAR: Moto - Formato 123 ABC
 */
public enum PlateType {
    /**
     * Placa de automóvil.
     * Formato esperado: ABC 123
     */
    CAR,

    /**
     * Placa de motocicleta.
     * Formato esperado: ABC 12A
     */
    MOTORCYCLE,

    /**
     * Placa de motocarro.
     * Formato esperado: 123 ABC
     */
    MOTORCAR
}