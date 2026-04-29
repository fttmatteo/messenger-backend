package app.domain.util;

/**
 * Utilidad para enmascarar datos sensibles en logs.
 * Ubicada en el dominio para ser accesible por servicios de dominio sin violar
 * la arquitectura hexagonal.
 */
public final class LogSanitizer {

    private LogSanitizer() {
    }

    public static String maskDocument(Long document) {
        if (document == null) {
            return "null";
        }
        return maskDocument(document.toString());
    }

    public static String maskDocument(String document) {
        if (document == null || document.isBlank()) {
            return "null";
        }
        String str = document.trim();
        if (str.length() <= 4) {
            return "****";
        }
        return str.substring(0, 3) + "***" + str.substring(str.length() - 2);
    }

    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "null";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***@***";
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    public static String maskToken(String token) {
        if (token == null || token.isBlank()) {
            return "null";
        }
        if (token.length() <= 10) {
            return "********";
        }
        return token.substring(0, 5) + "..." + token.substring(token.length() - 5);
    }

    public static String maskPin(String pin) {
        if (pin == null || pin.isBlank()) {
            return "null";
        }
        return "****";
    }
    
    public static String maskPlate(String plate) {
        if (plate == null || plate.isBlank()) {
            return "null";
        }
        return maskGeneric(plate, 2);
    }

    public static String maskGeneric(String value, int visibleChars) {
        if (value == null || value.isBlank()) {
            return "null";
        }
        if (value.length() <= visibleChars * 2) {
            return "****";
        }
        return value.substring(0, visibleChars) + "***" + value.substring(value.length() - visibleChars);
    }
}
