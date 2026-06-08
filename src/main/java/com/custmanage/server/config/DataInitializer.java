package com.custmanage.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 首次启动时初始化系统管理员账号。
 * 密码加密方式：MD5(盐 + 密码)，盐 = cust_manage_salt。
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String PASSWORD_SALT = "cust_manage_salt";

    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        initAdminUser();
    }

    private void initAdminUser() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE oa_account = 'admin' AND deleted_flag = 0",
                Integer.class);
        if (count != null && count > 0) {
            log.info("管理员账号已存在，跳过初始化");
            return;
        }

        String encodedPassword = md5(PASSWORD_SALT + "admin123");

        jdbcTemplate.update(
                "INSERT INTO sys_user (id, user_name, oa_account, org_id, password_hash, password_salt, account_status, created_by, created_time, updated_by, updated_time, deleted_flag) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, NOW(), ?)",
                1L, "系统管理员", "admin", 1L, encodedPassword, PASSWORD_SALT, "正常", null, null, 0);

        // 绑定管理员角色
        jdbcTemplate.update(
                "INSERT INTO sys_user_role (id, user_id, role_id, created_time) VALUES (?, ?, ?, NOW())",
                1L, 1L, 14L);

        log.info("系统管理员账号初始化完成: admin / admin123");
    }

    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
}
