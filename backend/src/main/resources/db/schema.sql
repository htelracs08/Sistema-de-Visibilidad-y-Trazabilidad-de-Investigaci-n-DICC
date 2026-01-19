PRAGMA foreign_keys = ON;

-- Tabla usuario
CREATE TABLE IF NOT EXISTS usuario (
  id TEXT PRIMARY KEY,
  nombres TEXT NOT NULL,
  apellidos TEXT NOT NULL,
  correo TEXT NOT NULL UNIQUE,
  password TEXT NOT NULL,
  rol TEXT NOT NULL,
  debe_cambiar_password INTEGER NOT NULL DEFAULT 1,
  creado_en TEXT NOT NULL DEFAULT (datetime('now'))
);

-- Tabla profesor
CREATE TABLE IF NOT EXISTS profesor (
  id TEXT PRIMARY KEY,
  nombres TEXT NOT NULL,
  apellidos TEXT NOT NULL,
  correo TEXT NOT NULL UNIQUE
);

-- Tabla proyecto (CON TODAS LAS COLUMNAS)
CREATE TABLE IF NOT EXISTS proyecto (
  id TEXT PRIMARY KEY,
  codigo TEXT NOT NULL UNIQUE,
  nombre TEXT NOT NULL,
  director_correo TEXT NOT NULL,
  activo INTEGER NOT NULL DEFAULT 1,
  max_ayudantes INTEGER NOT NULL DEFAULT 0,
  max_articulos INTEGER NOT NULL DEFAULT 0,
  fecha_inicio TEXT,
  fecha_fin TEXT,
  tipo TEXT,
  subtipo TEXT,
  creado_en TEXT NOT NULL DEFAULT (datetime('now'))
);

-- Tabla ayudante
CREATE TABLE IF NOT EXISTS ayudante (
  id TEXT PRIMARY KEY,
  nombres TEXT NOT NULL,
  apellidos TEXT NOT NULL,
  correo_institucional TEXT NOT NULL UNIQUE,
  facultad TEXT NOT NULL,
  quintil INTEGER NOT NULL,
  tipo_ayudante TEXT NOT NULL,
  creado_en TEXT NOT NULL DEFAULT (datetime('now'))
);

-- Tabla contrato
CREATE TABLE IF NOT EXISTS contrato (
  id TEXT PRIMARY KEY,
  proyecto_id TEXT NOT NULL,
  ayudante_id TEXT NOT NULL,
  fecha_inicio TEXT NOT NULL,
  fecha_fin TEXT NOT NULL,
  estado TEXT NOT NULL, -- ACTIVO | INACTIVO
  motivo_inactivo TEXT, -- RENUNCIA | FIN_CONTRATO | DESPIDO
  creado_en TEXT NOT NULL DEFAULT (datetime('now')),
  FOREIGN KEY (proyecto_id) REFERENCES proyecto(id),
  FOREIGN KEY (ayudante_id) REFERENCES ayudante(id)
);

-- Índices útiles
CREATE INDEX IF NOT EXISTS idx_contrato_proyecto ON contrato(proyecto_id);
CREATE INDEX IF NOT EXISTS idx_contrato_ayudante ON contrato(ayudante_id);
CREATE INDEX IF NOT EXISTS idx_contrato_estado ON contrato(estado);

-- Tabla bitácora mensual (mínima para semáforo)
CREATE TABLE IF NOT EXISTS bitacora_mensual (
  id TEXT PRIMARY KEY,
  contrato_id TEXT NOT NULL,
  anio INTEGER NOT NULL,
  mes INTEGER NOT NULL, -- 1..12
  estado TEXT NOT NULL, -- BORRADOR | ENVIADA | APROBADA | RECHAZADA
  creado_en TEXT NOT NULL DEFAULT (datetime('now')),
  actualizado_en TEXT NOT NULL DEFAULT (datetime('now')),
  UNIQUE (contrato_id, anio, mes),
  FOREIGN KEY (contrato_id) REFERENCES contrato(id)
);

CREATE INDEX IF NOT EXISTS idx_bitacora_contrato ON bitacora_mensual(contrato_id);
CREATE INDEX IF NOT EXISTS idx_bitacora_anio_mes ON bitacora_mensual(anio, mes);
CREATE INDEX IF NOT EXISTS idx_bitacora_estado ON bitacora_mensual(estado);
