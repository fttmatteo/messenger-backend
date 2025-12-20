package app.application.exceptions;

/**
 * Excepción para errores de validación de entrada.
 */
public class InputsException extends RuntimeException {

	public InputsException(String message) {
		super(message);
	}

}