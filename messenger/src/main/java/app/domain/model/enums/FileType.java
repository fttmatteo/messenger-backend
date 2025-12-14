package app.domain.model.enums;

/**
 * Enumeración de tipos de archivos almacenados en el sistema.
 * 
 * PHOTO: Archivo de fotografía/imagen
 * SIGNATURE: Archivo de firma digital
 */
public enum FileType {
    /**
     * Archivo de fotografía o imagen (evidencia, placa, etc).
     */
    PHOTO,

    /**
     * Archivo de firma digital capturada en el dispositivo.
     */
    SIGNATURE
}
