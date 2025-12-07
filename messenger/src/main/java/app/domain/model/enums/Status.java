package app.domain.model.enums;

public enum Status {
    ASSIGNED, // Automático al ingresar placa
    PENDING, // Requiere: Firma, Foto, Observación
    DELIVERED, // Requiere: Firma. Opcional: Foto, Observación
    FAILED, // Requiere: Firma, Foto, Observación
    RETURNED, // Requiere: Firma, Foto, Observación
    CANCELED, // Solo Admin
    OBSERVED, // Solo Admin
    RESOLVED // Ciclo final tras observación
}