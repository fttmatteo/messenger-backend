package app.domain.ports;

import java.security.Provider.Service;

public interface ServicePort {
    void save(Service service) throws Exception;

    void deleteById(Long idService) throws Exception;

    Service findById(Long idService) throws Exception;
}
