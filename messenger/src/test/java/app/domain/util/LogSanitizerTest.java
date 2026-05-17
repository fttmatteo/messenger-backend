package app.domain.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias de LogSanitizer")
class LogSanitizerTest {

    @Test
    void testMaskDocument() {
        assertEquals("123***89", LogSanitizer.maskDocument("123456789"));
        assertEquals("****", LogSanitizer.maskDocument("123"));
        assertEquals("null", LogSanitizer.maskDocument((String) null));
        assertEquals("null", LogSanitizer.maskDocument(""));
    }

    @Test
    @DisplayName("Debe enmascarar correo electrónico")
    void testMaskEmail() {
        assertEquals("v***@example.com", LogSanitizer.maskEmail("valen@example.com"));
        assertEquals("***@***", LogSanitizer.maskEmail("a@b.com"));
        assertEquals("null", LogSanitizer.maskEmail(null));
    }

    @Test
    @DisplayName("Debe enmascarar token")
    void testMaskToken() {
        assertEquals("abcde...vwxyz", LogSanitizer.maskToken("abcdefghijklmnopqrstuvwxyz"));
        assertEquals("********", LogSanitizer.maskToken("short"));
        assertEquals("null", LogSanitizer.maskToken(null));
    }

    @Test
    @DisplayName("Debe enmascarar pin")
    void testMaskPin() {
        assertEquals("****", LogSanitizer.maskPin("1234"));
        assertEquals("null", LogSanitizer.maskPin(null));
    }

    @Test
    @DisplayName("Debe enmascarar campo genérico")
    void testMaskGeneric() {
        assertEquals("123***789", LogSanitizer.maskGeneric("123456789", 3));
        assertEquals("****", LogSanitizer.maskGeneric("12345", 3));
        assertEquals("null", LogSanitizer.maskGeneric(null, 3));
    }
}
