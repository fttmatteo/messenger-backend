package app.adapter.in.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.adapter.in.validators.EmployeeValidator;
import app.domain.model.Employee;

/**
 * Componente encargado de la construcción de objetos {@link Employee}.
 *
 * Aplica validaciones de reglas de negocio a través de
 * {@link EmployeeValidator}
 * antes de crear la instancia del modelo de dominio.
 */
@Component
public class EmployeeBuilder {

    @Autowired
    private EmployeeValidator validator;

    /**
     * Construye una instancia de Employee con los datos proporcionados.
     *
     * Valida cada campo (documento, nombre, teléfono, usuario, contraseña, rol)
     * antes de asignarlo al objeto.
     *
     * @param document Número de documento del empleado.
     * @param fullName Nombre completo del empleado.
     * @param phone    Número de teléfono de contacto.
     * @param userName Nombre de usuario para el sistema.
     * @param password Contraseña de acceso.
     * @param role     Rol asignado (ADMIN, MESSENGER).
     * @return Instancia de {@link Employee} validada.
     * @throws Exception Si alguna validación de campo falla.
     */
    public Employee build(String document, String fullName, String phone, String userName, String password, String role)
            throws Exception {
        Employee employee = new Employee();
        employee.setDocument(validator.documentValidator(document));
        employee.setFullName(validator.fullNameValidator(fullName));
        employee.setPhone(validator.phoneValidator(phone));
        employee.setUserName(validator.userNameValidator(userName));
        employee.setPassword(validator.passwordValidator(password));
        employee.setRole(validator.roleValidator(role));
        return employee;
    }
}