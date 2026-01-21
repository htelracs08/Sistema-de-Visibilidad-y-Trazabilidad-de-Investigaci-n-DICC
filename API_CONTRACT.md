# API_CONTRACT — Sistema DICC (Backend REST)

Base URL (local):
- http://localhost:8080

Autenticación:
- HTTP Basic Auth
- Username: correo
- Password: password (texto plano por ahora)

Roles:
- JEFATURA
- DIRECTOR
- AYUDANTE

Convención de respuestas:
- Éxito: `{ "ok": true, ... }`
- Error: `{ "ok": false, "msg": "..." , ... }`

Estados relevantes:
- contrato.estado: ACTIVO | INACTIVO
- bitacora_mensual.estado: BORRADOR | ENVIADA | APROBADA | RECHAZADA

---

## Endpoints comunes (todos los roles)

### GET /api/v1/health
Público. Health check simple.

### GET /api/v1/me
Devuelve info del usuario autenticado (mínimo para que el frontend valide login).

Ejemplo:
```json
{ "ok": true, "correo": "juan.perez@epn.edu.ec", "rol": "AYUDANTE" }
```

### POST /api/v1/auth/cambiar-password
Cambia password del usuario logueado.

Body:
```json
{ "nuevaPassword": "Director2026*" }
```

Response:
```json
{ "ok": true }
```

---

## JEFATURA

### GET /api/v1/jefatura/profesores
Lista profesores cargados.

Response:
```json
[
  { "id":"...", "nombres":"...", "apellidos":"...", "correo":"..." }
]
```

### POST /api/v1/jefatura/proyectos
Crea proyecto (y crea usuario DIRECTOR si no existe, con password temporal).

Body:
```json
{
  "codigo": "PRJ-001",
  "nombre": "Proyecto IA",
  "correoDirector": "ariel.guana@epn.edu.ec"
}
```

Response:
```json
{ "ok": true, "proyectoId": "UUID" }
```

### GET /api/v1/jefatura/proyectos/resumen
Resumen por proyecto (dashboard de jefatura).

Incluye por proyecto:
- codigo
- nombre
- correoDirector
- activo
- tipoProyecto
- subtipoProyecto
- ayudantesActivos
- contratosTotal

Response:
```json
[
  {
    "proyectoId":"...",
    "codigo":"PRJ-001",
    "nombre":"Proyecto IA",
    "correoDirector":"ariel.guana@epn.edu.ec",
    "activo":true,
    "tipoProyecto":"INVESTIGACION",
    "subtipoProyecto":"INTERNO",
    "ayudantesActivos":1,
    "contratosTotal":2
  }
]
```

### GET /api/v1/jefatura/proyectos/{proyectoId}/ayudantes
Detalle por proyecto: contratos + datos ayudante (histórico).

Response:
```json
[
  {
    "contratoId":"...",
    "proyectoId":"...",
    "fechaInicio":"2026-01-01",
    "fechaFin":"2026-03-31",
    "estado":"ACTIVO",
    "motivoInactivo":null,
    "ayudanteId":"...",
    "nombres":"Juan",
    "apellidos":"Perez",
    "correoInstitucional":"juan.perez@epn.edu.ec",
    "facultad":"FIS",
    "quintil":2,
    "tipoAyudante":"AYUDANTE_INVESTIGACION"
  }
]
```

### GET /api/v1/jefatura/ayudantes/activos
Total global de ayudantes activos.

Response:
```json
{ "ok": true, "activos": 5 }
```

### GET /api/v1/jefatura/proyectos/estadisticas
Estadísticas de proyectos por (tipo, activo).

Response:
```json
[
  { "tipo":"INVESTIGACION", "activo":true, "total":3 },
  { "tipo":"INVESTIGACION", "activo":false, "total":1 }
]
```

### GET /api/v1/jefatura/ayudantes/estadisticas
Activos total + activos por tipoAyudante.

Response:
```json
{
  "ok": true,
  "totalActivos": 5,
  "porTipo": [
    { "tipoAyudante":"AYUDANTE_INVESTIGACION", "activos":3 },
    { "tipoAyudante":"AYUDANTE_DOCENCIA", "activos":2 }
  ]
}
```

### GET /api/v1/jefatura/semaforo
Semáforo de cumplimiento (una bitácora cuenta como “cumplida” solo si está APROBADA).

Response (ejemplo):
```json
{
  "ok": true,
  "items": [
    { "contratoId":"...", "estadoSemaforo":"VERDE", "aprobadasUltimos3Meses":3 }
  ]
}
```

---

## DIRECTOR

