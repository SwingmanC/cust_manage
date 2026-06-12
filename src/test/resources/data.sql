INSERT INTO t_yqyc_sys_org (id, org_code, org_name, org_type, org_path, status, created_time, updated_time)
VALUES (10, 'BU_TRAFFIC', '交通能源BU', 'BU', '/10/', 'ENABLED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO t_yqyc_sys_user (id, user_name, oa_account, org_id, account_status, created_time, updated_time)
VALUES (100, '张三', 'zhangsan', 10, 'ENABLED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO t_yqyc_crm_group (
  id, group_code, group_name, group_category, group_type_code, bu_org_id, manager_user_id,
  is_important, is_key_customer, is_139, location_type, main_contact_name, main_contact_phone,
  contact_book_status, social_security_count, status, created_time, updated_time, deleted_flag
) VALUES
(1000, 'GRP001', '苏州某某集团股份有限公司', 'LOCAL_PARENT', 'NAMED_GROUP', 10, 100, 1, 1, 1, 'LOCAL', '王主任', '13812345678', 'COMPLETE', 256, 'ENABLED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1001, 'GRP002', '苏州样例科技有限公司', 'LOCAL_SINGLE', 'NORMAL_GROUP', 10, 100, 0, 0, 0, 'LOCAL', '李经理', '13900001111', 'MISSING', 88, 'ENABLED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO t_yqyc_biz_service (id, service_code, service_name, service_category, status, sort_no, created_time, updated_time)
VALUES
(200, 'MOBILE_CLOUD', '移动云', '重点业务', 'ENABLED', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(201, 'DEDICATED_LINE', '专线', '重点业务', 'ENABLED', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(202, 'IOT', '物联网', '重点业务', 'ENABLED', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO t_yqyc_biz_group_revenue_monthly (id, group_id, service_id, revenue_month, revenue_amount, source_system, sync_batch_no, sync_time, created_time, updated_time)
VALUES
(3000, 1000, 200, '2026-01', 12000.50, 'REV_SYS', 'BATCH001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3001, 1000, 201, '2026-01', 8000.00, 'REV_SYS', 'BATCH001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3002, 1000, 200, '2026-02', 15000.00, 'REV_SYS', 'BATCH002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO t_yqyc_biz_group_bc (id, group_id, service_id, stat_month, quantity, source_system, created_time, updated_time)
VALUES
(4000, 1000, 200, '2026-01', 8, 'BC_SYS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4001, 1000, 201, '2026-01', 12, 'BC_SYS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4002, 1000, 202, '2026-02', 234, 'BC_SYS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO t_yqyc_biz_group_competitor (id, group_id, service_id, competitor_name, stat_month, quantity, remark, created_by, created_time, updated_by, updated_time)
VALUES
(5000, 1000, 201, '电信', '2026-01', 3, '集团有存量专线', 100, CURRENT_TIMESTAMP, 100, CURRENT_TIMESTAMP),
(5001, 1000, 200, '联通', '2026-01', 1, '移动云试点', 100, CURRENT_TIMESTAMP, 100, CURRENT_TIMESTAMP),
(5002, 1000, 201, '电信', '2026-02', 4, '专线增加', 100, CURRENT_TIMESTAMP, 100, CURRENT_TIMESTAMP);

INSERT INTO t_yqyc_crm_group_contact (id, group_id, contact_name, contact_phone, position_name, department_name, status, source_type, created_by, created_time, updated_by, updated_time, deleted_flag)
VALUES
(6000, 1000, '王主任', '13812345678', '信息技术部主任', '信息技术部', '有效', '手工', 100, CURRENT_TIMESTAMP, 100, CURRENT_TIMESTAMP, 0),
(6001, 1000, '李副总', '13912345678', '副总经理', '总裁办', '有效', '手工', 100, CURRENT_TIMESTAMP, 100, CURRENT_TIMESTAMP, 0),
(6002, 1000, '赵经理', '13700001111', 'IT经理', '信息技术部', '审核中', 'Excel', 100, CURRENT_TIMESTAMP, 100, CURRENT_TIMESTAMP, 0),
(6003, 1001, '刘主管', '13600002222', '行政主管', '行政部', '有效', '手工', 100, CURRENT_TIMESTAMP, 100, CURRENT_TIMESTAMP, 0);

INSERT INTO t_yqyc_biz_opportunity (id, group_id, source_type, opportunity_name, opportunity_type, opportunity_status, service_id, expected_amount, expected_close_date, description, owner_user_id, created_time, updated_time)
VALUES
(7000, 1000, '商机系统', '5G专网部署项目', '5G', '跟进中', 200, 500000.00, '2026-06-30', '计划Q2完成5G专网部署', 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7001, 1000, '泛商机平台', '移动云扩容', '移动云', '在途', 200, 800000.00, '2026-09-30', '集团计划扩容移动云资源', 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7002, 1000, '省公司推荐', '物联网升级', '物联网', '成交', 202, 200000.00, '2026-03-15', '已完成物联网平台升级', 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
