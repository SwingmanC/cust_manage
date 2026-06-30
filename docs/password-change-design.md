# 密码修改功能设计

> 编写日期：2026-06-25
> 范围：已登录用户自助修改本人登录密码（本地账号体系）

---

## 1. 背景与现状

- 系统本地账号密码加密方式为 **`MD5(盐 + 明文)`**，盐值常量 `cust_manage_salt`，逻辑集中在 `LocalAuthStrategy`（`md5()` 为类内静态方法、`PASSWORD_SALT` 为 private 常量）。
- 用户表 `t_yqyc_sys_user` 存 `password_hash`、`password_salt` 两列；登录时 `LocalAuthStrategy.authenticate(account, password)` 用 `md5(salt+password)` 与 `password_hash` 比对。
- 当前**没有**修改密码的接口与页面；初始密码由数据同步脚本统一设为 `123456`（见 `data_sync/`）。
- 鉴权链路：`AuthInterceptor`（JWT）→ `RequestContext`（ThreadLocal 当前用户），`/api/auth/login`、`/api/auth/oauth`、`/api/health` 在白名单内，其余 `/api/**` 必须带 Token。

本功能在不动现有加密体系的前提下，补齐"自助改密"。

---

## 2. 需求

1. 已登录用户可在右上角个人区入口修改**本人**密码。
2. 必须校验原密码；新密码需满足基本强度；两次输入一致。
3. 改密成功后**当前会话保持有效**（JWT 不含密码，无需强制重登；建议提示用户重新登录）。
4. 仅能改自己的密码，不通过 URL 传 userId，防止越权改他人密码。

---

## 3. 接口设计

### 3.1 修改密码

```
POST /api/auth/change-password
Header: Authorization: Bearer <token>
Body:
{
  "oldPassword": "123456",
  "newPassword": "Aa123456"
}
```

- **鉴权**：不在 `AuthInterceptor` 白名单中 → 必须登录；不加 `@RequirePermission`（任何已登录用户都可改本人密码）。
- **响应**：`ApiResponse<Void>`，成功 `code=0`；失败用 `BusinessException`（HTTP 200 + 业务码）。

| 场景 | 业务码 | message |
|---|---|---|
| 原密码错误 | 40002 | 原密码错误 |
| 新密码不合规（长度/强度） | 40003 | 新密码不合规 |
| 新旧密码相同 | 40004 | 新密码不能与原密码相同 |
| 用户不存在（极少，Token 有效但用户被删） | 40004 | 用户不存在 |

> 前端"确认新密码"的两次一致校验只在前端做，不进后端。

---

## 4. 后端设计

### 4.1 抽出密码工具（小重构）

当前加盐 + MD5 逻辑封在 `LocalAuthStrategy` 内（private）。改密也要用同一套。为避免重复并保证口径一致，新增一个轻量工具组件：

`com.custmanage.server.auth.PasswordCodec`（`@Component`）
```java
private static final String SALT = "cust_manage_salt";
public String hash(String plain) { return md5(SALT + plain); }
public boolean matches(String plain, String storedHash) {
    return storedHash != null && hash(plain).equalsIgnoreCase(storedHash);
}
// md5(...) 沿用 LocalAuthStrategy 现有实现
```
- `LocalAuthStrategy` 改为注入 `PasswordCodec`，删除其内部 `PASSWORD_SALT`/`md5`（鉴权口径不变，回归不受影响）。
- 数据同步脚本（`OrgPermissionSync`、`GroupDataSync` 之外的 `data_sync/`）仍各自硬编码盐值，**不动**（与 DB 历史数据保持一致）。

> 备选（更小改动）：把 `LocalAuthStrategy.md5` 与盐值提到 `public`/包级常量，`AuthServiceImpl` 直接复用。但抽 `PasswordCodec` 更清晰，推荐。

### 4.2 DTO

`com.custmanage.server.dto.ChangePasswordRequest`
```java
public record ChangePasswordRequest(
    @NotBlank String oldPassword,
    @NotBlank @Size(min = 6, max = 32) String newPassword
) {}
```
- 用 `@Valid` 触发基础校验（非空、长度）；更细的"新旧不同"在 Service 里判断。

### 4.3 Service

`IAuthService.changePassword(String oldPassword, String newPassword)`；`AuthServiceImpl` 实现：
1. `Long userId = RequestContext.currentUserId();`（来自 JWT，不可伪造他人）
2. `LoginUser user = userMapper.selectById(userId);` —— 取当前 `password_hash`；空则抛 40004。
3. `if (!passwordCodec.matches(oldPassword, user.getPasswordHash())) throw new BusinessException(40002, "原密码错误");`
4. 校验新密码：`newPassword.equals(oldPassword)` → 40004；长度/强度不足 → 40003。
5. `String newHash = passwordCodec.hash(newPassword);`
6. `userMapper.updatePassword(userId, newHash);`
7. （可选）写登录日志/审计：`loginLogMapper.insert(userId, account, ip, "改密", null)`。

