package app.adapter.out.persistence;

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
 * Adaptador de salida para persistencia de concesionarios.
 * 
 * Este adaptador implementa DealershipPort y actúa como puente entre la capa de
 * dominio
 * y la capa de infraestructura (JPA), manejando la conversión entre objetos de
 * dominio
 * (Dealership) y entidades de persistencia (DealershipEntity).
 * 
 * Responsabilidades:
 * - Convertir objetos de dominio a entidades JPA y viceversa usando
 * DealershipMapper
 * - Delegar operaciones de persistencia al DealershipRepository
 * - Mantener la independencia del dominio respecto a detalles de persistencia
 * 
 * Operaciones soportadas:
 * - save: Guardar o actualizar un concesionario
 * - findById: Buscar por ID
 * - findByName: Buscar por nombre
 * - findAll: Obtener todos los concesionarios
 * - deleteById: Eliminar por ID
 * - deleteByName: Eliminar por nombre
 * 
 * @see app.domain.ports.DealershipPort
 * @see app.infrastructure.persistence.repository.DealershipRepository
 * @see app.infrastructure.persistence.mapper.DealershipMapper
 */
@Component
public class DealershipAdapter implements DealershipPort {

    @Autowired
    private DealershipRepository repository;
    @Autowired
    private DealershipMapper mapper;

    /**
     * Guarda o actualiza un concesionario en la base de datos.
     * Actualiza el ID del objeto de dominio con el ID generado.
     */
    @Override
    public void save(Dealership dealership) {
        DealershipEntity entity = mapper.toEntity(dealership);
        DealershipEntity savedEntity = repository.save(entity);
        dealership.setIdDealership(savedEntity.getIdDealership());
    }

    /** Busca un concesionario por ID. */
    @Override
    public Dealership findById(Long id) {
        Optional<DealershipEntity> entity = repository.findById(id);
        if (entity.isPresent()) {
            return mapper.toDomain(entity.get());
        }
        return null;
    }

    /** Obtiene todos los concesionarios. */
    @Override
    public List<Dealership> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    /** Elimina un concesionario por ID. */
    @Override
    public void deleteById(Long idDealership) {
        repository.deleteById(idDealership);
    }

    /** Elimina un concesionario por nombre. */
    @Override
    public void deleteByName(String name) {
        repository.deleteByName(name);
    }

    /** Busca un concesionario por nombre. */
    @Override
    public Dealership findByName(String name) {
        DealershipEntity entity = repository.findByName(name);
        if (entity != null) {
            return mapper.toDomain(entity);
        }
        return null;
    }
}