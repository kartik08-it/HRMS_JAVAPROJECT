ALTER TABLE employees
  ADD INDEX IDX_EMPLOYEES_USER_ID (user_id);

ALTER TABLE employees
  DROP INDEX UKj2dmgsma6pont6kf7nic9elpd;

ALTER TABLE employees
  ADD COLUMN profile_type varchar(50) NOT NULL DEFAULT 'EMPLOYEE';