### 4.4 Mapper

`UserMapper` 新增：
```java
int updatePassword(@Param("userId") Long userId, @Param("passwordHash") String passwordHash);
```
`UserMapper.xml`：
```xml
<update id="updatePassword">
    UPDATE szzw.t_yqyc_sys_user
    SET password_hash = #{passwordHash}, updated_time = SYSDATE
    WHERE id = #{userId}
</update>
```
（Oracle 模式，`SYSDATE`；只更 `password_hash` 与 `updated_time`。）

### 4.5 Controller

`AuthController` 新增：
```java
@PostMapping("/change-password")
public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
    authService.changePassword(req.oldPassword(), req.newPassword());
    return ApiResponse.ok(null);
}
```

---

## 5. 前端设计（`cust_manage_web`）

### 5.1 入口
`MainLayout.vue` 右上角 `topbar-right`：在"用户名 · 角色 · 退出"旁加 **「修改密码」**（`el-button text`，或把个人区改成 `el-dropdown`：修改密码 / 退出登录）。

### 5.2 弹窗
`el-dialog`「修改密码」：三个 `el-input type="password"`：
- 原密码（必填）
- 新密码（必填，≥6 位；可加"字母+数字"强度提示）
- 确认新密码（必填，=== 新密码）

前端 `formRules`：非空 + 长度 + 自定义校验器「确认 === 新密码」。提交调 `changePassword`。

### 5.3 API
`src/api/auth.js` 新增：
```js
export function changePassword(oldPassword, newPassword) {
  return request.post('/auth/change-password', { oldPassword, newPassword })
}
```

### 5.4 成功后行为
- `ElMessage.success('密码已修改，下次登录请使用新密码')`，关闭弹窗。
- 不强制登出（当前 Token 仍有效）；如需更安全，可在成功后调 `userStore.logoutAction()` 引导重登——默认**不强制**，作为可选项。

---

## 6. 校验与边界

| 项 | 规则 | 位置 |
|---|---|---|
| 原密码必填 | 非空 | 前端 + DTO `@NotBlank` |
| 原密码正确 | `matches(old, hash)` | Service（40002） |
| 新密码长度 | 6–32 | DTO `@Size` + 前端 |
| 新旧不同 | `!new.equals(old)` | Service（40004）+ 前端提示 |
| 两次一致 | 确认 === 新 | 仅前端 |
| 越权 | 只用 `RequestContext.currentUserId()`，不接受 userId 参数 | Controller/Service |

---

## 7. 安全说明

- **沿用 MD5+盐**：与存量数据一致，本次不升级算法；建议后续整体迁到 BCrypt/Argon2（单独立项，需同步迁移历史 hash）。
- **防爆破**：原密码错误统一返回 40002，不区分"用户不存在"；如需更强，可在 Service 里对改密失败计数+短时锁定（本版暂不做）。
- **审计**：建议改密成功写一条登录日志/操作日志（`loginLogMapper` 或后续审计表）。
- **盐值单一**：全局固定盐，非每用户随机盐——属历史设计；迁移到 BCrypt 时一并改为每用户盐。

---

## 8. 涉及文件

**后端（cust_manage）**
- 新增：`auth/PasswordCodec.java`、`dto/ChangePasswordRequest.java`
- 修改：`controller/AuthController.java`（+`/change-password`）
- 修改：`service/IAuthService.java`、`service/impl/AuthServiceImpl.java`（+`changePassword`）
- 修改：`mapper/UserMapper.java`、`mapper/UserMapper.xml`（+`updatePassword`）
- 修改：`service/impl/LocalAuthStrategy.java`（改用 `PasswordCodec`，去除内部盐/md5）

**前端（cust_manage_web）**
- 修改：`api/auth.js`（+`changePassword`）
- 修改：`layouts/MainLayout.vue`（入口 + 改密弹窗，或抽 `components/ChangePasswordDialog.vue`）

---

## 9. 测试要点

- 原密码正确 → 改成功，DB `password_hash` 更新；用新密码可登录、旧密码不可。
- 原密码错误 → 40002，hash 不变。
- 新密码 <6 位 / 与原密码相同 → 40003 / 40004。
- 未带 Token 调 `/change-password` → 401。
- A 的 Token 不能改 B 的密码（接口不接收 userId）。
- 前端：两次新密码不一致时禁止提交；成功提示并关窗。

---

## 10. 不在本次范围

- 密码算法升级（MD5 → BCrypt）。
- 忘记密码 / 邮箱或短信重置流程。
- 改密失败次数锁定。
- 管理员重置他人密码（可在后续"用户管理"页扩展 `UserMapper.resetPassword`）。
