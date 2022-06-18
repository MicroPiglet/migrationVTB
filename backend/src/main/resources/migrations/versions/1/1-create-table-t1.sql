CREATE TABLE IF NOT EXISTS loanorc.t1
        (
        id                            uuid not null constraint t1_pk primary key,
        update_date                   timestamp not null default now(),
        application_migration_status  integer not null,
        error_description             varchar(255)
        );

        COMMENT ON TABLE loanorc.t1 IS 'Статус миграции заявок';
        COMMENT ON COLUMN loanorc.t1.id IS 'Номер заявки в системе ВТБ-онлайн';
        COMMENT ON COLUMN loanorc.t1.update_date IS 'Дата обновления записи';
        COMMENT ON COLUMN loanorc.t1.application_migration_status IS 'Статус ';
        COMMENT ON COLUMN loanorc.t1.error_description IS 'Текст ошибки';

