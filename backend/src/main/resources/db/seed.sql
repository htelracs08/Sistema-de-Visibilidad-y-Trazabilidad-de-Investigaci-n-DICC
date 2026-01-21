-- CORREGIDO: usar el correo que usas en los comandos de prueba
INSERT OR IGNORE INTO usuario (
  id, nombres, apellidos, correo, password, rol, debe_cambiar_password
) VALUES (
  '8f7a5df3-8f2a-4a4a-9dcb-0c2c9a0a0001',
  'JONATHAN STEVEN',
  'GUAMAN MAZA',
  'jonathan.guaman04@epn.edu.ec',
  'Jefatura123*',
  'JEFATURA',
  0
);