package app.domain.model;

import java.time.LocalDateTime;
import app.domain.model.enums.PlateType;

public class Plate {
    private Long id_plate;
    private String plateNumber;
    private PlateType plateType;
    private LocalDateTime registrationDate;
    private Employee employee;
    private Service service;

    public Long getId_plate() {
        return id_plate;
    }

    public void setId_plate(Long id_plate) {
        this.id_plate = id_plate;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public PlateType getPlateType() {
        return plateType;
    }

    public void setPlateType(PlateType plateType) {
        this.plateType = plateType;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

}
