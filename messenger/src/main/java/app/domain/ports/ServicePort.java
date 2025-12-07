package app.domain.ports;

import app.domain.model.Service;
import java.util.List;

public interface ServicePort {
    Service save(Service service) throws Exception;

    Service findById(Long idService) throws Exception;

    List<Service> findAllByEmployee(Long idEmployee) throws Exception;

    List<Service> findAllByEmployeeUserName(String username) throws Exception;
}