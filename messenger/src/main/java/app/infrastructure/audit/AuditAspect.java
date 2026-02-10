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
 * Aspecto AOP para auditoría de acciones marcadas con @AuditableAction.
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Intercepta métodos anotados con @AuditableAction para registrar la auditoría.
     * Registra: usuario, acción, método, parámetros, resultado (Éxito/Fallo) y
     * duración.
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
     * Obtiene el nombre del usuario autenticado actual.
     */
    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "ANONYMOUS";
    }

    /**
     * Obtiene el nombre calificado del método interceptado (Clase.metodo).
     */
    private String getMethodName(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
    }

    private String getParameters(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return "[]";
        }

        String params = Arrays.toString(args);

        // Enmascarar información sensible
        params = params.replaceAll("(?i)(password[:=]\\s?['\"]?)([^,}\"\\s]+)(['\"]?)", "$1****$3");
        params = params.replaceAll("(?i)(pin[:=]\\s?['\"]?)([^,}\"\\s]+)(['\"]?)", "$1****$3");
        params = params.replaceAll("(?i)(token[:=]\\s?['\"]?)([^,}\"\\s]+)(['\"]?)", "$1****$3");

        if (params.length() > 500) {
            params = params.substring(0, 500) + "...";
        }
        return params;
    }
}
