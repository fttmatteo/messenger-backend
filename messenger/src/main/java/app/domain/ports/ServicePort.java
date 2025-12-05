package app.domain.ports;

import java.security.Provider.Service;

public interface ServicePort {
    void save(Service service) throws Exception;
    void update(Service service) throws Exception;
    void deleteById(Long id) throws Exception;
    Service findById(Long id) throws Exception;
}
