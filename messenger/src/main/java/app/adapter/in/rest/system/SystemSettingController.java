package app.adapter.in.rest.system;

import app.application.usecase.SystemSettingUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión de configuraciones del sistema.
 */
@RestController
@RequestMapping("/settings")
public class SystemSettingController {

    @Autowired
    private SystemSettingUseCase systemSettingUseCase;

    /**
     * Obtiene la configuración actual de colores de estado.
     */
    @GetMapping(value = "/status-colors", produces = "application/json")
    public ResponseEntity<String> getStatusColors() {
        return ResponseEntity.ok(systemSettingUseCase.getStatusColors());
    }

    /**
     * Actualiza la configuración de los colores de estado.
     * Requiere rol ADMIN.
     */
    @PutMapping("/status-colors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStatusColors(@RequestBody String colorsJson) {
        systemSettingUseCase.updateStatusColors(colorsJson);
        return ResponseEntity.ok().build();
    }
}
