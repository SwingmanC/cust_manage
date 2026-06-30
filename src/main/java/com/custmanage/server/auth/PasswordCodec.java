package com.custmanage.server.auth;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 密码编解码：MD5(盐 + 明文)。盐值固定 {@code cust_manage_salt}，
 * 与数据同步脚本（{@code data_sync/}）及历史存量数据保持一致。
 *
 * <p>供 {@code LocalAuthStrategy}（登录校验）与 {@code AuthServiceImpl}（改密）共用，
 * 避免加盐/哈希逻辑重复、口径不一致。</p>
 */
@Component
public class PasswordCodec {

    private static final String SALT = "cust_manage_salt";

    /** 计算明文密码的哈希值 */
    public String hash(String plain) {
        return md5(SALT + plain);
    }

    /** 校验明文密码是否与已存储哈希匹配 */
    public boolean matches(String plain, String storedHash) {
        if (plain == null || storedHash == null || storedHash.trim().isEmpty()) {
            return false;
        }
        return hash(plain).equalsIgnoreCase(storedHash);
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
