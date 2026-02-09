package app.domain.ports;

import app.domain.model.Dealership;
import app.domain.model.WhatsAppSession;
import java.util.Optional;

/**
 * Puerto de salida para gestión de sesiones de WhatsApp.
 * Desacopla el dominio de la persistencia.
 */
public interface WhatsAppSessionPort {

    /**
     * Busca una sesión activa (no expirada) por número de teléfono.
     */
    Optional<WhatsAppSession> findActiveSession(String phoneNumber);

    /**
     * Crea una nueva sesión para un número de teléfono.
     */
    WhatsAppSession createSession(String phoneNumber, Dealership dealership, int expirationHours);

    /**
     * Elimina todas las sesiones de un número de teléfono.
     */
    void deleteByPhoneNumber(String phoneNumber);

    /**
     * Busca un concesionario por su PIN de WhatsApp.
     */
    Optional<Dealership> findDealershipByPin(String pin);

    /**
     * Obtiene las horas de expiración de sesión configuradas.
     */
    int getSessionExpirationHours();

    /**
     * Actualiza una sesión existente (ej. para guardar estado de paginación).
     */
    void updateSession(WhatsAppSession session);

    /**
     * Busca todas las sesiones activas asociadas a un concesionario.
     */
    java.util.List<WhatsAppSession> findActiveSessionsByDealership(Long dealershipId);
}
