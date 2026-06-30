package com.custmanage.server.service.impl;
import com.custmanage.server.common.MapUtil;

import com.custmanage.server.auth.DataScopeContext;
import com.custmanage.server.auth.context.RequestContext;
import com.custmanage.server.common.BusinessException;
import com.custmanage.server.common.PageResponse;
import com.custmanage.server.mapper.*;
import com.custmanage.server.service.IContactService;
import com.custmanage.server.service.IDataScopeService;
import com.custmanage.server.vo.ContactVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ContactServiceImpl implements IContactService {

    private final ContactMapper contactMapper;
    private final TicketMapper ticketMapper;
    private final ContactSnapshotMapper snapshotMapper;
    private final TicketApprovalMapper approvalMapper;
    private final GroupMapper groupMapper;
    private final IDataScopeService dataScopeService;

    private static final AtomicLong ID_SEQ = new AtomicLong(System.currentTimeMillis());

    public ContactServiceImpl(ContactMapper contactMapper,
                              TicketMapper ticketMapper,
                              ContactSnapshotMapper snapshotMapper,
                              TicketApprovalMapper approvalMapper,
                              GroupMapper groupMapper,
                              IDataScopeService dataScopeService) {
        this.contactMapper = contactMapper;
        this.ticketMapper = ticketMapper;
        this.snapshotMapper = snapshotMapper;
        this.approvalMapper = approvalMapper;
        this.groupMapper = groupMapper;
        this.dataScopeService = dataScopeService;
    }

    @Override
    public PageResponse<ContactVO> queryContacts(Long groupId, String tab, String keyword,
                                                  int pageNum, int pageSize) {
        // 数据权限
        List<Long> groupIds;
        if(groupId == null){
            groupIds = resolveGroupIds();
        }
        else{
            groupIds = new ArrayList<>();
            groupIds.add(groupId);
        }

        List<String> statusList = null;
        String status = null;
        if ("pending".equals(tab)) {
            statusList = Arrays.asList("待审核", "驳回");
        } else {
            status = "有效";  // active tab
        }

        long total = contactMapper.countContacts(groupIds, status, statusList);
        int offset = (pageNum - 1) * pageSize;
        // Use a simple approach: select all and sublist in memory for simplicity
        List<ContactVO> all = contactMapper.selectContacts(groupIds, status, statusList, keyword);
        int toIndex = Math.min(offset + pageSize, all.size());
        List<ContactVO> page = offset < all.size() ? all.subList(offset, toIndex) : Arrays.asList();
        return new PageResponse<>(total, page);
    }

    @Override
    public ContactVO getContact(Long id) {
        ContactVO contact = contactMapper.selectById(id);
        if (contact == null) {
            throw new BusinessException(404, "通讯录记录不存在");
        }
        return contact;
    }

    @Override
    @Transactional
    public Map<String, Object> createContact(Map<String, Object> params) {
        Long userId = RequestContext.currentUserId();
        boolean isBuLeader = isBuLeader();

        String status = isBuLeader ? "有效" : "待审核";
        String sourceType = "手工";

        long id = nextId();
        params.put("id", id);
        params.put("status", status);
        params.put("sourceType", sourceType);
        params.put("createdBy", userId);
        params.put("updatedBy", userId);
        contactMapper.insert(params);

        if (!isBuLeader) {
            // 客户经理：生成工单
            Long groupId = toLong(params.get("groupId"));
            Long ticketId = createTicket(groupId, id, "新增", userId);
            // 创建快照（新增：只有 after_* 字段）
            createSnapshot(ticketId, id, groupId, "新增", null, params);
            // 提交工单
            submitTicket(ticketId, userId);
        } else {
            // BU/网格长：直接生效，仅记录历史变更快照（无审核工单，ticket_id 为空）
            createSnapshot(null, id, toLong(params.get("groupId")), "新增", null, params);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("status", status);
        result.put("ticketId", isBuLeader ? null : ticketMapper.selectByBusinessId(id, "新增").getId());
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> updateContact(Long contactId, Map<String, Object> params) {
        ContactVO old = contactMapper.selectById(contactId);
        if (old == null) throw new BusinessException(404, "通讯录记录不存在");

        Long userId = RequestContext.currentUserId();
        boolean isBuLeader = isBuLeader();

        if (isBuLeader) {
            // BU长直接更新
            params.put("id", contactId);
            params.put("status", "有效");
            params.put("updatedBy", userId);
            contactMapper.update(params);
            // 记录历史变更（无审核工单，ticket_id 为空）
            createSnapshot(null, contactId, old.getGroupId(), "修改", old, params);
            // 若存在被驳回的工单，自动通过
            autoCloseRejectedTicket(contactId, userId);
            return MapUtil.of("id", contactId, "status", "有效", "ticketId", -1);
        }

        // 客户经理：更新状态为待审核
        params.put("id", contactId);
        params.put("status", "待审核");
        params.put("updatedBy", userId);
        contactMapper.update(params);

        // 检查是否存在被驳回的工单，存在则复用
        TicketMapper.TicketEntity existingTicket = ticketMapper.selectByBusinessId(contactId, null);
        Long ticketId;
        if (existingTicket != null && "驳回".equals(existingTicket.getTicketStatus())) {
            // 复用原工单：更新状态、追加快照、记录重新提交
            ticketId = existingTicket.getId();
            ticketMapper.updateStatus(ticketId, "待审核", null);
            createSnapshot(ticketId, contactId, old.getGroupId(), "修改", old, params);
            resubmitTicket(ticketId, userId);
        } else {
            // 新建工单
            ticketId = createTicket(old.getGroupId(), contactId, "修改", userId);
            createSnapshot(ticketId, contactId, old.getGroupId(), "修改", old, params);
            submitTicket(ticketId, userId);
        }

        return MapUtil.of("id", contactId, "status", "待审核",
                "ticketId", ticketId);
    }

    @Override
    @Transactional
    public Map<String, Object> deleteContact(Long contactId) {
        ContactVO old = contactMapper.selectById(contactId);
        if (old == null) throw new BusinessException(404, "通讯录记录不存在");

        Long userId = RequestContext.currentUserId();
        boolean isBuLeader = isBuLeader();

        if (isBuLeader) {
            contactMapper.deleteLogic(contactId);
            // 记录历史变更（无审核工单，ticket_id 为空）
            createSnapshot(null, contactId, old.getGroupId(), "删除", old, MapUtil.of());
            autoCloseRejectedTicket(contactId, userId);
            return MapUtil.of("id", contactId, "status", "已删除", "ticketId", -1);
        }

        // 客户经理：生成或复用删除工单
        TicketMapper.TicketEntity existingTicket = ticketMapper.selectByBusinessId(contactId, null);
        Long ticketId;
        Map<String, Object> emptyNew = MapUtil.of();
        if (existingTicket != null && "驳回".equals(existingTicket.getTicketStatus())) {
            ticketId = existingTicket.getId();
            ticketMapper.updateStatus(ticketId, "待审核", null);
            createSnapshot(ticketId, contactId, old.getGroupId(), "删除", old, emptyNew);
            resubmitTicket(ticketId, userId);
        } else {
            ticketId = createTicket(old.getGroupId(), contactId, "删除", userId);
            createSnapshot(ticketId, contactId, old.getGroupId(), "删除", old, emptyNew);
            submitTicket(ticketId, userId);
        }

        return MapUtil.of("id", contactId, "status", "待审核",
                "ticketId", ticketId);
    }

    @Override
    public List<Map<String, Object>> getHistory(Long contactId) {
        return contactMapper.selectHistory(contactId);
    }

    // ---------- 工单相关 ----------

    private Long createTicket(Long groupId, Long contactId, String type, Long submitterId) {
        long ticketId = nextId();
        String ticketNo = "WF" + System.currentTimeMillis();
        TicketMapper.TicketEntity ticket = new TicketMapper.TicketEntity();
        ticket.setId(ticketId);
        ticket.setTicketNo(ticketNo);
        ticket.setBusinessId(contactId);
        ticket.setGroupId(groupId);
        ticket.setTicketType(type);
        ticket.setTicketStatus("草稿");
        ticket.setSubmitterId(submitterId);
        ticketMapper.insert(ticket);
        return ticketId;
    }

    private void submitTicket(Long ticketId, Long submitterId) {
        ticketMapper.updateStatus(ticketId, "待审核", null);
        addApprovalRecord(ticketId, "提交", submitterId, "提交", "草稿", "待审核", null);
    }

    private void resubmitTicket(Long ticketId, Long submitterId) {
        addApprovalRecord(ticketId, "重新提交", submitterId, "重新提交", "驳回", "待审核", null);
    }

    private void addApprovalRecord(Long ticketId, String nodeName, Long approverId,
                                    String action, String fromStatus, String toStatus, String comment) {
        TicketApprovalMapper.TicketApprovalEntity record = new TicketApprovalMapper.TicketApprovalEntity();
        record.setId(nextId());
        record.setTicketId(ticketId);
        record.setNodeName(nodeName);
        record.setApproverId(approverId);
        record.setAction(action);
        record.setFromStatus(fromStatus);
        record.setToStatus(toStatus);
        record.setApprovalComment(comment);
        approvalMapper.insert(record);
    }

    private void createSnapshot(Long ticketId, Long contactId, Long groupId,
                                 String changeType, Object oldObj, Object newObj) {
        ContactSnapshotMapper.ContactSnapshotEntity snap = new ContactSnapshotMapper.ContactSnapshotEntity();
        snap.setId(nextId());
        snap.setTicketId(ticketId);
        snap.setContactId(contactId);
        snap.setGroupId(groupId);
        snap.setChangeType(changeType);

        if (oldObj instanceof ContactVO) {
            ContactVO old = (ContactVO) oldObj;
            snap.setBeforeContactName(old.getContactName());
            snap.setBeforeContactPhone(old.getContactPhone());
            snap.setBeforeDepartmentName(old.getDepartmentName());
            snap.setBeforePositionName(old.getPositionName());
        } else if (oldObj instanceof Map) {
            Map oldMap = (Map) oldObj;
            snap.setBeforeContactName((String) oldMap.get("contactName"));
            snap.setBeforeContactPhone((String) oldMap.get("contactPhone"));
            snap.setBeforeDepartmentName((String) oldMap.get("departmentName"));
            snap.setBeforePositionName((String) oldMap.get("positionName"));
        }

        if (newObj instanceof Map) {
            Map newMap = (Map) newObj;
            snap.setAfterContactName((String) newMap.get("contactName"));
            snap.setAfterContactPhone((String) newMap.get("contactPhone"));
            snap.setAfterDepartmentName((String) newMap.get("departmentName"));
            snap.setAfterPositionName((String) newMap.get("positionName"));
        }

        snapshotMapper.insert(snap);
    }

    // ---------- 工具方法 ----------

    private List<Long> resolveGroupIds() {
        Long userId = RequestContext.currentUserId();
        Long orgId = RequestContext.currentOrgId();
        List<String> roles = RequestContext.currentRoles();
        DataScopeContext scope = dataScopeService.resolveDataScope(userId, orgId, roles);

        switch (scope.getScopeType()) {
            case "全部": return null;
            case "本部门": return scope.getDeptOrgIds();
            case "本网格": return groupMapper.selectGroupIdsByOrgId(orgId);
            default: {
                List<Long> ids = groupMapper.selectGroupIdsByManager(userId);
                return ids != null && !ids.isEmpty() ? ids : Arrays.asList(-1L);
            }
        }
    }

    private boolean isBuLeader() {
        List<String> roles = RequestContext.currentRoles();
        return roles.contains("BU_LEADER") || roles.contains("DEPT_LEADER")
                || roles.contains("COMPANY_LEADER") || roles.contains("SYSTEM_ADMIN");
    }

    private void autoCloseRejectedTicket(Long businessId, Long userId) {
        TicketMapper.TicketEntity ticket = ticketMapper.selectByBusinessId(businessId, null);
        if (ticket != null && "驳回".equals(ticket.getTicketStatus())) {
            ticketMapper.updateStatus(ticket.getId(), "通过", userId);
            addApprovalRecord(ticket.getId(), "BU直接编辑", userId, "通过", "驳回", "通过", "管理员编辑后自动通过");
        }
    }

    private static long nextId() {
        return ID_SEQ.incrementAndGet();
    }

    /** 安全获取 Map 中的 Long 值（Jackson 反序列化时 Integer→Long 兼容） */
    private static Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Long) return (Long) val;
        if (val instanceof Integer) return ((Integer) val).longValue();
        if (val instanceof String) return Long.valueOf((String) val);
        return ((Number) val).longValue();
    }
}
