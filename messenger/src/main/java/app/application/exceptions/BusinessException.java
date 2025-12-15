package app.application.exceptions;

/**
 * Excepción personalizada para errores de lógica de negocio.
 * 
 * Se lanza cuando una operación viola reglas de negocio como:
 * Intentar eliminar un registro con dependencias activas
 * Validaciones de unicidad
 * Intentar realizar transiciones de estado inválidas
 */
public class BusinessException extends Exception {
	/**
	 * Construye una nueva excepción con el mensaje de error especificado.
	 * 
	 * @param message El mensaje detallado del error de negocio.
	 */
	public BusinessException(String message) {
		super(message);
	}
}