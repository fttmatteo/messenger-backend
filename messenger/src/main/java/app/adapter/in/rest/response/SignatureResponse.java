package app.adapter.in.rest.response;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) de respuesta para firmas digitales de recepción.
 * 
 * Este objeto representa una firma digital capturada como evidencia de
 * recepción
 * de un servicio de entrega, típicamente utilizada para confirmar entregas
 * exitosas.
 * 
 * Campos incluidos:
 * - idSignature: Identificador único de la firma
 * - signaturePath: Ruta de acceso al archivo de imagen de la firma
 * - uploadDate: Fecha y hora de carga de la firma
 * 
 * Las firmas se almacenan como imágenes en el sistema de archivos o en Google
 * Cloud Storage.
 * 
 * @see app.adapter.in.rest.controllers.ServiceDeliveryController
 * @see app.domain.model.Signature
 */
public class SignatureResponse {
    private Long idSignature;
    private String signaturePath;
    private LocalDateTime uploadDate;

    public SignatureResponse() {
    }

    public SignatureResponse(Long idSignature, String signaturePath, LocalDateTime uploadDate) {
        this.idSignature = idSignature;
        this.signaturePath = signaturePath;
        this.uploadDate = uploadDate;
    }

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
