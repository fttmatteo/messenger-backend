package app.application.exceptions;

/**
 * Excepción personalizada para errores de lógica de negocio.
 * 
 * <p>
 * Se lanza cuando una operación viola reglas de negocio como: 
 * </p>
 * <ul>
 * <li>Intentar eliminar un registro con dependencias activas</li>
 * <li>Validaciones de unicidad</li>
 * <li>Intentar realizar transiciones de estado inválidas</li>
 * </ul>
 */
public class BusinessException extends Exception {
	public BusinessException(String message) {
		super(message);
	}
}