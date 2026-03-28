CREATE TABLE attendance_records (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  employee_id BIGINT NOT NULL,
  record_date DATE NOT NULL,
  state VARCHAR(30) NOT NULL,
  check_in_time DATETIME(6) NULL,
  check_out_time DATETIME(6) NULL,
  late_minutes INT NULL,
  absence_reason VARCHAR(255) NULL,
  absence_approved BOOLEAN NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NULL,
  CONSTRAINT fk_attendance_records_employee
    FOREIGN KEY (employee_id) REFERENCES employees(id),
  CONSTRAINT uk_attendance_records_employee_date
    UNIQUE (employee_id, record_date)
);

CREATE INDEX idx_attendance_records_date
  ON attendance_records(record_date);

CREATE INDEX idx_attendance_records_employee
  ON attendance_records(employee_id);

CREATE TABLE attendance_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  employee_id BIGINT NOT NULL,
  action VARCHAR(30) NOT NULL,
  status VARCHAR(30) NULL,
  event_time DATETIME(6) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  CONSTRAINT fk_attendance_logs_employee
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE INDEX idx_attendance_logs_event_time
  ON attendance_logs(event_time);

CREATE INDEX idx_attendance_logs_employee
  ON attendance_logs(employee_id);
