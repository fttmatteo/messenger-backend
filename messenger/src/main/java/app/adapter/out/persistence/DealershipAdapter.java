package app.adapter.out.persistence;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;
import app.infrastructure.persistence.entities.DealershipEntity;
import app.infrastructure.persistence.mapper.DealershipMapper;
import app.infrastructure.persistence.repository.DealershipRepository;

@Service
public class DealershipAdapter implements DealershipPort {

    @Autowired
    private DealershipRepository dealershipRepository;

    @Override
    public void save(Dealership dealership) throws Exception {
        DealershipEntity entity = DealershipMapper.toEntity(dealership);
        DealershipEntity saved = dealershipRepository.save(entity);
        dealership.setIdDealership(saved.getIdDealership());
    }

    @Override
    public void deleteById(Long idDealership) throws Exception {
        dealershipRepository.deleteById(idDealership);
    }

    @Override
    public Dealership findById(Long idDealership) throws Exception {
        Optional<DealershipEntity> entity = dealershipRepository.findById(idDealership);
        return entity.map(DealershipMapper::toDomain).orElse(null);
    }
}