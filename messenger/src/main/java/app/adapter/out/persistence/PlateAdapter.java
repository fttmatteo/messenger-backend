package app.adapter.out.persistence;

import app.domain.model.Plate;
import app.domain.ports.PlatePort;
import app.infrastructure.persistence.entities.PlateEntity;
import app.infrastructure.persistence.mapper.PlateMapper;
import app.infrastructure.persistence.repository.PlateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para la entidad Plate.
 * Implementa PlatePort para buscar y guardar placas vehiculares.
 */
@Component
public class PlateAdapter implements PlatePort {

    @Autowired
    private PlateRepository repository;
    @Autowired
    private PlateMapper mapper;

    @Override
    public void save(Plate plate) {
        PlateEntity entity = mapper.toEntity(plate);
        PlateEntity savedEntity = repository.save(entity);
        plate.setIdPlate(savedEntity.getIdPlate());
    }

    @Override
    public Plate findById(Long id) {
        Optional<PlateEntity> entity = repository.findById(id);
        if (entity.isPresent()) {
            return mapper.toDomain(entity.get());
        }
        return null;
    }

    @Override
    public Plate findByPlateNumber(String plateNumber) {
        PlateEntity entity = repository.findByPlateNumber(plateNumber);
        if (entity != null) {
            return mapper.toDomain(entity);
        }
        return null;
    }

    @Override
    public List<Plate> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}