package app.adapter.out.persistence;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.model.Plate;
import app.domain.ports.PlatePort;
import app.infrastructure.persistence.entities.PlateEntity;
import app.infrastructure.persistence.mapper.PlateMapper;
import app.infrastructure.persistence.repository.PlateRepository;

@Service
public class PlateAdapter implements PlatePort {

    @Autowired
    private PlateRepository plateRepository;

    @Override
    public void save(Plate plate) throws Exception {
        PlateEntity entity = PlateMapper.toEntity(plate);
        PlateEntity saved = plateRepository.save(entity);
        plate.setIdPlate(saved.getIdPlate());
    }

    @Override
    public void deleteById(Long idPlate) throws Exception {
        if (plateRepository.existsById(idPlate)) {
            plateRepository.deleteById(idPlate);
        }
    }

    @Override
    public Plate findById(Long idPlate) throws Exception {
        Optional<PlateEntity> entity = plateRepository.findById(idPlate);
        return entity.map(PlateMapper::toDomain).orElse(null);
    }

    @Override
    public Plate findByPlateNumber(String plateNumber) throws Exception {
        PlateEntity entity = plateRepository.findByPlateNumber(plateNumber);
        return PlateMapper.toDomain(entity);
    }
}