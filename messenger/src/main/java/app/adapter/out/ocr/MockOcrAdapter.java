package app.adapter.out.ocr;

import app.domain.ports.OcrPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Adaptador mock de OCR para desarrollo local y tests.
 * 
 * Este adaptador simula el reconocimiento de placas sin llamar
 * a la API de Google Vision. Útil para:
 * - Desarrollo local sin credenciales de Google
 * - Tests automatizados
 * - Debugging
 * 
 * Se activa cuando app.ocr.mode=mock
 * 
 * @see app.domain.ports.OcrPort
 */
@Component
@ConditionalOnProperty(name = "app.ocr.mode", havingValue = "mock", matchIfMissing = true)
public class MockOcrAdapter implements OcrPort {

    private static final Logger logger = LoggerFactory.getLogger(MockOcrAdapter.class);

    /**
     * Simula la extracción de texto de una imagen.
     * 
     * Retorna una placa de prueba fija para permitir que los flujos
     * funcionen sin depender del servicio externo.
     * 
     * @param imageFile Archivo de imagen (ignorado en mock)
     * @return Placa de prueba: "ABC123"
     */
    @Override
    public String extractText(File imageFile) {
        logger.debug("MockOcrAdapter: Extrayendo texto de imagen (simulado)");
        logger.debug("Archivo: {}", imageFile.getName());

        // Retorna una placa de prueba
        String mockPlate = "ABC123";
        logger.info("MockOcrAdapter: Retornando placa mock: {}", mockPlate);

        return mockPlate;
    }
}
