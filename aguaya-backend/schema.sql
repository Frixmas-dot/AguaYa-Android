-- ============================================================
--  AguaYa — Schema actualizado
--  Ejecutar completo en MySQL Workbench
-- ============================================================

CREATE DATABASE IF NOT EXISTS aguaya
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE aguaya;

-- ------------------------------------------------------------
--  Tabla principal de reportes
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reportes (
  id          INT UNSIGNED    AUTO_INCREMENT PRIMARY KEY,
  barrio      VARCHAR(100)    NOT NULL,
  tipo        ENUM('Sin agua','Baja presión','Agua sucia','Fuga') NOT NULL,
  descripcion TEXT            NOT NULL,
  urgencia    ENUM('baja','media','alta') NOT NULL DEFAULT 'media',
  estado      ENUM('pendiente','en_proceso','resuelto') NOT NULL DEFAULT 'pendiente',
  lat         DOUBLE          NULL,
  lng         DOUBLE          NULL,
  foto_url    VARCHAR(500)    NULL COMMENT 'URL de imagen en Cloudinary',
  activo      TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '1: activo/visible, 0: archivado/soft delete',
  deleted_at  DATETIME        NULL DEFAULT NULL COMMENT 'Fecha de archivado',
  created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_tipo (tipo),
  INDEX idx_barrio (barrio),
  INDEX idx_estado (estado),
  INDEX idx_urgencia (urgencia),
  INDEX idx_fecha (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
--  Tabla oficial de Barrios y Localidades de Santa Marta
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS barrios (
  id        INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
  nombre    VARCHAR(100)  NOT NULL UNIQUE,
  localidad ENUM('Localidad 1','Localidad 2','Localidad 3') NOT NULL,
  lat       DOUBLE        NOT NULL,
  lng       DOUBLE        NOT NULL,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
--  Tabla de confirmaciones ("yo también tengo este problema")
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS confirmaciones (
  id          INT UNSIGNED    AUTO_INCREMENT PRIMARY KEY,
  reporte_id  INT UNSIGNED    NOT NULL,
  ip_hash     VARCHAR(64)     NOT NULL COMMENT 'Hash de IP para evitar duplicados',
  created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (reporte_id) REFERENCES reportes(id) ON DELETE CASCADE,
  UNIQUE KEY uq_confirmacion (reporte_id, ip_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
--  Datos de ejemplo (idempotente)
-- ------------------------------------------------------------
INSERT IGNORE INTO reportes (id, barrio, tipo, descripcion, urgencia, estado, lat, lng, created_at) VALUES
  (1, 'El Rodadero', 'Sin agua',     'Sin agua desde ayer en el sector.',         'alta',  'pendiente',  11.2050, -74.2250, DATE_SUB(NOW(), INTERVAL 2  HOUR)),
  (2, 'Centro',      'Baja presión', 'Baja presión en el servicio.',              'media', 'en_proceso', 11.2408, -74.2110, DATE_SUB(NOW(), INTERVAL 4  HOUR)),
  (3, 'Gaira',       'Agua sucia',   'Agua con sedimentos y color marrón.',       'media', 'pendiente',  11.1880, -74.2150, DATE_SUB(NOW(), INTERVAL 5  HOUR)),
  (4, 'Bastidas',    'Fuga',         'Fuga visible en tubería principal.',         'alta',  'pendiente',  11.2520, -74.1850, DATE_SUB(NOW(), INTERVAL 6  HOUR)),
  (5, 'Pescaíto',    'Sin agua',     'Corte total sin aviso previo.',             'alta',  'en_proceso', 11.2489, -74.1972, DATE_SUB(NOW(), INTERVAL 7  HOUR)),
  (6, 'La Paz',      'Baja presión', 'Presión muy baja desde la mañana.',         'baja',  'resuelto',   11.2350, -74.2200, DATE_SUB(NOW(), INTERVAL 9  HOUR));

-- ------------------------------------------------------------
--  Vistas para materia de Bases de Datos
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW vista_estadisticas AS
SELECT
  tipo,
  COUNT(*)                                                AS total,
  SUM(CASE WHEN estado = 'resuelto'   THEN 1 ELSE 0 END) AS resueltos,
  SUM(CASE WHEN estado = 'pendiente'  THEN 1 ELSE 0 END) AS pendientes,
  SUM(CASE WHEN estado = 'en_proceso' THEN 1 ELSE 0 END) AS en_proceso,
  SUM(CASE WHEN urgencia = 'alta'     THEN 1 ELSE 0 END) AS urgencia_alta
FROM reportes
GROUP BY tipo;

CREATE OR REPLACE VIEW vista_barrios AS
SELECT
  barrio,
  COUNT(*) AS total_reportes,
  SUM(CASE WHEN estado != 'resuelto' THEN 1 ELSE 0 END) AS activos
FROM reportes
GROUP BY barrio
ORDER BY total_reportes DESC;

CREATE OR REPLACE VIEW vista_confirmaciones AS
SELECT
  r.id, r.barrio, r.tipo, r.estado,
  COUNT(c.id) AS confirmaciones
FROM reportes r
LEFT JOIN confirmaciones c ON c.reporte_id = r.id
GROUP BY r.id
ORDER BY confirmaciones DESC;
