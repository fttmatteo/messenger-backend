package app.domain.model;

import java.time.LocalDateTime;

public class Signature {
    private Long id_signature;
    private String signature_path;
    private LocalDateTime upload_date;
    private Service service;
    private Employee employee;

    public Long getId_signature() {
        return id_signature;
    }

    public void setId_signature(Long id_signature) {
        this.id_signature = id_signature;
    }

    public String getSignature_path() {
        return signature_path;
    }

    public void setSignature_path(String signature_path) {
        this.signature_path = signature_path;
    }

    public LocalDateTime getUpload_date() {
        return upload_date;
    }

    public void setUpload_date(LocalDateTime upload_date) {
        this.upload_date = upload_date;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}