package app.application.exceptions;

/**
 * Excepción para errores de validación de datos de entrada.
 * Se lanza cuando los datos recibidos no cumplen con el formato o requisitos
 * esperados.
 */
public class InputsException extends Exception {
	public InputsException(String message) {
		super(message);
	}

}