### PUT /api/v1/director/proyectos/{proyectoId}
Completa detalles del proyecto (tipo/subtipo, fechas, máximos).

Body:
```json
{
  "fechaInicio":"2026-01-01",
  "fechaFin":"2026-12-31",
  "tipo":"INVESTIGACION",
  "subtipo":"INTERNO",
  "maxAyudantes":2,
  "maxArticulos":3
}
```

Response:
```json
{ "ok": true }
```

### GET /api/v1/director/proyectos/{proyectoId}/ayudantes
Lista contratos + ayudantes del proyecto (histórico + estado).

### POST /api/v1/director/proyectos/{proyectoId}/ayudantes
Registra ayudante + crea contrato ACTIVO.
- Si el usuario AYUDANTE no existe, lo crea con password temporal.

Body:
```json
{
  "nombres":"Juan",
  "apellidos":"Perez",
  "correoInstitucional":"juan.perez@epn.edu.ec",
  "facultad":"FIS",
  "quintil":2,
  "tipoAyudante":"AYUDANTE_INVESTIGACION",
  "fechaInicioContrato":"2026-01-01",
  "fechaFinContrato":"2026-03-31"
}
```

Response:
```json
{ "ok": true, "ayudanteId":"...", "contratoId":"..." }
```

### POST /api/v1/director/contratos/{contratoId}/finalizar
Finaliza contrato (INACTIVO + motivo).

Body:
```json
{ "motivo":"RENUNCIA" }
```

Response:
```json
{ "ok": true }
```

### GET /api/v1/director/proyectos/{proyectoId}/bitacoras/pendientes
Lista bitácoras ENVIADAS del proyecto (solo si el proyecto pertenece al director).

Response:
```json
[
  { "bitacoraId":"...", "contratoId":"...", "anio":2026, "mes":1, "estado":"ENVIADA", "correoInstitucional":"..." }
]
```

### GET /api/v1/director/bitacoras/{bitacoraId}
Ver bitácora completa (cabecera + semanas + actividades) solo si pertenece a proyecto del director.

Response:
```json
{ "ok": true, "bitacora": { ... }, "semanas": [ ... ] }
```

### POST /api/v1/director/bitacoras/{bitacoraId}/revisar
Revisión de bitácora:
- APROBAR → estado APROBADA
- RECHAZAR → estado BORRADOR (reabre para corrección)
- Solo permitido si estadoActual == ENVIADA

Body:
```json
{ "decision":"APROBAR", "observacion":"OK" }
```

Response:
```json
{ "ok": true, "nuevoEstado":"APROBADA" }
```

---

## AYUDANTE

### POST /api/v1/ayudante/bitacoras/actual
Obtiene o crea la bitácora mensual actual del contrato ACTIVO del ayudante.

Response:
```json
{ "ok": true, "bitacoraId":"..." }
```

### POST /api/v1/ayudante/bitacoras/{bitacoraId}/semanas
Crea semana dentro de la bitácora (solo si BORRADOR y pertenece a su contrato activo).

Body:
```json
{
  "fechaInicioSemana":"2026-01-05",
  "fechaFinSemana":"2026-01-09",
  "actividadesRealizadas":"Avance del módulo",
  "observaciones":"Sin novedades",
  "anexos":"-"
}
```

Response:
```json
{ "ok": true, "semanaId":"..." }
```

### POST /api/v1/ayudante/semanas/{semanaId}/actividades
Crea actividad en una semana (solo si BORRADOR y pertenece a su contrato activo).

Body:
```json
{ "horaInicio":"08:00", "horaSalida":"10:00", "descripcion":"Reunión con director" }
```

Response:
```json
{ "ok": true, "actividadId":"...", "totalHoras": 2.0 }
```

### GET /api/v1/ayudante/bitacoras/{bitacoraId}
Ver bitácora completa (solo si pertenece a su contrato activo).

### GET /api/v1/ayudante/bitacoras/{bitacoraId}/semanas
Listar semanas de una bitácora (solo autorizado).

### GET /api/v1/ayudante/semanas/{semanaId}/actividades
Listar actividades de una semana (solo autorizado).

### POST /api/v1/ayudante/bitacoras/{bitacoraId}/enviar
Envía bitácora (BORRADOR -> ENVIADA) solo si:
- tiene >= 1 semana
- tiene >= 1 actividad

Response:
```json
{ "ok": true }
```

---

## DEBUG (solo desarrollo)

### POST /debug/bitacora/aprobar
Inserta una bitácora mensual APROBADA (para pruebas del semáforo).

Body:
```json
{ "contratoId":"...", "anio":2026, "mes":1 }
```

Response:
```json
{ "ok": true }
```
