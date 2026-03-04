ALTER TABLE employees
  MODIFY joining_date datetime(6) NOT NULL,
  MODIFY department varchar(255) NOT NULL,
  MODIFY designation varchar(255) NOT NULL,
  MODIFY salary double NOT NULL;
