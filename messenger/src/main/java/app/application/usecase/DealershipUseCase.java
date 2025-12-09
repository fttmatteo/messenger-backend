package app.application.usecase;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.domain.model.Dealership;
import app.domain.services.CreateDealership;
import app.domain.services.DeleteDealership;
import app.domain.services.SearchDealership;
import app.domain.services.UpdateDealership;

@Service
public class DealershipUseCase {

    @Autowired
    private CreateDealership createDealership;
    @Autowired
    private UpdateDealership updateDealership;
    @Autowired
    private SearchDealership searchDealership;
    @Autowired
    private DeleteDealership deleteDealership;

    public void create(Dealership dealership) throws Exception {
        createDealership.create(dealership);
    }

    public void update(Long id, Dealership dealership) throws Exception {
        updateDealership.update(id, dealership);
    }

    public Dealership findById(Long id) throws Exception {
        return searchDealership.findById(id);
    }

    public Dealership findByName(String name) throws Exception {
        return searchDealership.findByName(name);
    }

    public List<Dealership> findAll() {
        return searchDealership.findAll();
    }

    public void deleteById(Long id) throws Exception {
        deleteDealership.deleteById(id);
    }

    public void deleteByName(String name) throws Exception {
        deleteDealership.deleteByName(name);
    }
}