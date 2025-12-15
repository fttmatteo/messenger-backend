package app.application.usecase.location;

import app.domain.model.Dealership;
import app.domain.model.Location;
import app.domain.ports.DealershipPort;
import app.domain.ports.LocationPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para geocodificar la dirección de un concesionario.
 * 
 * Utiliza la API de Google Geocoding a través del puerto de localización para
 * convertir la dirección física de un concesionario en coordenadas geográficas
 * (latitud y longitud), permitiendo su visualización en mapas y cálculos de
 * ruta.
 */
@Service
public class GeocodeDealership {

    @Autowired
    private LocationPort locationPort;
    @Autowired
    private DealershipPort dealershipPort;

    /**
     * Ejecuta el proceso de geocodificación para un concesionario específico.
     * 
     * Recupera el concesionario, obtiene las coordenadas de su dirección,
     * actualiza la entidad con la nueva ubicación y marca el indicador de
     * geolocalización.
     * Finalmente, persiste los cambios en la base de datos.
     * 
     * @param dealershipId ID del concesionario a procesar.
     * @return El objeto Dealership actualizado con las nuevas coordenadas.
     */
    public Dealership execute(Long dealershipId) {
        // Obtener el concesionario
        Dealership dealership = dealershipPort.findById(dealershipId);

        // Geocodificar la dirección
        Location location = locationPort.geocodeAddress(dealership.getAddress());

        // Actualizar el concesionario con las coordenadas
        dealership.setLatitude(location.getLatitude());
        dealership.setLongitude(location.getLongitude());
        dealership.setIsGeolocated(true);

        // Guardar cambios
        dealershipPort.save(dealership);

        // Retornar el concesionario actualizado
        return dealership;
    }

    /**
     * Geocodifica una dirección arbitraria sin persistir resultados.
     * 
     * Útil para validar direcciones antes de guardarlas o para mostrar
     * vistas previas de ubicación en la interfaz de usuario.
     * 
     * @param address La dirección en texto plano a geocodificar.
     * @return Un objeto Location con las coordenadas obtenidas.
     */
    public Location geocodeAddress(String address) {
        return locationPort.geocodeAddress(address);
    }
}
