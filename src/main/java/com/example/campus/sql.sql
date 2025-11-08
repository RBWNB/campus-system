create DATABASE campus_system;




ALTER TABLE leaves
    ADD COLUMN reviewed_at DATETIME,
    ADD COLUMN reviewer VARCHAR(255),
    ADD COLUMN comment TEXT;
