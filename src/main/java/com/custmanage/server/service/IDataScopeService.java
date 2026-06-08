package com.custmanage.server.service;

import com.custmanage.server.auth.DataScopeContext;

import java.util.List;

/**
 * 数据权限解析服务接口。
 */
public interface IDataScopeService {

    /**
     * 根据用户 ID、组织 ID、角色编码解析数据范围。
     */
    DataScopeContext resolveDataScope(Long userId, Long userOrgId, List<String> roleCodes);
}
