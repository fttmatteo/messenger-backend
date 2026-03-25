package app.infrastructure.persistence.adapter;

import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;
import app.infrastructure.persistence.entities.DealershipEntity;
import app.infrastructure.persistence.mapper.DealershipMapper;
import app.infrastructure.persistence.repository.DealershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
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
    @Transactional(readOnly = true)
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

    /**
     * Busca todos los concesionarios delegando al repositorio JPA.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Dealership> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Elimina un concesionario por su ID delegando al repositorio JPA.
     */
    @Override
    public void deleteById(Long idDealership) {
        repository.deleteById(idDealership);
    }

    @Override
    @Transactional(readOnly = true)
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

    @Override
    @Transactional(readOnly = true)
    /**
     * Busca un concesionario por su PIN de WhatsApp.
     */
    public Dealership findByWhatsappPin(String whatsappPin) {
        Optional<DealershipEntity> entity = repository.findByWhatsappPin(whatsappPin);
        return entity.map(mapper::toDomain).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * Busca concesionario por UUID público.
     */
    public Dealership findByUuid(String uuid) {
        Optional<DealershipEntity> entity = repository.findByUuid(uuid);
        return entity.map(mapper::toDomain).orElse(null);
    }
}
