package com.custmanage.server.vo;

import java.util.List;

public class LoginResponse {

    private String token;
    private UserInfo userInfo;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UserInfo getUserInfo() { return userInfo; }
    public void setUserInfo(UserInfo userInfo) { this.userInfo = userInfo; }

    public static class UserInfo {
        private Long userId;
        private String userName;
        private String oaAccount;
        private Long orgId;
        private List<String> roles;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getOaAccount() { return oaAccount; }
        public void setOaAccount(String oaAccount) { this.oaAccount = oaAccount; }
        public Long getOrgId() { return orgId; }
        public void setOrgId(Long orgId) { this.orgId = orgId; }
        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }
    }
}
