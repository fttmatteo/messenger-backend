package app.infrastructure.persistence.entities;

import app.domain.model.enums.Status;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA para servicios archivados permanentemente.
 * Contiene datos desnormalizados para preservar información incluso si se
 * borran
 * las entidades relacionadas (empleados, concesionarios, etc.)
 */
@Entity
@Table(name = "deleted_services")
public class DeletedServiceEntity {

    @Id
    @Column(name = "id_service_delivery")
    private Long idServiceDelivery;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    private Status currentStatus;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;



    @Column(name = "plate_id")
    private Long plateId;

    @Column(name = "dealership_id")
    private Long dealershipId;

    @Column(name = "messenger_id")
    private Long messengerId;

    @Column(name = "signature_id")
    private Long signatureId;

    @Column(name = "permanently_deleted_at", nullable = false)
    private LocalDateTime permanentlyDeletedAt;

    @Column(name = "permanently_deleted_by")
    private Long permanentlyDeletedBy;

    @Column(name = "deletion_reason")
    private String deletionReason;

    @Column(name = "messenger_name")
    private String messengerName;

    @Column(name = "messenger_document")
    private String messengerDocument;

    @Column(name = "messenger_phone")
    private String messengerPhone;

    @Column(name = "dealership_name")
    private String dealershipName;

    @Column(name = "dealership_address", length = 500)
    private String dealershipAddress;

    @Column(name = "dealership_zone", length = 100)
    private String dealershipZone;

    @Column(name = "origin_dealership_id", nullable = false)
    private Long originDealershipId;

    @Column(name = "origin_dealership_name", nullable = false)
    private String originDealershipName;

    @Column(name = "origin_dealership_address", nullable = false, length = 500)
    private String originDealershipAddress;

    @Column(name = "origin_dealership_zone", nullable = false, length = 100)
    private String originDealershipZone;

    @Column(name = "plate_number", nullable = false)
    private String plateNumber;

    @Column(name = "plate_type", nullable = false)
    private String plateType;

    public DeletedServiceEntity() {
    }

    public Long getIdServiceDelivery() {
        return idServiceDelivery;
    }

    public void setIdServiceDelivery(Long idServiceDelivery) {
        this.idServiceDelivery = idServiceDelivery;
    }

    public Status getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(Status currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Long getPlateId() {
        return plateId;
    }

    public void setPlateId(Long plateId) {
        this.plateId = plateId;
    }

    public Long getDealershipId() {
        return dealershipId;
    }

    public void setDealershipId(Long dealershipId) {
        this.dealershipId = dealershipId;
    }

    public Long getMessengerId() {
        return messengerId;
    }

    public void setMessengerId(Long messengerId) {
        this.messengerId = messengerId;
    }

    public Long getSignatureId() {
        return signatureId;
    }

    public void setSignatureId(Long signatureId) {
        this.signatureId = signatureId;
    }

    public LocalDateTime getPermanentlyDeletedAt() {
        return permanentlyDeletedAt;
    }

    public void setPermanentlyDeletedAt(LocalDateTime permanentlyDeletedAt) {
        this.permanentlyDeletedAt = permanentlyDeletedAt;
    }

    public Long getPermanentlyDeletedBy() {
        return permanentlyDeletedBy;
    }

    public void setPermanentlyDeletedBy(Long permanentlyDeletedBy) {
        this.permanentlyDeletedBy = permanentlyDeletedBy;
    }

    public String getDeletionReason() {
        return deletionReason;
    }

    public void setDeletionReason(String deletionReason) {
        this.deletionReason = deletionReason;
    }

    public String getMessengerName() {
        return messengerName;
    }

    public void setMessengerName(String messengerName) {
        this.messengerName = messengerName;
    }

    public String getMessengerDocument() {
        return messengerDocument;
    }

    public void setMessengerDocument(String messengerDocument) {
        this.messengerDocument = messengerDocument;
    }

    public String getMessengerPhone() {
        return messengerPhone;
    }

    public void setMessengerPhone(String messengerPhone) {
        this.messengerPhone = messengerPhone;
    }

    public String getDealershipName() {
        return dealershipName;
    }

    public void setDealershipName(String dealershipName) {
        this.dealershipName = dealershipName;
    }

    public String getDealershipAddress() {
        return dealershipAddress;
    }

    public void setDealershipAddress(String dealershipAddress) {
        this.dealershipAddress = dealershipAddress;
    }

    public String getDealershipZone() {
        return dealershipZone;
    }

    public void setDealershipZone(String dealershipZone) {
        this.dealershipZone = dealershipZone;
    }

    public Long getOriginDealershipId() {
        return originDealershipId;
    }

    public void setOriginDealershipId(Long originDealershipId) {
        this.originDealershipId = originDealershipId;
    }

    public String getOriginDealershipName() {
        return originDealershipName;
    }

    public void setOriginDealershipName(String originDealershipName) {
        this.originDealershipName = originDealershipName;
    }

    public String getOriginDealershipAddress() {
        return originDealershipAddress;
    }

    public void setOriginDealershipAddress(String originDealershipAddress) {
        this.originDealershipAddress = originDealershipAddress;
    }

    public String getOriginDealershipZone() {
        return originDealershipZone;
    }

    public void setOriginDealershipZone(String originDealershipZone) {
        this.originDealershipZone = originDealershipZone;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getPlateType() {
        return plateType;
    }

    public void setPlateType(String plateType) {
        this.plateType = plateType;
    }
}
