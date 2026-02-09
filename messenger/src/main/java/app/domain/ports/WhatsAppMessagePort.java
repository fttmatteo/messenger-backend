package app.domain.ports;

/**
 * Puerto de salida para envío de mensajes de WhatsApp.
 * Desacopla el dominio de la implementación específica de WhatsApp Cloud API.
 */
public interface WhatsAppMessagePort {

    /**
     * Envía una ubicación geográfica nativa a un número de WhatsApp.
     */
    boolean sendLocation(String to, double latitude, double longitude, String name, String address);

    /**
     * Envía un mensaje de texto a un número de WhatsApp.
     */
    boolean sendTextMessage(String to, String message);

    /**
     * Envía un mensaje interactivo con botones de respuesta rápida.
     */
    boolean sendReplyButtons(String to, String bodyText, java.util.List<String> buttonTitles,
            java.util.List<String> buttonIds);

    /**
     * Envía un mensaje interactivo con una lista de opciones (menú desplegable).
     */
    boolean sendListMessage(String to, String bodyText, String buttonText, String listTitle,
            java.util.List<String> rowTitles, java.util.List<String> rowDescriptions, java.util.List<String> rowIds);
}
