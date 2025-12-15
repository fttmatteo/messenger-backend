package app.application.exceptions;

/**
 * Excepción para errores de validación de datos de entrada.
 * Se lanza cuando los datos recibidos no cumplen con el formato o requisitos
 * esperados.
 */
public class InputsException extends Exception {
	/**
	 * Construye una nueva excepción con el mensaje de validación especificado.
	 * 
	 * @param message El mensaje detallado del error de validación.
	 */
	public InputsException(String message) {
		super(message);
	}

}