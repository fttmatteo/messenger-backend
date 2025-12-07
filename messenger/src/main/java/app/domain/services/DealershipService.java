package app.domain.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.adapter.in.validators.DealershipValidator;
import app.application.exceptions.BusinessException;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;

@Service
public class DealershipService {

    @Autowired
    private DealershipPort dealershipPort;
    @Autowired
    private DealershipValidator validator;

    public void create(Dealership dealership) throws Exception {
        validator.validateCreation(dealership);
        dealershipPort.save(dealership);
    }

    public void update(Dealership dealership) throws Exception {
        Dealership existing = dealershipPort.findById(dealership.getIdDealership());
        if (existing == null) {
            throw new BusinessException("El concesionario no existe.");
        }
        
        validator.validateUpdate(dealership, existing);
        dealershipPort.update(dealership);
    }

    public List<Dealership> findAll() {
        return dealershipPort.findAll();
    }

    public Dealership findById(Long id) {
        return dealershipPort.findById(id);
    }
    
    public Dealership findByName(String name) {
        return dealershipPort.findByName(name);
    }

    public void deleteById(Long id) throws Exception {
        if (dealershipPort.findById(id) == null) {
            throw new BusinessException("El concesionario no existe.");
        }
        validator.validateDelete(id);
        dealershipPort.deleteById(id);
    }
    
    public void deleteByName(String name) throws Exception {
        Dealership dealership = dealershipPort.findByName(name);
        if (dealership == null) {
            throw new BusinessException("El concesionario no existe.");
        }
        validator.validateDelete(dealership.getIdDealership());
        dealershipPort.deleteByName(name);
    }
}