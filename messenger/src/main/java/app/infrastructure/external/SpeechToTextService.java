package app.infrastructure.external;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;

/**
 * Servicio para transcribir audio a texto usando Google Cloud Speech-to-Text
 * API.
 * Optimizado para velocidad en frases cortas.
 */
@Service
public class SpeechToTextService {

    private static final Logger logger = LoggerFactory.getLogger(SpeechToTextService.class);
    private static final int MAX_AUDIO_SIZE_BYTES = 10 * 1024 * 1024;
    private static final int MIN_AUDIO_SIZE_BYTES = 1000;

    private SpeechClient speechClient;

    @PostConstruct
    public void init() {
        try {
            this.speechClient = SpeechClient.create();
        } catch (IOException e) {
            logger.error("Error al inicializar SpeechClient: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        if (speechClient != null) {
            speechClient.close();
        }
    }

    public String transcribe(byte[] audioBytes, String languageCode) throws IOException {
        if (audioBytes == null || audioBytes.length == 0) {
            throw new IllegalArgumentException("El audio no puede estar vacío");
        }
        if (audioBytes.length < MIN_AUDIO_SIZE_BYTES) {
            throw new IllegalArgumentException("El audio es demasiado corto para transcribir");
        }
        if (audioBytes.length > MAX_AUDIO_SIZE_BYTES) {
            throw new IllegalArgumentException("El audio excede el tamaño máximo permitido (10MB)");
        }
        if (languageCode == null || languageCode.isBlank()) {
            languageCode = "es-CO";
        }

        if (speechClient == null) {
            try {
                this.speechClient = SpeechClient.create();
            } catch (IOException e) {
                throw new IOException("No se pudo conectar al servicio de transcripción", e);
            }
        }

        try {
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(ByteString.copyFrom(audioBytes))
                    .build();

            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.WEBM_OPUS)
                    .setSampleRateHertz(48000)
                    .setLanguageCode(languageCode)
                    .setModel("command_and_search")
                    .setEnableAutomaticPunctuation(false)
                    .build();

            RecognizeResponse response = speechClient.recognize(config, audio);

            StringBuilder transcript = new StringBuilder();
            for (SpeechRecognitionResult result : response.getResultsList()) {
                if (!result.getAlternativesList().isEmpty()) {
                    transcript.append(result.getAlternativesList().get(0).getTranscript());
                }
            }

            return transcript.toString().trim();

        } catch (ApiException e) {
            logger.error("Error Speech API: {}", e.getMessage());
            throw new IOException("Error del servicio de transcripción", e);
        } catch (Exception e) {
            logger.error("Error transcripción: {}", e.getMessage());
            throw new IOException("Error al transcribir audio", e);
        }
    }

    public String transcribe(byte[] audioBytes) throws IOException {
        return transcribe(audioBytes, "es-CO");
    }
}
