-- ============================================
-- SISTEMA DE GESTIÓN DE PROYECTOS DICC
-- Base de Datos: SQLite
-- ============================================

-- ============================================
-- TABLA: LABORATORIOS
-- ============================================
CREATE TABLE IF NOT EXISTS laboratorio (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo_laboratorio VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    ubicacion VARCHAR(200),
    responsable VARCHAR(200),
    extension VARCHAR(20),
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    creado_por VARCHAR(100),
    modificado_por VARCHAR(100),
    activo BOOLEAN DEFAULT 1
);

-- ============================================
-- TABLA: USUARIOS (Superclase)
-- ============================================
CREATE TABLE IF NOT EXISTS usuario (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo_usuario VARCHAR(20) NOT NULL, -- 'DOCENTE', 'AYUDANTE', 'JEFATURA'
    codigo_epn VARCHAR(50) UNIQUE NOT NULL,
    cedula VARCHAR(10) UNIQUE NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    correo_institucional VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL, -- 'JEFATURA_DICC', 'DIRECTOR_PROYECTO', 'AYUDANTE_PROYECTO'
    email_verificado BOOLEAN DEFAULT 0,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    creado_por VARCHAR(100),
    modificado_por VARCHAR(100),
    activo BOOLEAN DEFAULT 1,
    
    -- Campos específicos DOCENTE
    departamento VARCHAR(100),
    cubiculo VARCHAR(50),
    extension VARCHAR(20),
    area_investigacion VARCHAR(200),
    
    -- Campos específicos AYUDANTE
    carrera VARCHAR(150),
    facultad VARCHAR(150),
    quintil INTEGER,
    semestre_actual INTEGER,
    promedio_general DECIMAL(4,2),
    
    -- Campos específicos JEFATURA
    cargo VARCHAR(100),
    codigo_registro_especial VARCHAR(100)
);

-- Índices para búsquedas rápidas
CREATE INDEX idx_usuario_correo ON usuario(correo_institucional);
CREATE INDEX idx_usuario_codigo ON usuario(codigo_epn);
CREATE INDEX idx_usuario_rol ON usuario(rol);
CREATE INDEX idx_usuario_tipo ON usuario(tipo_usuario);

-- ============================================
-- TABLA: PROYECTOS AUTORIZADOS
-- ============================================
CREATE TABLE IF NOT EXISTS proyecto_autorizado (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo_proyecto VARCHAR(50) UNIQUE NOT NULL,
    fecha_autorizacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    autorizado_por VARCHAR(100),
    utilizado BOOLEAN DEFAULT 0,
    fecha_utilizacion DATETIME,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    creado_por VARCHAR(100),
    modificado_por VARCHAR(100),
    activo BOOLEAN DEFAULT 1
);

CREATE INDEX idx_proyecto_autorizado_codigo ON proyecto_autorizado(codigo_proyecto);
CREATE INDEX idx_proyecto_autorizado_utilizado ON proyecto_autorizado(utilizado);

-- ============================================
-- TABLA: PROYECTOS
-- ============================================
CREATE TABLE IF NOT EXISTS proyecto (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo_proyecto VARCHAR(50) UNIQUE NOT NULL,
    titulo VARCHAR(300) NOT NULL,
    descripcion TEXT,
    objetivo_general TEXT,
    fecha_inicio_real DATE,
    fecha_fin_estimada DATE,
    fecha_fin_real DATE,
    duracion_semestres INTEGER NOT NULL,
    semestre_actual INTEGER DEFAULT 1,
    estado VARCHAR(30) NOT NULL, -- 'AUTORIZADO_PENDIENTE', 'ACTIVO', 'FINALIZADO', 'SUSPENDIDO', 'CANCELADO'
    tipo_proyecto VARCHAR(50) NOT NULL, -- 'INVESTIGACION', 'TRANSFERENCIA_TECNOLOGICA', 'VINCULACION'
    
    -- Foreign Keys
    director_id INTEGER NOT NULL,
    laboratorio_id INTEGER NOT NULL,
    
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    creado_por VARCHAR(100),
    modificado_por VARCHAR(100),
    activo BOOLEAN DEFAULT 1,
    
    FOREIGN KEY (director_id) REFERENCES usuario(id) ON DELETE RESTRICT,
    FOREIGN KEY (laboratorio_id) REFERENCES laboratorio(id) ON DELETE RESTRICT
);

CREATE INDEX idx_proyecto_codigo ON proyecto(codigo_proyecto);
CREATE INDEX idx_proyecto_director ON proyecto(director_id);
CREATE INDEX idx_proyecto_estado ON proyecto(estado);
CREATE INDEX idx_proyecto_tipo ON proyecto(tipo_proyecto);

-- ============================================
-- TABLA: CAPACIDAD AYUDANTES POR SEMESTRE
-- ============================================
CREATE TABLE IF NOT EXISTS proyecto_capacidad_semestre (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    proyecto_id INTEGER NOT NULL,
    numero_semestre INTEGER NOT NULL,
    numero_ayudantes INTEGER NOT NULL,
    numero_meses_por_ayudante INTEGER NOT NULL,
    
    FOREIGN KEY (proyecto_id) REFERENCES proyecto(id) ON DELETE CASCADE,
    UNIQUE(proyecto_id, numero_semestre)
);

