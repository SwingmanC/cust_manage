CREATE TABLE sys_org (
  id BIGINT PRIMARY KEY,
  org_code VARCHAR(50) NOT NULL,
  org_name VARCHAR(100) NOT NULL,
  parent_id BIGINT,
  leader_user_id BIGINT,
  org_type VARCHAR(32),
  org_path VARCHAR(500),
  sort_no INT DEFAULT 0,
  status VARCHAR(20) NOT NULL,
  created_time TIMESTAMP,
  updated_time TIMESTAMP
);

CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY,
  user_name VARCHAR(50) NOT NULL,
  mobile VARCHAR(30),
  oa_account VARCHAR(80),
  hr_no VARCHAR(80),
  org_id BIGINT,
  password_hash VARCHAR(200),
  password_salt VARCHAR(100),
  account_status VARCHAR(20) NOT NULL,
  last_login_time TIMESTAMP,
  created_time TIMESTAMP,
  updated_time TIMESTAMP
);

CREATE TABLE crm_group (
  id BIGINT PRIMARY KEY,
  group_code VARCHAR(32) NOT NULL,
  group_name VARCHAR(200) NOT NULL,
  group_category VARCHAR(32),
  group_type_code VARCHAR(32),
  bu_org_id BIGINT,
  manager_user_id BIGINT,
  is_important TINYINT,
  is_key_customer TINYINT,
  is_139 TINYINT,
  location_type VARCHAR(16),
  main_contact_name VARCHAR(50),
  main_contact_phone VARCHAR(30),
  contact_book_status VARCHAR(20),
  social_security_count INT DEFAULT 0,
  status VARCHAR(20) NOT NULL,
  created_by BIGINT,
  created_time TIMESTAMP,
  updated_by BIGINT,
  updated_time TIMESTAMP,
  deleted_flag TINYINT DEFAULT 0
);

CREATE TABLE biz_service (
  id BIGINT PRIMARY KEY,
  service_code VARCHAR(50) NOT NULL,
  service_name VARCHAR(100) NOT NULL,
  service_category VARCHAR(50),
  status VARCHAR(20) NOT NULL,
  sort_no INT DEFAULT 0,
  created_time TIMESTAMP,
  updated_time TIMESTAMP
);

CREATE TABLE biz_group_revenue_monthly (
  id BIGINT PRIMARY KEY,
  group_id BIGINT NOT NULL,
  service_id BIGINT NOT NULL,
  revenue_month CHAR(7) NOT NULL,
  revenue_amount DECIMAL(18,2) DEFAULT 0,
  source_system VARCHAR(50) NOT NULL,
  sync_batch_no VARCHAR(64),
  sync_time TIMESTAMP,
  created_time TIMESTAMP,
  updated_time TIMESTAMP
);

CREATE TABLE biz_group_bc (
  id BIGINT PRIMARY KEY,
  group_id BIGINT NOT NULL,
  service_id BIGINT NOT NULL,
  stat_month CHAR(7) NOT NULL,
  quantity DECIMAL(18,2) DEFAULT 0,
  source_system VARCHAR(50),
  created_time TIMESTAMP,
  updated_time TIMESTAMP
);

CREATE TABLE biz_group_competitor (
  id BIGINT PRIMARY KEY,
  group_id BIGINT NOT NULL,
  service_id BIGINT NOT NULL,
  competitor_name VARCHAR(20) NOT NULL,
  stat_month CHAR(7) NOT NULL,
  quantity DECIMAL(18,2) DEFAULT 0,
  remark VARCHAR(500),
  created_by BIGINT,
  created_time TIMESTAMP,
  updated_by BIGINT,
  updated_time TIMESTAMP
);

CREATE TABLE crm_group_contact (
  id BIGINT PRIMARY KEY,
  group_id BIGINT NOT NULL,
  contact_name VARCHAR(50) NOT NULL,
  contact_phone VARCHAR(30) NOT NULL,
  position_name VARCHAR(100),
  department_name VARCHAR(100),
  status VARCHAR(20),
  source_type VARCHAR(20),
  created_by BIGINT,
  created_time TIMESTAMP,
  updated_by BIGINT,
  updated_time TIMESTAMP,
  deleted_flag TINYINT DEFAULT 0
);

CREATE TABLE biz_opportunity (
  id BIGINT PRIMARY KEY,
  group_id BIGINT NOT NULL,
  source_type VARCHAR(32),
  opportunity_name VARCHAR(200) NOT NULL,
  opportunity_type VARCHAR(50),
  opportunity_status VARCHAR(32),
  service_id BIGINT,
  expected_amount DECIMAL(18,2),
  expected_close_date DATE,
  description VARCHAR(2000),
  owner_user_id BIGINT,
  created_time TIMESTAMP,
  updated_time TIMESTAMP
);
