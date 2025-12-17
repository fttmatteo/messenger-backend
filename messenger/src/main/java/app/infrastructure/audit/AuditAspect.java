package app.infrastructure.audit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Aspecto AOP para auditar acciones administrativas.
 * 
 * Intercepta métodos anotados con @AuditableAction y genera logs de auditoría
 * con información completa sobre la operación realizada.
 * 
 * Información registrada:
 * - Timestamp de la acción
 * - Usuario que ejecutó la acción
 * - Tipo de acción (CREATE, UPDATE, DELETE, etc.)
 * - Método invocado
 * - Parámetros de entrada
 * - Resultado (SUCCESS/FAILURE)
 * - Tiempo de ejecución
 * - Mensaje de error (si aplica)
 * 
 * Los logs se escriben con nivel WARN para garantizar visibilidad en producción
 * y facilitar su recolección por herramientas de monitoreo (ELK, Splunk, etc.).
 * 
 * Formato de log:
 * AUDIT | timestamp | user | action | method | params | result | duration_ms |
 * error
 * 
 * @see AuditableAction
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Intercepta y audita métodos anotados con @AuditableAction.
     */
    @Around("@annotation(auditableAction)")
    public Object auditAction(ProceedingJoinPoint joinPoint, AuditableAction auditableAction) throws Throwable {
        long startTime = System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String user = getCurrentUser();
        String action = auditableAction.action();
        String method = getMethodName(joinPoint);
        String params = getParameters(joinPoint);

        Object result = null;
        String status = "SUCCESS";
        String errorMessage = "";

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            status = "FAILURE";
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Formato estructurado para fácil parsing
            auditLogger.warn("AUDIT | {} | {} | {} | {} | {} | {} | {}ms | {}",
                    timestamp,
                    user,
                    action,
                    method,
                    params,
                    status,
                    duration,
                    errorMessage);
        }
    }

    /**
     * Obtiene el username del usuario actual desde el contexto de seguridad.
     */
    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "ANONYMOUS";
    }

    /**
     * Obtiene el nombre completo del método (clase.método).
     */
    private String getMethodName(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
    }

    /**
     * Obtiene los parámetros del método de forma segura.
     * Limita la longitud para evitar logs excesivamente largos.
     */
    private String getParameters(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return "[]";
        }

        String params = Arrays.toString(args);
        // Limitar longitud para evitar logs muy largos
        if (params.length() > 200) {
            params = params.substring(0, 200) + "...";
        }
        return params;
    }
}
