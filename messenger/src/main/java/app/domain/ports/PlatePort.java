// Archivo: app/domain/ports/PlatePort.java
package app.domain.ports;

import app.domain.model.Plate;
import java.util.List;

public interface PlatePort {
    
    // Eliminamos save, update y delete. 
    // La gestión se hace a través de ServiceDeliveryPort.save()
    
    // Mantenemos solo búsquedas para validaciones o consultas rápidas
    Plate findById(Long idPlate);
    
    // Útil para saber si una placa ya está registrada o buscar su historial
    Plate findByPlateNumber(String plateNumber);
    
    List<Plate> findAll();
}