CREATE INDEX idx_capacidad_proyecto ON proyecto_capacidad_semestre(proyecto_id);

-- ============================================
-- TABLA: CONTRATOS
-- ============================================
CREATE TABLE IF NOT EXISTS contrato (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numero_contrato VARCHAR(50) UNIQUE NOT NULL,
    fecha_solicitud DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_aprobacion DATETIME,
    fecha_rechazo DATETIME,
    fecha_inicio_contrato DATE,
    fecha_fin_contrato DATE,
    fecha_renuncia DATE,
    meses_pactados INTEGER,
    meses_trabajados INTEGER DEFAULT 0,
    horas_semanales_pactadas INTEGER,
    remuneracion_mensual DECIMAL(10,2),
    estado VARCHAR(40) NOT NULL, -- 'PENDIENTE_APROBACION_DIRECTOR', 'ACTIVO', 'FINALIZADO_NORMAL', 'FINALIZADO_RENUNCIA', 'RECHAZADO'
    motivo_rechazo TEXT,
    motivo_renuncia TEXT,
    semestre_asignado INTEGER,
    
    -- Foreign Keys
    proyecto_id INTEGER NOT NULL,
    ayudante_id INTEGER NOT NULL,
    aprobado_por_id INTEGER,
    
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    creado_por VARCHAR(100),
    modificado_por VARCHAR(100),
    activo BOOLEAN DEFAULT 1,
    
    FOREIGN KEY (proyecto_id) REFERENCES proyecto(id) ON DELETE RESTRICT,
    FOREIGN KEY (ayudante_id) REFERENCES usuario(id) ON DELETE RESTRICT,
    FOREIGN KEY (aprobado_por_id) REFERENCES usuario(id) ON DELETE SET NULL
);

CREATE INDEX idx_contrato_proyecto ON contrato(proyecto_id);
CREATE INDEX idx_contrato_ayudante ON contrato(ayudante_id);
CREATE INDEX idx_contrato_estado ON contrato(estado);
CREATE INDEX idx_contrato_semestre ON contrato(semestre_asignado);

-- ============================================
-- TABLA: BITÁCORAS MENSUALES
-- ============================================
CREATE TABLE IF NOT EXISTS bitacora_mensual (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo_bitacora VARCHAR(50) UNIQUE NOT NULL,
    mes INTEGER NOT NULL CHECK(mes >= 1 AND mes <= 12),
    anio INTEGER NOT NULL,
    fecha_envio DATETIME,
    fecha_revision DATETIME,
    estado VARCHAR(30) NOT NULL, -- 'BORRADOR', 'ENVIADA_REVISION', 'APROBADA', 'RECHAZADA', 'REQUIERE_MODIFICACION'
    horas_totales DECIMAL(6,2) DEFAULT 0,
    comentarios_ayudante TEXT,
    comentarios_director TEXT,
    
    -- Foreign Keys
    contrato_id INTEGER NOT NULL,
    revisada_por_id INTEGER,
    
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    creado_por VARCHAR(100),
    modificado_por VARCHAR(100),
    activo BOOLEAN DEFAULT 1,
    
    FOREIGN KEY (contrato_id) REFERENCES contrato(id) ON DELETE CASCADE,
    FOREIGN KEY (revisada_por_id) REFERENCES usuario(id) ON DELETE SET NULL,
    UNIQUE(contrato_id, mes, anio)
);

CREATE INDEX idx_bitacora_contrato ON bitacora_mensual(contrato_id);
CREATE INDEX idx_bitacora_estado ON bitacora_mensual(estado);
CREATE INDEX idx_bitacora_fecha ON bitacora_mensual(anio, mes);

-- ============================================
-- TABLA: ACTIVIDADES
-- ============================================
CREATE TABLE IF NOT EXISTS actividad (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numero_actividad INTEGER NOT NULL,
    descripcion TEXT NOT NULL,
    objetivo_actividad TEXT,
    resultado_obtenido TEXT,
    tiempo_dedicado_horas DECIMAL(5,2) NOT NULL,
    fecha_ejecucion DATE NOT NULL,
    evidencia_url VARCHAR(500),
    categoria VARCHAR(100),
    
    -- Foreign Key
    bitacora_id INTEGER NOT NULL,
    
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    creado_por VARCHAR(100),
    modificado_por VARCHAR(100),
    activo BOOLEAN DEFAULT 1,
    
    FOREIGN KEY (bitacora_id) REFERENCES bitacora_mensual(id) ON DELETE CASCADE
);

CREATE INDEX idx_actividad_bitacora ON actividad(bitacora_id);

