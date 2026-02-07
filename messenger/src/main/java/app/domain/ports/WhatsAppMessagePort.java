package app.domain.ports;

/**
 * Puerto de salida para envío de mensajes de WhatsApp.
 * Desacopla el dominio de la implementación específica de WhatsApp Cloud API.
 */
public interface WhatsAppMessagePort {

    /**
     * Envía un mensaje de texto a un número de WhatsApp.
     * 
     * @param to      Número de teléfono destino
     * @param message Contenido del mensaje
     * @return true si el envío fue exitoso
     */
    boolean sendTextMessage(String to, String message);
}
