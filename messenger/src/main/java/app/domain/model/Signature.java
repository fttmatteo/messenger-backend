package app.domain.model;

import java.time.LocalDateTime;

/**
 * Modelo de dominio que representa una firma digital de confirmación de
 * entrega.
 * 
 * La firma es capturada del asesor del concesionario que recibe la placa
 * vehicular, sirviendo como evidencia legal de que la entrega fue completada
 * exitosamente.
 * 
 * La firma se almacena como imagen en el sistema de archivos y se asocia
 * directamente con el servicio de entrega correspondiente.
 * 
 */
public class Signature {
    private Long idSignature;
    private String signaturePath;
    private LocalDateTime uploadDate;

    public Long getIdSignature() {
        return idSignature;
    }

    public void setIdSignature(Long idSignature) {
        this.idSignature = idSignature;
    }

    public String getSignaturePath() {
        return signaturePath;
    }

    public void setSignaturePath(String signaturePath) {
        this.signaturePath = signaturePath;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}