create index if not exists t1_application_migration_status_idx on loanorc.t1(application_migration_status);
--rollback drop index if exists t1_application_migration_status_idx;

create index if not exists t1_update_date_desc_idx on loanorc.t1(update_date desc);
--rollback drop index if exists t1_update_date_desc_idx;