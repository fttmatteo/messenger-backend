package app.adapter.in.rest.response;

import app.domain.model.enums.Status;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO (Data Transfer Object) de respuesta para registros del historial de
 * estados de un servicio.
 * 
 * Este objeto representa un cambio de estado en el ciclo de vida de un servicio
 * de entrega,
 * proporcionando trazabilidad completa de quién realizó el cambio, cuándo
 * ocurrió,
 * y qué evidencias fotográficas se asociaron al cambio.
 * 
 * Campos incluidos:
 * - idStatusHistory: Identificador único del registro histórico
 * - previousStatus: Estado anterior del servicio
 * - newStatus: Nuevo estado asignado
 * - changeDate: Fecha y hora del cambio de estado
 * - changedBy: Empleado que realizó el cambio (para auditoría)
 * - photos: Lista de fotografías asociadas a este cambio de estado
 * 
 * Este registro permite auditar completamente el flujo de un servicio de
 * entrega.
 * 
 * @see app.adapter.in.rest.controllers.ServiceDeliveryController
 * @see app.domain.model.StatusHistory
 * @see app.domain.model.enums.Status
 */
public class StatusHistoryResponse {
    private Long idStatusHistory;
    private Status previousStatus;
    private Status newStatus;
    private LocalDateTime changeDate;
    private EmployeeResponse changedBy;
    private List<PhotoResponse> photos;

    public StatusHistoryResponse() {
    }

    public StatusHistoryResponse(Long idStatusHistory, Status previousStatus, Status newStatus,
            LocalDateTime changeDate, EmployeeResponse changedBy) {
        this.idStatusHistory = idStatusHistory;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changeDate = changeDate;
        this.changedBy = changedBy;
    }

    public Long getIdStatusHistory() {
        return idStatusHistory;
    }

    public void setIdStatusHistory(Long idStatusHistory) {
        this.idStatusHistory = idStatusHistory;
    }

    public Status getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(Status previousStatus) {
        this.previousStatus = previousStatus;
    }

    public Status getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(Status newStatus) {
        this.newStatus = newStatus;
    }

    public LocalDateTime getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(LocalDateTime changeDate) {
        this.changeDate = changeDate;
    }

    public EmployeeResponse getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(EmployeeResponse changedBy) {
        this.changedBy = changedBy;
    }

    public List<PhotoResponse> getPhotos() {
        return photos;
    }

    public void setPhotos(List<PhotoResponse> photos) {
        this.photos = photos;
    }
}
