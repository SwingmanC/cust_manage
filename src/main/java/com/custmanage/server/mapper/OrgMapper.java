package com.custmanage.server.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrgMapper {

    /** 查询组织信息 */
    OrgEntity selectById(@Param("orgId") Long orgId);

    /** 查询指定 org_path 下的所有子组织 ID（用于本部门数据范围） */
    List<Long> selectOrgIdsByPathPrefix(@Param("orgPathPrefix") String orgPathPrefix);

    /** 组织实体 */
    class OrgEntity {
        private Long id;
        private String orgCode;
        private String orgName;
        private Long parentId;
        private String orgType;
        private String orgPath;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getOrgCode() { return orgCode; }
        public void setOrgCode(String orgCode) { this.orgCode = orgCode; }
        public String getOrgName() { return orgName; }
        public void setOrgName(String orgName) { this.orgName = orgName; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        public String getOrgType() { return orgType; }
        public void setOrgType(String orgType) { this.orgType = orgType; }
        public String getOrgPath() { return orgPath; }
        public void setOrgPath(String orgPath) { this.orgPath = orgPath; }
    }
}
