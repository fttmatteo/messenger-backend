package app.domain.ports;

/**
 * Puerto de salida para envío de mensajes de WhatsApp.
 * Desacopla el dominio de la implementación específica de WhatsApp Cloud API.
 */
public interface WhatsAppMessagePort {
        boolean sendLocation(String to, double latitude, double longitude, String name, String address);

        boolean sendTextMessage(String to, String message);

        boolean sendReplyButtons(String to, String bodyText, java.util.List<String> buttonTitles,
                        java.util.List<String> buttonIds);

        boolean sendListMessage(String to, String bodyText, String buttonText, String listTitle,
                        java.util.List<String> rowTitles, java.util.List<String> rowDescriptions,
                        java.util.List<String> rowIds);

        boolean sendImage(String to, String imageUrl, String caption);
}
