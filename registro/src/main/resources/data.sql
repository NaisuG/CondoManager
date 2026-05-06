-- Condominios 
INSERT INTO condominio (id, nombre, direccion) VALUES (1, 'Edificio Central', 'Santiago 123');
INSERT INTO condominio (id, nombre, direccion) VALUES (2, 'Condominio Central', 'Santiago Centro 456');

-- Tipos de Unidad 
INSERT INTO tipo_unidad (id, nombre) VALUES (1, 'UNIDAD TIPO A');
INSERT INTO tipo_unidad (id, nombre) VALUES (2, 'UNIDAD TIPO B');

-- Torres
INSERT INTO torre (id, id_condominio, numero) VALUES (1, 1, 10);

-- Unidades 
INSERT INTO unidad (id, id_torre, numero, id_tipo, m2) VALUES (1, 1, 101, 1, 55.5);
INSERT INTO unidad (id, id_torre, numero, id_tipo, m2) VALUES (2, 1, 102, 2, 75.5);

-- Reiniciar secuencias
SELECT setval(pg_get_serial_sequence('condominio', 'id'), (SELECT MAX(id) FROM condominio));
SELECT setval(pg_get_serial_sequence('tipo_unidad', 'id'), (SELECT MAX(id) FROM tipo_unidad));
SELECT setval(pg_get_serial_sequence('torre', 'id'), (SELECT MAX(id) FROM torre));
SELECT setval(pg_get_serial_sequence('unidad', 'id'), (SELECT MAX(id) FROM unidad));