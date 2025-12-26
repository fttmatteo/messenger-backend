package app.infrastructure.persistence.adapter;

import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;
import app.infrastructure.persistence.entities.DealershipEntity;
import app.infrastructure.persistence.mapper.DealershipMapper;
import app.infrastructure.persistence.repository.DealershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter de persistencia para concesionarios.
 */
@Component
public class DealershipAdapter implements DealershipPort {

    @Autowired
    private DealershipRepository repository;
    @Autowired
    private DealershipMapper mapper;

    @Override
    /**
     * Guarda la entidad mapeada desde el dominio y devuelve el resultado mapeado de
     * nuevo.
     */
    public Dealership save(Dealership dealership) {
        DealershipEntity entity = mapper.toEntity(dealership);
        DealershipEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    /**
     * Busca por ID delegando al repositorio JPA.
     */
    public Dealership findById(Long id) {
        Optional<DealershipEntity> entity = repository.findById(id);
        if (entity.isPresent()) {
            return mapper.toDomain(entity.get());
        }
        return null;
    }

    @Override
    public List<Dealership> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long idDealership) {
        repository.deleteById(idDealership);
    }

    @Override
    /**
     * Busca por nombre delegando al repositorio JPA.
     */
    public Dealership findByName(String name) {
        DealershipEntity entity = repository.findByName(name);
        if (entity != null) {
            return mapper.toDomain(entity);
        }
        return null;
    }
}
