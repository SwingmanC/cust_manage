package com.custmanage.server.service.impl;
import com.custmanage.server.common.MapUtil;

import com.custmanage.server.auth.DataScopeContext;
import com.custmanage.server.auth.context.RequestContext;
import com.custmanage.server.common.BusinessException;
import com.custmanage.server.common.PageResponse;
import com.custmanage.server.mapper.*;
import com.custmanage.server.service.ICompetitorService;
import com.custmanage.server.service.IDataScopeService;
import com.custmanage.server.vo.CompetitorVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CompetitorServiceImpl implements ICompetitorService {

    private final CompetitorMapper competitorMapper;
    private final CompetitorSnapshotMapper snapshotMapper;
    private final TicketMapper ticketMapper;
    private final TicketApprovalMapper approvalMapper;
    private final GroupMapper groupMapper;
    private final IDataScopeService dataScopeService;
    private static final AtomicLong ID_SEQ = new AtomicLong(System.currentTimeMillis() + 50000);

    public CompetitorServiceImpl(CompetitorMapper competitorMapper,
                                  CompetitorSnapshotMapper snapshotMapper,
                                  TicketMapper ticketMapper,
                                  TicketApprovalMapper approvalMapper,
                                  GroupMapper groupMapper,
                                  IDataScopeService dataScopeService) {
        this.competitorMapper = competitorMapper;
        this.snapshotMapper = snapshotMapper;
        this.ticketMapper = ticketMapper;
        this.approvalMapper = approvalMapper;
        this.groupMapper = groupMapper;
        this.dataScopeService = dataScopeService;
    }

    @Override
    public PageResponse<CompetitorVO> queryList(Long groupId, String tab, String keyword,
                                                 int pageNum, int pageSize) {
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
            status = "有效";
        }

        long total = competitorMapper.countList(groupIds, status, statusList);
        List<CompetitorVO> all = competitorMapper.selectList(groupIds, status, statusList, keyword);
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, all.size());
        List<CompetitorVO> page = from < all.size() ? all.subList(from, to) : Arrays.asList();
        return new PageResponse<>(total, page);
    }

    @Override
    public CompetitorVO getDetail(Long id) {
        CompetitorVO c = competitorMapper.selectById(id);
        if (c == null) throw new BusinessException(404, "竞对记录不存在");
        return c;
    }

    @Override
    @Transactional
    public Map<String, Object> create(Map<String, Object> params) {
        Long userId = RequestContext.currentUserId();
        boolean isBuLeader = isBuLeader();
        String status = isBuLeader ? "有效" : "待审核";
        long id = nextId();
        params.put("id", id);
        params.put("status", status);
        params.put("createdBy", userId);
        params.put("updatedBy", userId);
        competitorMapper.insert(params);

        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("status", status);
        if (!isBuLeader) {
            Long ticketId = createTicket(toLong(params.get("groupId")), id, "新增", userId);
            createSnapshot(ticketId, id, toLong(params.get("groupId")), "新增", null, params);
            submitTicket(ticketId, userId);
            result.put("ticketId", ticketId);
        } else {
            // BU/网格长：直接生效，仅记录历史变更快照（无审核工单，ticket_id 为空）
            createSnapshot(null, id, toLong(params.get("groupId")), "新增", null, params);
            result.put("ticketId", -1);
        }
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> update(Long id, Map<String, Object> params) {
        CompetitorVO old = competitorMapper.selectById(id);
        if (old == null) throw new BusinessException(404, "竞对记录不存在");

        Long userId = RequestContext.currentUserId();
        if (isBuLeader()) {
            params.put("id", id);
            params.put("status", "有效");
            params.put("updatedBy", userId);
            competitorMapper.update(params);
            // 记录历史变更（无审核工单，ticket_id 为空）
            createSnapshot(null, id, old.getGroupId(), "修改", old, params);
            autoCloseRejectedTicket(id, userId);
            return MapUtil.of("id", id, "status", "有效", "ticketId", -1);
        }

        params.put("id", id);
        params.put("status", "待审核");
        params.put("updatedBy", userId);
        competitorMapper.update(params);

        TicketMapper.TicketEntity existing = ticketMapper.selectByBusinessId(id, null);
        Long ticketId;
        if (existing != null && "驳回".equals(existing.getTicketStatus())) {
            ticketId = existing.getId();
            ticketMapper.updateStatus(ticketId, "待审核", null);
            createSnapshot(ticketId, id, old.getGroupId(), "修改", old, params);
            resubmitTicket(ticketId, userId);
        } else {
            ticketId = createTicket(old.getGroupId(), id, "修改", userId);
            createSnapshot(ticketId, id, old.getGroupId(), "修改", old, params);
            submitTicket(ticketId, userId);
        }
        return MapUtil.of("id", id, "status", "待审核", "ticketId", ticketId);
    }

    @Override
    @Transactional
    public Map<String, Object> delete(Long id) {
        CompetitorVO old = competitorMapper.selectById(id);
        if (old == null) throw new BusinessException(404, "竞对记录不存在");

        Long userId = RequestContext.currentUserId();
        if (isBuLeader()) {
            competitorMapper.deleteLogic(id);
            // 记录历史变更（无审核工单，ticket_id 为空）
            createSnapshot(null, id, old.getGroupId(), "删除", old, null);
            autoCloseRejectedTicket(id, userId);
            return MapUtil.of("id", id, "status", "已删除", "ticketId", -1);
        }

        TicketMapper.TicketEntity existing = ticketMapper.selectByBusinessId(id, null);
        Long ticketId;
        if (existing != null && "驳回".equals(existing.getTicketStatus())) {
            ticketId = existing.getId();
            ticketMapper.updateStatus(ticketId, "待审核", null);
            createSnapshot(ticketId, id, old.getGroupId(), "删除", old, null);
            resubmitTicket(ticketId, userId);
        } else {
            ticketId = createTicket(old.getGroupId(), id, "删除", userId);
            createSnapshot(ticketId, id, old.getGroupId(), "删除", old, null);
            submitTicket(ticketId, userId);
        }
        return MapUtil.of("id", id, "status", "待审核", "ticketId", ticketId);
    }

    @Override
    public List<Map<String, Object>> getHistory(Long id) {
        return competitorMapper.selectHistory(id);
    }

    @Override
    public Map<String, Object> getTicketDetail(Long ticketId) {
        TicketMapper.TicketEntity ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) throw new BusinessException(404, "工单不存在");
        List<CompetitorSnapshotMapper.CompetitorSnapshotEntity> snapshots = snapshotMapper.selectByTicketId(ticketId);
        List<TicketApprovalMapper.TicketApprovalEntity> records = approvalMapper.selectByTicketId(ticketId);
        Map<String, Object> result = new HashMap<>();
        result.put("ticket", ticket);
        result.put("snapshots", snapshots);
        result.put("approvalRecords", records);
        return result;
    }

    @Override
    public Map<String, Object> getTicketByCompetitorId(Long competitorId) {
        TicketMapper.TicketEntity ticket = ticketMapper.selectByBusinessId(competitorId, null);
        if (ticket == null) throw new BusinessException(404, "未找到关联工单");
        return getTicketDetail(ticket.getId());
    }

    @Override
    @Transactional
    public void approveTicket(Long ticketId, String comment) {
        TicketMapper.TicketEntity ticket = ticketMapper.selectById(ticketId);
        if (ticket == null || !"待审核".equals(ticket.getTicketStatus()))
            throw new BusinessException(40001, "工单状态已变更，无法审核");

        if ("新增".equals(ticket.getTicketType()) || "修改".equals(ticket.getTicketType())) {
            competitorMapper.updateStatus(ticket.getBusinessId(), "有效");
        } else if ("删除".equals(ticket.getTicketType())) {
            competitorMapper.deleteLogic(ticket.getBusinessId());
        }
        ticketMapper.updateStatus(ticketId, "通过", RequestContext.currentUserId());
        addApprovalRecord(ticketId, "BU审核", "通过", "待审核", "通过", comment);
    }

    @Override
    @Transactional
    public void rejectTicket(Long ticketId, String reason) {
        TicketMapper.TicketEntity ticket = ticketMapper.selectById(ticketId);
        if (ticket == null || !"待审核".equals(ticket.getTicketStatus()))
            throw new BusinessException(40001, "工单状态已变更，无法审核");

        competitorMapper.updateStatus(ticket.getBusinessId(), "驳回");
        ticketMapper.updateStatus(ticketId, "驳回", RequestContext.currentUserId());
        addApprovalRecord(ticketId, "BU审核", "驳回", "待审核", "驳回", reason);
    }

    // ---------- helpers ----------

    private Long createTicket(Long groupId, Long bizId, String type, Long submitter) {
        long id = nextId();
        TicketMapper.TicketEntity t = new TicketMapper.TicketEntity();
        t.setId(id); t.setTicketNo("WF" + System.currentTimeMillis());
        t.setBusinessId(bizId); t.setGroupId(groupId);
        t.setTicketType(type); t.setTicketStatus("草稿"); t.setSubmitterId(submitter);
        ticketMapper.insert(t);
        return id;
    }

    private void submitTicket(Long ticketId, Long submitter) {
        ticketMapper.updateStatus(ticketId, "待审核", null);
        addApprovalRecord(ticketId, "提交", "提交", "草稿", "待审核", null);
    }

    private void resubmitTicket(Long ticketId, Long submitter) {
        addApprovalRecord(ticketId, "重新提交", "重新提交", "驳回", "待审核", null);
    }

    private void createSnapshot(Long ticketId, Long recordId, Long groupId,
                                 String changeType, Object oldObj, Object newObj) {
        CompetitorSnapshotMapper.CompetitorSnapshotEntity s = new CompetitorSnapshotMapper.CompetitorSnapshotEntity();
        s.setId(nextId()); s.setTicketId(ticketId);
        s.setCompetitorRecordId(recordId); s.setGroupId(groupId);
        s.setChangeType(changeType);

        if (oldObj instanceof CompetitorVO) {
            CompetitorVO o = (CompetitorVO) oldObj;
            s.setBeforeServiceId(o.getServiceId()); s.setBeforeCompetitorName(o.getCompetitorName());
            s.setBeforeQuantity(o.getQuantity()); s.setBeforeStatMonth(o.getStatMonth());
            s.setBeforeRemark(o.getRemark());
        }
        if (newObj instanceof Map) {
            Map m = (Map) newObj;
            s.setAfterServiceId(toLong(m.get("serviceId")));
            s.setAfterCompetitorName((String) m.get("competitorName"));
            Object qty = m.get("quantity");
            s.setAfterQuantity(qty instanceof BigDecimal ? ((BigDecimal) qty) : qty != null ? new BigDecimal(qty.toString()) : null);
            s.setAfterStatMonth((String) m.get("statMonth"));
            s.setAfterRemark((String) m.get("remark"));
        }
        snapshotMapper.insert(s);
    }

    private void addApprovalRecord(Long ticketId, String node, String action,
                                    String from, String to, String comment) {
        TicketApprovalMapper.TicketApprovalEntity r = new TicketApprovalMapper.TicketApprovalEntity();
        r.setId(nextId()); r.setTicketId(ticketId); r.setNodeName(node);
        r.setApproverId(RequestContext.currentUserId()); r.setAction(action);
        r.setFromStatus(from); r.setToStatus(to); r.setApprovalComment(comment);
        approvalMapper.insert(r);
    }

    private List<Long> resolveGroupIds() {
        Long userId = RequestContext.currentUserId();
        Long orgId = RequestContext.currentOrgId();
        DataScopeContext scope = dataScopeService.resolveDataScope(userId, orgId, RequestContext.currentRoles());
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

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Long) return (Long) v;
        if (v instanceof Integer) return ((Integer) v).longValue();
        if (v instanceof String) return Long.valueOf((String) v);
        return ((Number) v).longValue();
    }

    private void autoCloseRejectedTicket(Long businessId, Long userId) {
        TicketMapper.TicketEntity ticket = ticketMapper.selectByBusinessId(businessId, null);
        if (ticket != null && "驳回".equals(ticket.getTicketStatus())) {
            ticketMapper.updateStatus(ticket.getId(), "通过", userId);
            addApprovalRecord(ticket.getId(), "BU直接编辑", "通过", "驳回", "通过", "管理员编辑后自动通过");
        }
    }

    private static long nextId() { return ID_SEQ.incrementAndGet(); }
}
