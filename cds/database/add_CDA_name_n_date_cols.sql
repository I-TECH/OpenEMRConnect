-- Additional fields for index and retrieval of CDS records 
-- 
-- to run, from a command shell:
--   mysql -u <user> -p <database_name> < <this_file>
-- 
-- i.e.
--   mysql -u oecuser -p cds < add_CDA_name_n_date_cols.sql

ALTER TABLE CDA ADD COLUMN first_name TEXT NULL;
ALTER TABLE CDA ADD COLUMN last_name TEXT NULL;
ALTER TABLE CDA ADD COLUMN date_generated DATETIME NOT NULL;
ALTER TABLE CDA ADD COLUMN date_stored DATETIME NOT NULL;
