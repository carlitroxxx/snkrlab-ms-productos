-- Ejecutar una sola vez, con respaldo previo, antes de arrancar la nueva version.
-- MySQL: no borra registros ni modifica IDs, stock o snapshots de compras.
ALTER TABLE productos ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;
