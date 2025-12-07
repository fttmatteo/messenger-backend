package app.application.usecase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.services.CreatePlate;
import app.domain.model.Plate;

@Service
public class MessengerUseCase {

    @Autowired
    private CreatePlate createPlateService;

    public void createPlate(Plate plate) throws Exception {
        createPlateService.create(plate);
    }
}
