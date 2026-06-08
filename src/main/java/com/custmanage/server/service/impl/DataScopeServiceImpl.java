package com.custmanage.server.service.impl;

import com.custmanage.server.auth.DataScopeContext;
import com.custmanage.server.mapper.DataScopeMapper;
import com.custmanage.server.mapper.OrgMapper;
import com.custmanage.server.mapper.RoleMapper;
import com.custmanage.server.service.IDataScopeService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DataScopeServiceImpl implements IDataScopeService {

    private final DataScopeMapper dataScopeMapper;
    private final OrgMapper orgMapper;
    private final RoleMapper roleMapper;

    public DataScopeServiceImpl(DataScopeMapper dataScopeMapper,
                                OrgMapper orgMapper,
                                RoleMapper roleMapper) {
        this.dataScopeMapper = dataScopeMapper;
        this.orgMapper = orgMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public DataScopeContext resolveDataScope(Long userId, Long userOrgId, List<String> roleCodes) {
        DataScopeContext context = new DataScopeContext();
        context.setCurrentUserId(userId);
        context.setUserOrgId(userOrgId);

        if (roleCodes == null || roleCodes.isEmpty()) {
            context.setScopeType("本人");
            return context;
        }

        List<Long> roleIds = roleMapper.selectRoleIdsByCodes(roleCodes);
        if (roleIds.isEmpty()) {
            context.setScopeType("本人");
            return context;
        }

        List<DataScopeMapper.DataScopeEntity> scopes = dataScopeMapper.selectByRoleIds(roleIds);
        if (scopes.isEmpty()) {
            context.setScopeType("本人");
            return context;
        }

        String mergedScope = mergeScopes(scopes);
        context.setScopeType(mergedScope);

        if ("本部门".equals(mergedScope) && userOrgId != null) {
            OrgMapper.OrgEntity org = orgMapper.selectById(userOrgId);
            if (org != null) {
                String deptOrgPath = findDeptOrgPath(org);
                if (deptOrgPath != null) {
                    List<Long> deptOrgIds = orgMapper.selectOrgIdsByPathPrefix(deptOrgPath);
                    context.setDeptOrgIds(deptOrgIds);
                }
            }
        }

        return context;
    }

    private String mergeScopes(List<DataScopeMapper.DataScopeEntity> scopes) {
        boolean hasAll = false, hasDept = false, hasGrid = false;
        for (DataScopeMapper.DataScopeEntity s : scopes) {
            switch (s.getScopeType()) {
                case "全部": hasAll = true; break;
                case "本部门": hasDept = true; break;
                case "本网格": hasGrid = true; break;
            }
        }
        if (hasAll) return "全部";
        if (hasDept) return "本部门";
        if (hasGrid) return "本网格";
        return "本人";
    }

    private String findDeptOrgPath(OrgMapper.OrgEntity org) {
        OrgMapper.OrgEntity current = org;
        for (int i = 0; i < 10 && current != null; i++) {
            if ("部门".equals(current.getOrgType())) {
                return current.getOrgPath() + "/";
            }
            if (current.getParentId() != null) {
                current = orgMapper.selectById(current.getParentId());
            } else {
                break;
            }
        }
        return org.getOrgPath() + "/";
    }
}
