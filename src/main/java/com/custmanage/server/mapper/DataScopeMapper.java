package com.custmanage.server.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DataScopeMapper {

    /** 查询角色绑定的数据权限 */
    List<DataScopeEntity> selectByRoleIds(@Param("roleIds") List<Long> roleIds);

    /** 数据权限实体 */
    class DataScopeEntity {
        private Long id;
        private Long roleId;
        private String scopeType;
        private Long scopeOrgId;
        private Long scopeGroupId;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getRoleId() { return roleId; }
        public void setRoleId(Long roleId) { this.roleId = roleId; }
        public String getScopeType() { return scopeType; }
        public void setScopeType(String scopeType) { this.scopeType = scopeType; }
        public Long getScopeOrgId() { return scopeOrgId; }
        public void setScopeOrgId(Long scopeOrgId) { this.scopeOrgId = scopeOrgId; }
        public Long getScopeGroupId() { return scopeGroupId; }
        public void setScopeGroupId(Long scopeGroupId) { this.scopeGroupId = scopeGroupId; }
    }
}
