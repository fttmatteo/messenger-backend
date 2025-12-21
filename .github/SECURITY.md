# Security Policy / Política de Seguridad

**[🇺🇸 English](#security-policy)** | **[🇪🇸 Español](#política-de-seguridad)**

---

# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.4.0   | :white_check_mark: |
| < 1.0.0 | :x:                |

## Reporting a Vulnerability

We take security seriously. If you discover a security vulnerability, please follow responsible disclosure:

### Do NOT

- ❌ Open a public GitHub Issue
- ❌ Discuss the vulnerability publicly before it's fixed
- ❌ Exploit the vulnerability beyond what's necessary to demonstrate it

### Do

1. **Email us directly** at: **valenciaardila988@icloud.com**
2. Include in your report:
   - Description of the vulnerability
   - Steps to reproduce (Proof of Concept)
   - Potential impact assessment
   - Any suggested fixes (optional)

### What to Expect

| Timeline | Action |
|----------|--------|
| 24-48 hours | Initial response acknowledging receipt |
| 7 days | Preliminary assessment shared with you |
| 30-90 days | Fix developed, tested, and deployed |

We will keep you informed throughout the process and credit you in our release notes (unless you prefer to remain anonymous).

## Security Best Practices

This project implements:

- ✅ JWT-based authentication with secure token handling
- ✅ Role-based access control (ADMIN, COORDINATOR, MESSENGER)
- ✅ Input validation and sanitization
- ✅ Parameterized queries (no SQL injection)
- ✅ HTTPS enforcement in production
- ✅ Secrets managed via environment variables
- ✅ Dependency scanning via Dependabot

---

# Política de Seguridad

## Versiones Soportadas

| Versión | Soportada          |
| ------- | ------------------ |
| 1.4.0   | :white_check_mark: |
| < 1.0.0 | :x:                |

## Reportar una Vulnerabilidad

Tomamos la seguridad seriamente. Si descubres una vulnerabilidad, por favor sigue la divulgación responsable:

### NO hagas

- ❌ Abrir un Issue público en GitHub
- ❌ Discutir la vulnerabilidad públicamente antes de que sea corregida
- ❌ Explotar la vulnerabilidad más allá de lo necesario para demostrarla

### SÍ haz

1. **Envíanos un correo** a: **valenciaardila988@icloud.com**
2. Incluye en tu reporte:
   - Descripción de la vulnerabilidad
   - Pasos para reproducirla (Prueba de Concepto)
   - Evaluación del impacto potencial
   - Sugerencias de corrección (opcional)

### Qué Esperar

| Tiempo | Acción |
|--------|--------|
| 24-48 horas | Respuesta inicial confirmando recepción |
| 7 días | Evaluación preliminar compartida contigo |
| 30-90 días | Corrección desarrollada, probada y desplegada |

Te mantendremos informado durante el proceso y te daremos crédito en nuestras notas de versión (a menos que prefieras permanecer anónimo).

## Prácticas de Seguridad

Este proyecto implementa:

- ✅ Autenticación basada en JWT con manejo seguro de tokens
- ✅ Control de acceso basado en roles (ADMIN, COORDINATOR, MESSENGER)
- ✅ Validación y sanitización de entradas
- ✅ Consultas parametrizadas (sin inyección SQL)
- ✅ HTTPS obligatorio en producción
- ✅ Secretos gestionados via variables de entorno
- ✅ Escaneo de dependencias via Dependabot
