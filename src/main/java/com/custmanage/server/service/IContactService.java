package com.custmanage.server.service;

import com.custmanage.server.common.PageResponse;
import com.custmanage.server.vo.ContactVO;

import java.util.List;
import java.util.Map;

public interface IContactService {

    /** 分页查询通讯录（含数据权限过滤） */
    PageResponse<ContactVO> queryContacts(Long groupId, String tab, String keyword, int pageNum, int pageSize);

    /** 查询单条 */
    ContactVO getContact(Long id);

    /** 新增通讯录（自动判断是否生成工单） */
    Map<String, Object> createContact(Map<String, Object> params);

    /** 编辑通讯录 */
    Map<String, Object> updateContact(Long id, Map<String, Object> params);

    /** 删除通讯录 */
    Map<String, Object> deleteContact(Long id);

    /** 历史变更 */
    List<Map<String, Object>> getHistory(Long contactId);
}
