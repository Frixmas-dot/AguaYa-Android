-- ============================================================
--  AguaYa — Script de Migración V2 (Borrado Lógico y Barrios Oficiales)
--  Ejecutar en la base de datos 'aguaya' existente sin perder datos
-- ============================================================

USE aguaya;

-- 1. Agregar columnas para Borrado Lógico (Soft Delete)
ALTER TABLE reportes 
  ADD COLUMN IF NOT EXISTS activo TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1: visible/activo, 0: archivado/oculto',
  ADD COLUMN IF NOT EXISTS deleted_at DATETIME NULL DEFAULT NULL COMMENT 'Fecha y hora en que se archivó';

-- Actualizar reportes existentes para asegurar que estén activos
UPDATE reportes SET activo = 1 WHERE activo IS NULL;

-- 2. Tabla oficial de Barrios de Santa Marta
CREATE TABLE IF NOT EXISTS barrios (
  id        INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
  nombre    VARCHAR(100)  NOT NULL UNIQUE,
  localidad ENUM('Localidad 1','Localidad 2','Localidad 3') NOT NULL,
  lat       DOUBLE        NOT NULL,
  lng       DOUBLE        NOT NULL,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Poblado con Barrios y Sectores Oficiales de Santa Marta (POT y Alcaldía Distrital)
INSERT IGNORE INTO barrios (nombre, localidad, lat, lng) VALUES
  -- LOCALIDAD 1: Cultural Tayrona - San Pedro Alejandrino
  ('Mamatoco', 'Localidad 1', 11.2356, -74.1706),
  ('Bonda', 'Localidad 1', 11.2333, -74.1167),
  ('Cantilito', 'Localidad 1', 11.2280, -74.1290),
  ('Timayui', 'Localidad 1', 11.2310, -74.1350),
  ('Ciudad Equidad', 'Localidad 1', 11.2289, -74.1458),
  ('Once de Noviembre', 'Localidad 1', 11.2380, -74.1550),
  ('20 de Octubre', 'Localidad 1', 11.2370, -74.1600),
  ('Garagoa', 'Localidad 1', 11.2270, -74.1500),
  ('El Cisne', 'Localidad 1', 11.2240, -74.1480),
  ('Rodrigo Ahumada', 'Localidad 1', 11.2260, -74.1460),
  ('Bolivariana', 'Localidad 1', 11.2320, -74.1850),
  ('Los Laureles', 'Localidad 1', 11.2290, -74.1750),
  ('Santa Cruz', 'Localidad 1', 11.2310, -74.1730),
  ('Villa del Carmen', 'Localidad 1', 11.2280, -74.1680),
  ('Villa Bella', 'Localidad 1', 11.2300, -74.1650),
  ('Villa Concha', 'Localidad 1', 11.2330, -74.1630),
  ('El Parque', 'Localidad 1', 11.2222, -74.1625),
  ('La Concepción', 'Localidad 1', 11.2250, -74.1720),
  ('Santa Clara', 'Localidad 1', 11.2340, -74.1710),
  ('Villa Dania', 'Localidad 1', 11.2270, -74.1520),
  ('Nueva Colombia', 'Localidad 1', 11.2330, -74.1590),
  ('Los Alpes', 'Localidad 1', 11.2260, -74.1640),
  ('Curinca', 'Localidad 1', 11.2210, -74.1710),
  ('Luz del Mundo', 'Localidad 1', 11.2345, -74.1670),
  ('Yucal', 'Localidad 1', 11.2255, -74.1580),
  ('Oasis', 'Localidad 1', 11.2365, -74.1610),
  ('Rosalía', 'Localidad 1', 11.2325, -74.1660),
  ('San Pedro Alejandrino', 'Localidad 1', 11.2335, -74.1830),
  ('Santa Ana', 'Localidad 1', 11.2340, -74.1810),

  -- LOCALIDAD 2: Histórica - Rodrigo de Bastidas
  ('Centro Histórico', 'Localidad 2', 11.2442, -74.2117),
  ('Pescaíto', 'Localidad 2', 11.2505, -74.2098),
  ('San Martín', 'Localidad 2', 11.2480, -74.2050),
  ('San Jorge', 'Localidad 2', 11.2520, -74.1980),
  ('San Fernando', 'Localidad 2', 11.2510, -74.1950),
  ('Nacho Vives', 'Localidad 2', 11.2560, -74.1920),
  ('Taganga', 'Localidad 2', 11.2667, -74.1833),
  ('Bastidas', 'Localidad 2', 11.2520, -74.1850),
  ('Chimila', 'Localidad 2', 11.2580, -74.1880),
  ('17 de Diciembre', 'Localidad 2', 11.2410, -74.1750),
  ('20 de Julio', 'Localidad 2', 11.2390, -74.1810),
  ('Los Almendros', 'Localidad 2', 11.2467, -74.1950),
  ('El Cundí', 'Localidad 2', 11.2410, -74.2060),
  ('Bavaria', 'Localidad 2', 11.2389, -74.2014),
  ('El Mayor', 'Localidad 2', 11.2360, -74.2040),
  ('Minuto de Dios', 'Localidad 2', 11.2260, -74.1980),
  ('Taminaca', 'Localidad 2', 11.2430, -74.2030),
  ('Santa Fe', 'Localidad 2', 11.2340, -74.1800),
  ('Obrero', 'Localidad 2', 11.2435, -74.2080),
  ('Olaya Herrera', 'Localidad 2', 11.2450, -74.2070),
  ('Manzanares', 'Localidad 2', 11.2350, -74.2130),
  ('Martinete', 'Localidad 2', 11.2320, -74.2140),
  ('San Pablo', 'Localidad 2', 11.2470, -74.2010),
  ('Betania', 'Localidad 2', 11.2460, -74.1990),
  ('Los Ángeles', 'Localidad 2', 11.2415, -74.2045),
  ('El Pradito', 'Localidad 2', 11.2440, -74.2010),
  ('Alcudia', 'Localidad 2', 11.2405, -74.2005),
  ('Urbanización Riascos', 'Localidad 2', 11.2375, -74.2025),
  ('Jardín', 'Localidad 2', 11.2333, -74.1917),
  ('Libertador', 'Localidad 2', 11.2395, -74.1930),
  ('María Eugenia', 'Localidad 2', 11.2250, -74.2083),
  ('Pastrana', 'Localidad 2', 11.2200, -74.2050),
  ('Primero de Mayo', 'Localidad 2', 11.2220, -74.2070),
  ('Las Américas', 'Localidad 2', 11.2230, -74.2060),
  ('Corea', 'Localidad 2', 11.2240, -74.2090),
  ('San José del Pando', 'Localidad 2', 11.2270, -74.2010),
  ('El Pando', 'Localidad 2', 11.2215, -74.1980),
  ('Murallas del Pando', 'Localidad 2', 11.2205, -74.1960),
  ('Acodis', 'Localidad 2', 11.2195, -74.1970),
  ('Colinas del Pando', 'Localidad 2', 11.2185, -74.1990),
  ('San Francisco', 'Localidad 2', 11.2490, -74.2000),
  ('Villa del Río', 'Localidad 2', 11.2385, -74.1965),
  ('Boulevard de las Rosas', 'Localidad 2', 11.2365, -74.1945),

  -- LOCALIDAD 3: Turística - Perla del Caribe
  ('El Rodadero', 'Localidad 3', 11.2056, -74.2267),
  ('Gaira', 'Localidad 3', 11.1931, -74.2197),
  ('Playa Salguero', 'Localidad 3', 11.1980, -74.2290),
  ('Pozos Colorados', 'Localidad 3', 11.1714, -74.2306),
  ('Bello Horizonte', 'Localidad 3', 11.1578, -74.2322),
  ('La Paz', 'Localidad 3', 11.1780, -74.2150),
  ('Cristo Rey', 'Localidad 3', 11.1820, -74.2180),
  ('Vista Hermosa', 'Localidad 3', 11.1750, -74.2170),
  ('Aeromar', 'Localidad 3', 11.1450, -74.2310),
  ('Don Jaca', 'Localidad 3', 11.1350, -74.2300),
  ('Los Lirios', 'Localidad 3', 11.1850, -74.2160),
  ('Nueva Betel', 'Localidad 3', 11.1810, -74.2190),
  ('Ayapel', 'Localidad 3', 11.1790, -74.2175),
  ('Totumo', 'Localidad 3', 11.1830, -74.2140),
  ('Rodadero Reservado', 'Localidad 3', 11.2090, -74.2240),
  ('Cerro Ziruma', 'Localidad 3', 11.2150, -74.2210),
  ('Salguero Beach', 'Localidad 3', 11.1960, -74.2280),
  ('Plenomar', 'Localidad 3', 11.1680, -74.2315),
  ('Zuana', 'Localidad 3', 11.1520, -74.2325),
  ('Irotama', 'Localidad 3', 11.1500, -74.2330);
