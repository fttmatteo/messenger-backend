package app.adapter.in.builder;

import app.domain.model.Dealership;
import app.domain.model.Employee;
import app.domain.model.Plate;
import app.domain.model.Service;
import app.domain.model.StatusHistory;
import app.domain.model.enums.Status;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
public class ServiceBuilder {

    public Service build(Plate plate, Employee messenger, Dealership dealership) {
        Service service = new Service();
        service.setPlate(plate);
        service.setEmployee(messenger);
        service.setStatus(Status.ASSIGNED);
        service.setAssignedDate(LocalDateTime.now());
        service.setObservation("Asignación inicial automática");
        service.setPlateType(plate.getPlateType());
        service.setStatusHistory(new ArrayList<>());

        // Lógica de historial inicial movida al Builder
        StatusHistory history = new StatusHistory();
        history.setNewStatus(Status.ASSIGNED);
        history.setChangeDate(LocalDateTime.now());
        history.setObservation("Asignación inicial automática");
        history.setModifiedBy(messenger.getUserName());
        history.setService(service);

        service.getStatusHistory().add(history);

        return service;
    }
}