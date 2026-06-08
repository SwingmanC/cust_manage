package com.custmanage.server.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper {

    /** 查询角色拥有的菜单列表 */
    List<MenuEntity> selectMenusByRoleCodes(@Param("roleCodes") List<String> roleCodes);

    /** 查询角色拥有的权限标识列表（按钮 + 接口类型） */
    List<String> selectPermissionCodesByRoleCodes(@Param("roleCodes") List<String> roleCodes);

    /** 菜单实体 */
    class MenuEntity {
        private Long id;
        private Long parentId;
        private String menuName;
        private String menuType;
        private String routePath;
        private String componentPath;
        private String permissionCode;
        private Integer sortNo;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        public String getMenuName() { return menuName; }
        public void setMenuName(String menuName) { this.menuName = menuName; }
        public String getMenuType() { return menuType; }
        public void setMenuType(String menuType) { this.menuType = menuType; }
        public String getRoutePath() { return routePath; }
        public void setRoutePath(String routePath) { this.routePath = routePath; }
        public String getComponentPath() { return componentPath; }
        public void setComponentPath(String componentPath) { this.componentPath = componentPath; }
        public String getPermissionCode() { return permissionCode; }
        public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
        public Integer getSortNo() { return sortNo; }
        public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    }
}
