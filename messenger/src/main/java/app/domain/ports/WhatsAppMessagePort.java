package app.domain.ports;

/**
 * Puerto de salida para envío de mensajes de WhatsApp.
 * Desacopla el dominio de la implementación específica de WhatsApp Cloud API.
 */
public interface WhatsAppMessagePort {

    /**
     * Envía una ubicación geográfica nativa a un número de WhatsApp.
     * 
     * @param to        Número de teléfono destino
     * @param latitude  Latitud
     * @param longitude Longitud
     * @param name      Nombre del lugar (ej. "Ubicación de entrega")
     * @param address   Dirección del lugar
     * @return true si el envío fue exitoso
     */
    boolean sendLocation(String to, double latitude, double longitude, String name, String address);

    /**
     * Envía un mensaje de texto a un número de WhatsApp.
     * 
     * @param to      Número de teléfono destino
     * @param message Contenido del mensaje
     * @return true si el envío fue exitoso
     */
    boolean sendTextMessage(String to, String message);
}
