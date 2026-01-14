-- ============================================
-- DATOS INICIALES PARA EL SISTEMA DICC
-- ============================================

-- ============================================
-- INSERTAR LABORATORIOS
-- ============================================
INSERT OR IGNORE INTO laboratorio (codigo_laboratorio, nombre, ubicacion, responsable, extension) VALUES
('LAB-001', 'Laboratorio de Inteligencia Artificial', 'Edificio FICA - Piso 3', 'Dr. Juan Pérez', '1234'),
('LAB-002', 'Laboratorio de Redes y Telecomunicaciones', 'Edificio FICA - Piso 2', 'Dra. María González', '1235'),
('LAB-003', 'Laboratorio de Desarrollo de Software', 'Edificio FICA - Piso 4', 'Dr. Carlos López', '1236'),
('LAB-004', 'Laboratorio de Sistemas Embebidos', 'Edificio FICA - Piso 1', 'Dr. Ana Martínez', '1237'),
('LAB-005', 'Laboratorio de Bases de Datos', 'Edificio FICA - Piso 3', 'Dr. Luis Rodríguez', '1238');

-- ============================================
-- INSERTAR USUARIO JEFATURA INICIAL
-- Password: jefatura123 (debe ser hasheado en producción)
-- ============================================
INSERT OR IGNORE INTO usuario (
    tipo_usuario, 
    codigo_epn, 
    cedula, 
    nombres, 
    apellidos, 
    correo_institucional, 
    password_hash, 
    rol, 
    email_verificado,
    cargo,
    codigo_registro_especial
) VALUES (
    'JEFATURA',
    'JEFA001',
    '1234567890',
    'María',
    'Rodríguez Vega',
    'jefatura.dicc@epn.edu.ec',
    'jefatura123', -- BCrypt hash de "jefatura123"
    'JEFATURA_DICC',
    1,
    'Directora del DICC',
    'JEFATURA-DICC-2025-SECRET'
);

-- ============================================
-- INSERTAR DOCENTES DE EJEMPLO
-- Password: docente123
-- ============================================
INSERT OR IGNORE INTO usuario (
    tipo_usuario,
    codigo_epn,
    cedula,
    nombres,
    apellidos,
    correo_institucional,
    password_hash,
    rol,
    email_verificado,
    departamento,
    cubiculo,
    extension,
    area_investigacion
) VALUES 
(
    'DOCENTE',
    'DOC001',
    '1710234567',
    'Carlos',
    'Ramírez Torres',
    'carlos.ramirez@epn.edu.ec',
    'jefatura123',
    'DIRECTOR_PROYECTO',
    1,
    'Departamento de Informática',
    'FICA-301',
    '1240',
    'Inteligencia Artificial y Machine Learning'
),
(
    'DOCENTE',
    'DOC002',
    '1710345678',
    'Ana',
    'Morales Castro',
    'ana.morales@epn.edu.ec',
    'jefatura123',
    'DIRECTOR_PROYECTO',
    1,
    'Departamento de Sistemas',
    'FICA-205',
    '1241',
    'Desarrollo de Software y Arquitecturas'
);

-- ============================================
-- INSERTAR AYUDANTES DE EJEMPLO
-- Password: ayudante123
-- ============================================
INSERT OR IGNORE INTO usuario (
    tipo_usuario,
    codigo_epn,
    cedula,
    nombres,
    apellidos,
    correo_institucional,
    password_hash,
    rol,
    email_verificado,
    carrera,
    facultad,
    quintil,
    semestre_actual,
    promedio_general
) VALUES 
(
    'AYUDANTE',
    'L00001234',
    '1720456789',
    'José',
    'García López',
    'jose.garcia@epn.edu.ec',
    'jefatura123',
    'AYUDANTE_PROYECTO',
    1,
    'Ingeniería en Sistemas Informáticos',
    'Facultad de Ingeniería en Sistemas',
    2,
    7,
    8.5
),
(
    'AYUDANTE',
    'L00002345',
    '1720567890',
    'Laura',
    'Fernández Ruiz',
    'laura.fernandez@epn.edu.ec',
    'jefatura123',
    'AYUDANTE_PROYECTO',
    1,
    'Ingeniería de Software',
    'Facultad de Ingeniería en Sistemas',
    1,
    6,
    9.2
),
(
    'AYUDANTE',
    'L00003456',
    '1720678901',
    'Pedro',
    'Sánchez Mora',
    'pedro.sanchez@epn.edu.ec',
    'jefatura123',
    'AYUDANTE_PROYECTO',
    1,
    'Ingeniería Electrónica',
    'Facultad de Ingeniería Eléctrica y Electrónica',
    3,
    5,
    8.0
);

