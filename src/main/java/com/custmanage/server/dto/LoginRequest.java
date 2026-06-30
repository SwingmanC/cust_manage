package com.custmanage.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "账号不能为空")
    private String account;

    @NotBlank(message = "密码不能为空")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(@JsonProperty("account") String account,
                        @JsonProperty("password") String password) {
        this.account = account;
        this.password = password;
    }

    public String account() {
        return account;
    }

    public String password() {
        return password;
    }
}
