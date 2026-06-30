package com.custmanage.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 修改密码请求。当前登录用户自助修改本人密码。
 * 「确认新密码」的一致性校验只在前端做，不进后端。
 */
public class ChangePasswordRequest {

    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "新密码长度需在 6-32 位之间")
    private String newPassword;

    public ChangePasswordRequest() {
    }

    public ChangePasswordRequest(@JsonProperty("oldPassword") String oldPassword,
                                  @JsonProperty("newPassword") String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String oldPassword() {
        return oldPassword;
    }

    public String newPassword() {
        return newPassword;
    }
}
