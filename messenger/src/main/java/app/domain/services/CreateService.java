package app.domain.services;

import app.domain.model.Employee;
import app.domain.model.Plate;
import app.domain.model.Service;
import app.domain.ports.ServicePort;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Service
public class CreateService {

    @Autowired
    private ServicePort servicePort;

    public void create(Service service) throws Exception {
        // Aquí irían validaciones de negocio si fueran necesarias antes de guardar
        servicePort.save(service);
    }

    public void createServiceFromPlate(Plate plate, Employee adminUser, Long idDealership) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createServiceFromPlate'");
    }
}