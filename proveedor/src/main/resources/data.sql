INSERT INTO proveedor (id, nombre_empresa, rut, correo) 
VALUES (1, 'Ascensores Ascensio', '77.123.456-K', 'contacto@ascensoresasencio.cl');

SELECT setval(pg_get_serial_sequence('proveedor', 'id'), (SELECT MAX(id) FROM proveedor));