-- ============================================
-- TABLA: ARTÍCULOS
-- ============================================
CREATE TABLE IF NOT EXISTS articulo (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    doi VARCHAR(200) UNIQUE NOT NULL,
    titulo VARCHAR(500) NOT NULL,
    revista VARCHAR(200),
    volumen VARCHAR(50),
    numero VARCHAR(50),
    pagina_inicio INTEGER,
    pagina_fin INTEGER,
    fecha_publicacion DATE,
    cuartil VARCHAR(10),
    indexacion VARCHAR(100),
    url_acceso VARCHAR(500),
    estado_publicacion VARCHAR(50),
    
    -- Foreign Key
    proyecto_id INTEGER NOT NULL,
    
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    creado_por VARCHAR(100),
    modificado_por VARCHAR(100),
    activo BOOLEAN DEFAULT 1,
    
    FOREIGN KEY (proyecto_id) REFERENCES proyecto(id) ON DELETE RESTRICT
);

CREATE INDEX idx_articulo_proyecto ON articulo(proyecto_id);
CREATE INDEX idx_articulo_doi ON articulo(doi);

-- ============================================
-- TABLA: AUTORES EXTERNOS
-- ============================================
CREATE TABLE IF NOT EXISTS autor_externo (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(150),
    afiliacion VARCHAR(200),
    pais_afiliacion VARCHAR(100),
    orcid VARCHAR(50),
    
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    creado_por VARCHAR(100),
    modificado_por VARCHAR(100),
    activo BOOLEAN DEFAULT 1
);

-- ============================================
-- TABLA: AUTORES DE ARTÍCULOS (Relación N:M)
-- ============================================
CREATE TABLE IF NOT EXISTS autor_articulo (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    articulo_id INTEGER NOT NULL,
    usuario_id INTEGER, -- NULL si es autor externo
    autor_externo_id INTEGER, -- NULL si es usuario interno
    orden_autoria INTEGER NOT NULL,
    es_autor_correspondiente BOOLEAN DEFAULT 0,
    contribucion TEXT,
    
    FOREIGN KEY (articulo_id) REFERENCES articulo(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE SET NULL,
    FOREIGN KEY (autor_externo_id) REFERENCES autor_externo(id) ON DELETE SET NULL,
    CHECK ((usuario_id IS NOT NULL AND autor_externo_id IS NULL) OR 
           (usuario_id IS NULL AND autor_externo_id IS NOT NULL))
);

CREATE INDEX idx_autor_articulo_articulo ON autor_articulo(articulo_id);
CREATE INDEX idx_autor_articulo_usuario ON autor_articulo(usuario_id);

-- ============================================
-- TABLA: NOTIFICACIONES
-- ============================================
CREATE TABLE IF NOT EXISTS notificacion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    titulo VARCHAR(200) NOT NULL,
    mensaje TEXT NOT NULL,
    tipo VARCHAR(50) NOT NULL, -- 'NUEVO_PROYECTO', 'NUEVA_SOLICITUD_CONTRATO', etc.
    fecha_envio DATETIME DEFAULT CURRENT_TIMESTAMP,
    leida BOOLEAN DEFAULT 0,
    fecha_lectura DATETIME,
    url_referencia VARCHAR(300),
    datos_adicionales TEXT,
    
    -- Foreign Keys
    destinatario_id INTEGER NOT NULL,
    emisor_id INTEGER,
    
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    creado_por VARCHAR(100),
    modificado_por VARCHAR(100),
    activo BOOLEAN DEFAULT 1,
    
    FOREIGN KEY (destinatario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    FOREIGN KEY (emisor_id) REFERENCES usuario(id) ON DELETE SET NULL
);

CREATE INDEX idx_notificacion_destinatario ON notificacion(destinatario_id);
CREATE INDEX idx_notificacion_leida ON notificacion(leida);

-- ============================================
-- TABLA: CONFIGURACIÓN DEL SISTEMA
-- ============================================
CREATE TABLE IF NOT EXISTS configuracion_sistema (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    clave_configuracion VARCHAR(100) UNIQUE NOT NULL,
    valor_configuracion TEXT NOT NULL,
    descripcion TEXT,
    editable_por_jefatura BOOLEAN DEFAULT 0,
    
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    creado_por VARCHAR(100),
    modificado_por VARCHAR(100),
    activo BOOLEAN DEFAULT 1
);

-- ============================================
-- TABLA: AUDITORÍA
-- ============================================
CREATE TABLE IF NOT EXISTS evento_auditoria (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo_evento VARCHAR(100) NOT NULL,
    entidad_afectada VARCHAR(100) NOT NULL,
    id_entidad_afectada INTEGER,
    accion VARCHAR(50) NOT NULL, -- 'CREATE', 'UPDATE', 'DELETE', 'LOGIN', etc.
    detalles TEXT,
    ip_origen VARCHAR(50),
    fecha_evento DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Key
    usuario_id INTEGER,
    
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE SET NULL
);

CREATE INDEX idx_auditoria_usuario ON evento_auditoria(usuario_id);
CREATE INDEX idx_auditoria_fecha ON evento_auditoria(fecha_evento);
CREATE INDEX idx_auditoria_entidad ON evento_auditoria(entidad_afectada, id_entidad_afectada);