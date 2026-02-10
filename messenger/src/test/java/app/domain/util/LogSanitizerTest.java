package app.domain.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LogSanitizerTest {

    @Test
    void testMaskDocument() {
        assertEquals("123***89", LogSanitizer.maskDocument("123456789"));
        assertEquals("****", LogSanitizer.maskDocument("123"));
        assertEquals("null", LogSanitizer.maskDocument((String) null));
        assertEquals("null", LogSanitizer.maskDocument(""));
    }

    @Test
    void testMaskEmail() {
        assertEquals("v***@example.com", LogSanitizer.maskEmail("valen@example.com"));
        assertEquals("***@***", LogSanitizer.maskEmail("a@b.com"));
        assertEquals("null", LogSanitizer.maskEmail(null));
    }

    @Test
    void testMaskToken() {
        assertEquals("abcde...vwxyz", LogSanitizer.maskToken("abcdefghijklmnopqrstuvwxyz"));
        assertEquals("********", LogSanitizer.maskToken("short"));
        assertEquals("null", LogSanitizer.maskToken(null));
    }

    @Test
    void testMaskPin() {
        assertEquals("****", LogSanitizer.maskPin("1234"));
        assertEquals("null", LogSanitizer.maskPin(null));
    }

    @Test
    void testMaskGeneric() {
        assertEquals("123***789", LogSanitizer.maskGeneric("123456789", 3));
        assertEquals("****", LogSanitizer.maskGeneric("12345", 3));
        assertEquals("null", LogSanitizer.maskGeneric(null, 3));
    }
}
