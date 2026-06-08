package com.custmanage.server.service.impl;

import com.custmanage.server.auth.DataScopeContext;
import com.custmanage.server.auth.context.LoginUser;
import com.custmanage.server.common.BusinessException;
import com.custmanage.server.config.JwtConfig;
import com.custmanage.server.mapper.*;
import com.custmanage.server.service.IAuthService;
import com.custmanage.server.service.IDataScopeService;
import com.custmanage.server.service.AuthStrategy;
import com.custmanage.server.vo.LoginResponse;
import com.custmanage.server.vo.MenuTreeVO;
import com.custmanage.server.vo.PermissionLoadResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AuthServiceImpl implements IAuthService {

    private final JwtConfig jwtConfig;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final LoginLogMapper loginLogMapper;
    private final IDataScopeService dataScopeService;
    private final List<AuthStrategy> strategies;

    public AuthServiceImpl(JwtConfig jwtConfig,
                           UserMapper userMapper,
                           RoleMapper roleMapper,
                           MenuMapper menuMapper,
                           LoginLogMapper loginLogMapper,
                           IDataScopeService dataScopeService,
                           List<AuthStrategy> strategies) {
        this.jwtConfig = jwtConfig;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
        this.loginLogMapper = loginLogMapper;
        this.dataScopeService = dataScopeService;
        this.strategies = strategies;
    }

    @Override
    public LoginResponse login(String account, String password, String ip) {
        AuthStrategy strategy = strategies.stream()
                .filter(s -> s.supportedType() == AuthStrategy.AuthType.LOCAL)
                .findFirst()
                .orElseThrow(() -> new BusinessException(50000, "无可用认证策略"));

        LoginUser user;
        try {
            user = strategy.authenticate(account, password);
        } catch (BusinessException e) {
            loginLogMapper.insert(null, account, ip, "失败", e.getMessage());
            throw e;
        }

        LoginUser fullUser = userMapper.selectByOaAccount(account);
        List<String> roles = roleMapper.selectRoleCodesByUserId(fullUser.getUserId());
        fullUser.setRoles(roles);

        String token = generateToken(fullUser);
        loginLogMapper.insert(fullUser.getUserId(), account, ip, "成功", null);

        return buildLoginResponse(fullUser, token);
    }

    @Override
    public String generateToken(LoginUser user) {
        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.getExpiration());

        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim("userName", user.getUserName())
                .claim("oaAccount", user.getOaAccount())
                .claim("orgId", user.getOrgId())
                .claim("roles", user.getRoles())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    @Override
    public Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public PermissionLoadResponse loadPermissions(Long userId) {
        LoginUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(40004, "用户不存在");
        }

        List<String> roleCodes = roleMapper.selectRoleCodesByUserId(userId);
        user.setRoles(roleCodes);

        List<MenuMapper.MenuEntity> menuEntities = menuMapper.selectMenusByRoleCodes(roleCodes);
        List<MenuTreeVO> menuTree = buildMenuTree(menuEntities);

        List<String> permissions = menuMapper.selectPermissionCodesByRoleCodes(roleCodes);

        DataScopeContext scopeContext = dataScopeService.resolveDataScope(userId, user.getOrgId(), roleCodes);

        PermissionLoadResponse response = new PermissionLoadResponse();
        response.setMenuTree(menuTree);
        response.setPermissions(permissions);

        PermissionLoadResponse.DataScopeInfo scopeInfo = new PermissionLoadResponse.DataScopeInfo();
        scopeInfo.setScopeType(scopeContext.getScopeType());
        scopeInfo.setUserOrgId(scopeContext.getUserOrgId());
        scopeInfo.setDeptOrgIds(scopeContext.getDeptOrgIds());
        response.setDataScope(scopeInfo);

        return response;
    }

    private LoginResponse buildLoginResponse(LoginUser user, String token) {
        LoginResponse.UserInfo info = new LoginResponse.UserInfo();
        info.setUserId(user.getUserId());
        info.setUserName(user.getUserName());
        info.setOaAccount(user.getOaAccount());
        info.setOrgId(user.getOrgId());
        info.setRoles(user.getRoles());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserInfo(info);
        return response;
    }

    private List<MenuTreeVO> buildMenuTree(List<MenuMapper.MenuEntity> entities) {
        Map<Long, MenuTreeVO> nodeMap = new HashMap<>();
        for (MenuMapper.MenuEntity e : entities) {
            MenuTreeVO vo = new MenuTreeVO();
            vo.setId(e.getId());
            vo.setParentId(e.getParentId());
            vo.setMenuName(e.getMenuName());
            vo.setMenuType(e.getMenuType());
            vo.setRoutePath(e.getRoutePath());
            vo.setComponentPath(e.getComponentPath());
            vo.setPermissionCode(e.getPermissionCode());
            vo.setSortNo(e.getSortNo());
            vo.setChildren(new ArrayList<>());
            nodeMap.put(e.getId(), vo);
        }

        List<MenuTreeVO> roots = new ArrayList<>();
        for (MenuMapper.MenuEntity e : entities) {
            MenuTreeVO vo = nodeMap.get(e.getId());
            if (e.getParentId() == null || !nodeMap.containsKey(e.getParentId())) {
                roots.add(vo);
            } else {
                nodeMap.get(e.getParentId()).getChildren().add(vo);
            }
        }
        return roots;
    }
}
