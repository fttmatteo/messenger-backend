package app.adapter.in.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import app.application.exceptions.BusinessException;
import app.domain.model.Employee;

@Component
public class AuthValidator {

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void validateLogin(Employee employee, String inputPassword) throws BusinessException {
        // 1. Validar que el usuario exista
        if (employee == null) {
            throw new BusinessException("Usuario no encontrado o credenciales incorrectas.");
        }

        // 2. Validar contraseña
        if (!passwordEncoder.matches(inputPassword, employee.getPassword())) {
            throw new BusinessException("Contraseña incorrecta.");
        }
        
        // Aquí podrías agregar más validaciones, como si el usuario está activo/inactivo
    }
}