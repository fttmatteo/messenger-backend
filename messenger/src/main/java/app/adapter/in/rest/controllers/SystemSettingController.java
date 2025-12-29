package app.adapter.in.rest.controllers;

import app.application.usecase.SystemSettingUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
public class SystemSettingController {

    @Autowired
    private SystemSettingUseCase systemSettingUseCase;

    @GetMapping(value = "/status-colors", produces = "application/json")
    public ResponseEntity<String> getStatusColors() {
        return ResponseEntity.ok(systemSettingUseCase.getStatusColors());
    }

    @PutMapping("/status-colors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStatusColors(@RequestBody String colorsJson) {
        systemSettingUseCase.updateStatusColors(colorsJson);
        return ResponseEntity.ok().build();
    }
}
