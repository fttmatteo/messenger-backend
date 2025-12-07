package app.adapter.out.persistence;

import app.domain.model.Service;
import app.domain.ports.ServicePort;
import app.infrastructure.persistence.entities.ServiceEntity;
import app.infrastructure.persistence.entities.StatusHistoryEntity;
import app.infrastructure.persistence.mapper.ServiceMapper;
import app.infrastructure.persistence.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ServiceAdapter implements ServicePort {

    @Autowired
    private ServiceRepository serviceRepository;

    @Override
    public Service save(Service service) throws Exception {
        ServiceEntity entity = ServiceMapper.toEntity(service);

        if (service.getStatusHistory() != null && !service.getStatusHistory().isEmpty()) {
            app.domain.model.StatusHistory lastDomainHistory = service.getStatusHistory()
                    .get(service.getStatusHistory().size() - 1);

            StatusHistoryEntity historyEntity = new StatusHistoryEntity();
            historyEntity.setPreviousStatus(
                    lastDomainHistory.getPreviousStatus() != null ? lastDomainHistory.getPreviousStatus().name()
                            : null);
            historyEntity.setNewStatus(lastDomainHistory.getNewStatus().name());
            historyEntity.setObservation(lastDomainHistory.getObservation());
            historyEntity.setChangeDate(lastDomainHistory.getChangeDate());
            historyEntity.setChangedBy(lastDomainHistory.getModifiedBy());
            historyEntity.setService(entity);
            entity.getStatusHistory().add(historyEntity);
        }

        ServiceEntity saved = serviceRepository.save(entity);
        return ServiceMapper.toDomain(saved);
    }

    @Override
    public Service findById(Long idService) throws Exception {
        return serviceRepository.findById(idService)
                .map(ServiceMapper::toDomain)
                .orElse(null);
    }

    @Override
    public List<Service> findAllByEmployee(Long idEmployee) throws Exception {
        return serviceRepository.findAllByEmployee_IdEmployee(idEmployee).stream()
                .map(ServiceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Service> findAllByEmployeeUserName(String username) throws Exception {
        return serviceRepository.findAllByEmployee_UserName(username).stream()
                .map(ServiceMapper::toDomain)
                .collect(Collectors.toList());
    }
}