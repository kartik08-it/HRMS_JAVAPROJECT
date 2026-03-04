ALTER TABLE employees
  ADD COLUMN username varchar(255) NULL,
  ADD COLUMN password varchar(255) NULL,
  ADD COLUMN email text NULL,
  ADD COLUMN email_hash varchar(255) NULL,
  ADD COLUMN phone text NULL,
  ADD COLUMN phone_hash varchar(255) NULL;

ALTER TABLE employees
  ADD UNIQUE INDEX UK_EMPLOYEES_USERNAME (username),
  ADD UNIQUE INDEX UK_EMPLOYEES_EMAIL_HASH (email_hash),
  ADD UNIQUE INDEX UK_EMPLOYEES_PHONE_HASH (phone_hash);
