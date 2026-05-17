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
    private static final int MIN_AUDIO_SIZE_BYTES = 500; // Reducido para permitir palabras cortas

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
        return transcribe(audioBytes, languageCode, "audio/webm");
    }

    public String transcribe(byte[] audioBytes, String languageCode, String mimeType) throws IOException {
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

            RecognitionConfig.AudioEncoding encoding = RecognitionConfig.AudioEncoding.WEBM_OPUS;
            int sampleRate = 48000;

            if (mimeType != null) {
                if (mimeType.contains("mp4") || mimeType.contains("aac") || mimeType.contains("m4a")) {
                    encoding = RecognitionConfig.AudioEncoding.ENCODING_UNSPECIFIED;
                    sampleRate = 0;
                } else if (mimeType.contains("ogg")) {
                    encoding = RecognitionConfig.AudioEncoding.OGG_OPUS;
                    sampleRate = 16000;
                }
            }

            RecognitionConfig.Builder configBuilder = RecognitionConfig.newBuilder()
                    .setEncoding(encoding)
                    .setLanguageCode(languageCode)
                    .setModel("command_and_search")
                    .setEnableAutomaticPunctuation(false);

            if (sampleRate > 0) {
                configBuilder.setSampleRateHertz(sampleRate);
            }

            RecognizeResponse response = speechClient.recognize(configBuilder.build(), audio);

            StringBuilder transcript = new StringBuilder();
            for (SpeechRecognitionResult result : response.getResultsList()) {
                if (!result.getAlternativesList().isEmpty()) {
                    transcript.append(result.getAlternativesList().get(0).getTranscript());
                }
            }

            return transcript.toString().trim();

        } catch (ApiException e) {
            logger.error("Error en Google Speech API. Status: {}, Reason: {}, Message: {}",
                    e.getStatusCode().getCode(), e.getReason(), e.getMessage());
            throw new IOException("Error del servicio de transcripción (GCP): " + e.getReason(), e);
        } catch (Exception e) {
            logger.error("Error inesperado durante la transcripción. ", e);
            throw new IOException("Error interno al procesar el audio: " + e.getMessage(), e);
        }
    }

    public String transcribe(byte[] audioBytes) throws IOException {
        return transcribe(audioBytes, "es-CO", "audio/webm");
    }
}
