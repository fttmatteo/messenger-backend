package app.adapter.in.rest.controllers;

import org.springframework.web.bind.annotation.RestController;
import app.application.exceptions.BusinessException;
import app.application.exceptions.InputsException;
import app.domain.model.Plate;
import app.adapter.in.builder.PlateBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import app.application.usecase.MessengerUseCase;

@RestController
@RequestMapping("/messenger")
@PreAuthorize("hasRole('MESSENGER')")
public class MessengerController {

    @Autowired
    private PlateBuilder plateBuilder;

    @Autowired
    private MessengerUseCase messengerUseCase;

    @PostMapping("/plates")
    @PreAuthorize("hasRole('MESSENGER')")
    public ResponseEntity<?> createPlate(@RequestBody app.adapter.in.rest.request.PlateRequest request) {
        try {
            Plate plate = plateBuilder.build(request.getPlateNumber());
            messengerUseCase.createPlate(plate);
            return ResponseEntity.status(HttpStatus.CREATED).body(plate);
        } catch (InputsException ie) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ie.getMessage());
        } catch (BusinessException be) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(be.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