-- ============================================
-- INSERTAR PROYECTOS AUTORIZADOS
-- ============================================
INSERT OR IGNORE INTO proyecto_autorizado (codigo_proyecto, autorizado_por) VALUES
('PROJ-2025-IA-001', 'jefatura.dicc@epn.edu.ec'),
('PROJ-2025-IA-002', 'jefatura.dicc@epn.edu.ec'),
('PROJ-2025-SW-001', 'jefatura.dicc@epn.edu.ec'),
('PROJ-2025-SW-002', 'jefatura.dicc@epn.edu.ec'),
('PROJ-2025-TT-001', 'jefatura.dicc@epn.edu.ec'),
('PROJ-2025-VIN-001', 'jefatura.dicc@epn.edu.ec'),
('PROJ-2025-VIN-002', 'jefatura.dicc@epn.edu.ec');

-- ============================================
-- INSERTAR CONFIGURACIÓN DEL SISTEMA
-- ============================================
INSERT OR IGNORE INTO configuracion_sistema (clave_configuracion, valor_configuracion, descripcion, editable_por_jefatura) VALUES
('CODIGO_REGISTRO_JEFATURA', 'JEFATURA-DICC-2025-SECRET', 'Código especial para registro de Jefatura', 0),
('MAX_MESES_CONTRATO', '6', 'Número máximo de meses por contrato', 1),
('DIAS_LIMITE_BITACORA', '5', 'Días límite para enviar bitácora mensual', 1),
('NOTIFICACIONES_ACTIVAS', 'true', 'Estado del sistema de notificaciones', 1),
('RETENCION_NOTIFICACIONES_DIAS', '30', 'Días que se retienen notificaciones', 1);

-- ============================================
-- PROYECTO DE EJEMPLO (OPCIONAL - para demo)
-- ============================================
-- Descomentar si quieres un proyecto pre-creado para pruebas

/*
INSERT INTO proyecto (
    codigo_proyecto,
    titulo,
    descripcion,
    objetivo_general,
    fecha_inicio_real,
    fecha_fin_estimada,
    duracion_semestres,
    semestre_actual,
    estado,
    tipo_proyecto,
    director_id,
    laboratorio_id
) VALUES (
    'PROJ-2025-IA-001',
    'Sistema de Reconocimiento Facial con Deep Learning',
    'Desarrollo de un sistema de reconocimiento facial utilizando redes neuronales profundas',
    'Implementar un sistema preciso y eficiente de reconocimiento facial para control de acceso',
    '2025-02-01',
    '2025-12-31',
    2,
    1,
    'ACTIVO',
    'INVESTIGACION',
    (SELECT id FROM usuario WHERE codigo_epn = 'DOC001'),
    (SELECT id FROM laboratorio WHERE codigo_laboratorio = 'LAB-001')
);

-- Capacidad del proyecto de ejemplo
INSERT INTO proyecto_capacidad_semestre (proyecto_id, numero_semestre, numero_ayudantes, numero_meses_por_ayudante)
SELECT 
    (SELECT id FROM proyecto WHERE codigo_proyecto = 'PROJ-2025-IA-001'),
    1,
    2,
    5
UNION ALL
SELECT 
    (SELECT id FROM proyecto WHERE codigo_proyecto = 'PROJ-2025-IA-001'),
    2,
    3,
    6;

-- Marcar proyecto como utilizado
UPDATE proyecto_autorizado 
SET utilizado = 1, fecha_utilizacion = CURRENT_TIMESTAMP 
WHERE codigo_proyecto = 'PROJ-2025-IA-001';
*/