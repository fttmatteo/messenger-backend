package app.adapter.in.rest.response;

/**
 * Respuesta de transcripción de audio a texto.
 */
public record TranscriptionResponse(
        String transcript,
        String language,
        boolean success) {
    public static TranscriptionResponse success(String transcript, String language) {
        return new TranscriptionResponse(transcript, language, true);
    }

    public static TranscriptionResponse error(String message) {
        return new TranscriptionResponse(message, null, false);
    }
}
