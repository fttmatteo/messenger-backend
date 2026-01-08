package app.infrastructure.logging;

/**
 * Utilidad para enmascarar datos sensibles en logs.
 */
public final class LogSanitizer {

    private LogSanitizer() {
        // Utility class
    }

    public static String maskDocument(Long document) {
        if (document == null) {
            return "null";
        }
        return maskDocument(document.toString());
    }

    public static String maskDocument(String document) {
        if (document == null) {
            return "null";
        }
        String str = document;
        if (str.length() <= 4) {
            return "****";
        }
        return str.substring(0, 3) + "***" + str.substring(str.length() - 2);
    }

    public static String maskEmail(String email) {
        if (email == null) {
            return "null";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***@***";
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}
