package app.adapter.out.persistence.adapter;

import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;
import app.adapter.out.persistence.entities.DealershipEntity;
import app.adapter.out.persistence.mapper.DealershipMapper;
import app.adapter.out.persistence.repository.DealershipRepository;
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

    /**
     * Busca todos los concesionarios delegando al repositorio JPA.
     */
    @Override
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
    /**
     * Busca un concesionario por su PIN de WhatsApp.
     */
    public Dealership findByWhatsappPin(String whatsappPin) {
        Optional<DealershipEntity> entity = repository.findByWhatsappPin(whatsappPin);
        return entity.map(mapper::toDomain).orElse(null);
    }

    @Override
    /**
     * Busca concesionario por UUID público.
     */
    public Dealership findByUuid(String uuid) {
        Optional<DealershipEntity> entity = repository.findByUuid(uuid);
        return entity.map(mapper::toDomain).orElse(null);
    }
}
