package com.custmanage.server.auth.context;

import java.util.List;

/**
 * JWT 解析后的登录用户信息，存入 ThreadLocal。
 */
public class LoginUser {

    private Long userId;
    private String userName;
    private String oaAccount;
    private Long orgId;
    private String orgPath;
    private String passwordHash;
    private String accountStatus;
    private List<String> roles;

    public LoginUser() {}

    public LoginUser(Long userId, String userName, String oaAccount, Long orgId, String orgPath, List<String> roles) {
        this.userId = userId;
        this.userName = userName;
        this.oaAccount = oaAccount;
        this.orgId = orgId;
        this.orgPath = orgPath;
        this.roles = roles;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getOaAccount() { return oaAccount; }
    public void setOaAccount(String oaAccount) { this.oaAccount = oaAccount; }

    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }

    public String getOrgPath() { return orgPath; }
    public void setOrgPath(String orgPath) { this.orgPath = orgPath; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
