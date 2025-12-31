package app.infrastructure.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import app.domain.exception.UnauthorizedException;
import app.domain.model.Employee;
import app.domain.ports.EmployeePort;

/**
 * Helper para obtener información del usuario autenticado desde el contexto de
 * seguridad.
 * Centraliza la lógica de extracción del usuario actual para todos los
 * controllers.
 */
@Component
public class SecurityHelper {

    @Autowired
    private EmployeePort employeePort;

    /**
     * Obtiene el empleado actualmente autenticado.
     */
    public Employee getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("No hay sesión de usuario activa.");
        }

        String documentStr = auth.getName();

        if (documentStr == null || documentStr.isEmpty() || "anonymousUser".equals(documentStr)) {
            throw new UnauthorizedException("Autenticación de usuario no encontrada.");
        }

        Long document;
        try {
            document = Long.parseLong(documentStr);
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("Formato de documento de usuario inválido.");
        }

        Employee currentUser = employeePort.findByDocument(document);

        if (currentUser == null) {
            throw new UnauthorizedException("Usuario autenticado no encontrado en el sistema.");
        }

        return currentUser;
    }

    /**
     * Obtiene el ID del empleado actualmente autenticado.
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getIdEmployee();
    }

    /**
     * Verifica si el usuario actual tiene rol de ADMIN.
     */
    public boolean isCurrentUserAdmin() {
        Employee user = getCurrentUser();
        return user.getRole() == app.domain.model.enums.Role.ADMIN;
    }

    /**
     * Verifica si el usuario actual tiene rol de MESSENGER.
     */
    public boolean isCurrentUserMessenger() {
        Employee user = getCurrentUser();
        return user.getRole() == app.domain.model.enums.Role.MESSENGER;
    }
}
