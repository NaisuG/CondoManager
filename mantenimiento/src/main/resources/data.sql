INSERT INTO orden_mantenimiento (id, id_proveedor, descripcion, estado) 
VALUES (1, 1, 'Mantenimiento ascensor', 'PENDIENTE');

SELECT setval(pg_get_serial_sequence('orden_mantenimiento', 'id'), (SELECT MAX(id) FROM orden_mantenimiento));