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
 * Adaptador de salida para persistencia de placas vehiculares.
 * 
 * Este adaptador implementa PlatePort y actúa como puente entre la capa de
 * dominio
 * y la capa de infraestructura (JPA), manejando la conversión entre objetos de
 * dominio
 * (Plate) y entidades de persistencia (PlateEntity).
 * 
 * Responsabilidades:
 * - Convertir objetos de dominio a entidades JPA y viceversa usando PlateMapper
 * - Delegar operaciones de persistencia al PlateRepository
 * - Mantener la independencia del dominio respecto a detalles de persistencia
 * 
 * Operaciones soportadas:
 * - save: Guardar o actualizar una placa
 * - findById: Buscar por ID
 * - findByPlateNumber: Buscar por número de placa (único)
 * - findAll: Obtener todas las placas registradas
 * 
 * Las placas se asocian a servicios de entrega y pueden ser detectadas mediante
 * OCR
 * o ingresadas manualmente.
 * 
 * @see app.domain.ports.PlatePort
 * @see app.infrastructure.persistence.repository.PlateRepository
 * @see app.infrastructure.persistence.mapper.PlateMapper
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