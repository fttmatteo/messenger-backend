package app.adapter.in.rest.controllers;

import app.adapter.in.rest.response.TranscriptionResponse;
import app.infrastructure.external.SpeechToTextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador REST para transcripción de audio a texto.
 * Usa Google Cloud Speech-to-Text API.
 */
@RestController
@RequestMapping("/api/transcribe")
@PreAuthorize("isAuthenticated()")
public class TranscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(TranscriptionController.class);
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final SpeechToTextService speechToTextService;

    public TranscriptionController(SpeechToTextService speechToTextService) {
        this.speechToTextService = speechToTextService;
    }

    /**
     * Transcribe un archivo de audio a texto.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TranscriptionResponse> transcribe(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam(value = "language", defaultValue = "es-CO") String language) {
        try {
            if (audio.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(TranscriptionResponse.error("Archivo de audio requerido"));
            }

            if (audio.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest()
                        .body(TranscriptionResponse.error("El archivo excede el tamaño máximo (10MB)"));
            }

            String transcript = speechToTextService.transcribe(audio.getBytes(), language, audio.getContentType());

            if (transcript.isEmpty()) {
                return ResponseEntity.ok(TranscriptionResponse.error("No se detectó voz en el audio"));
            }

            return ResponseEntity.ok(TranscriptionResponse.success(transcript, language));

        } catch (Exception e) {
            logger.error("Error en transcripción: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(TranscriptionResponse.error("Error al procesar el audio: " + e.getMessage()));
        }
    }
}
