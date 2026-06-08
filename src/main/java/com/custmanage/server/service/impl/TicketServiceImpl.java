package com.custmanage.server.service.impl;

import com.custmanage.server.auth.context.RequestContext;
import com.custmanage.server.common.BusinessException;
import com.custmanage.server.mapper.*;
import com.custmanage.server.service.ITicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TicketServiceImpl implements ITicketService {

    private final TicketMapper ticketMapper;
    private final TicketApprovalMapper approvalMapper;
    private final ContactSnapshotMapper snapshotMapper;
    private final ContactMapper contactMapper;

    private static final AtomicLong ID_SEQ = new AtomicLong(System.currentTimeMillis() + 10000);

    public TicketServiceImpl(TicketMapper ticketMapper,
                             TicketApprovalMapper approvalMapper,
                             ContactSnapshotMapper snapshotMapper,
                             ContactMapper contactMapper) {
        this.ticketMapper = ticketMapper;
        this.approvalMapper = approvalMapper;
        this.snapshotMapper = snapshotMapper;
        this.contactMapper = contactMapper;
    }

    @Override
    public Map<String, Object> getTicketDetail(Long ticketId) {
        TicketMapper.TicketEntity ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) throw new BusinessException(404, "工单不存在");

        List<ContactSnapshotMapper.ContactSnapshotEntity> snapshots =
                snapshotMapper.selectByTicketId(ticketId);
        List<TicketApprovalMapper.TicketApprovalEntity> records =
                approvalMapper.selectByTicketId(ticketId);

        Map<String, Object> result = new HashMap<>();
        result.put("ticket", ticket);
        result.put("snapshots", snapshots);
        result.put("approvalRecords", records);

        // 查询集团名称
        if (ticket.getGroupId() != null) {
            result.put("groupId", ticket.getGroupId());
        }

        return result;
    }

    @Override
    @Transactional
    public void approveTicket(Long ticketId, String comment) {
        TicketMapper.TicketEntity ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) throw new BusinessException(404, "工单不存在");
        if (!"待审核".equals(ticket.getTicketStatus())) {
            throw new BusinessException(40001, "工单状态已变更，无法审核");
        }

        Long approverId = RequestContext.currentUserId();

        // 审核通过 → 更新业务表
        if ("新增".equals(ticket.getTicketType()) || "修改".equals(ticket.getTicketType())) {
            contactMapper.updateStatus(ticket.getBusinessId(), "有效");
        } else if ("删除".equals(ticket.getTicketType())) {
            contactMapper.deleteLogic(ticket.getBusinessId());
        }

        // 更新工单状态
        ticketMapper.updateStatus(ticketId, "通过", approverId);

        // 记录审批
        addApprovalRecord(ticketId, "BU审核", approverId, "通过", "待审核", "通过", comment);
    }

    @Override
    @Transactional
    public void rejectTicket(Long ticketId, String reason) {
        TicketMapper.TicketEntity ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) throw new BusinessException(404, "工单不存在");
        if (!"待审核".equals(ticket.getTicketStatus())) {
            throw new BusinessException(40001, "工单状态已变更，无法审核");
        }

        Long approverId = RequestContext.currentUserId();

        // 驳回 → 通讯录状态改为驳回
        contactMapper.updateStatus(ticket.getBusinessId(), "驳回");

        // 更新工单
        ticketMapper.updateStatus(ticketId, "驳回", approverId);

        // 记录审批
        addApprovalRecord(ticketId, "BU审核", approverId, "驳回", "待审核", "驳回", reason);
    }

    @Override
    public Map<String, Object> getTicketByContactId(Long contactId) {
        // 查找该联系人最新的工单
        TicketMapper.TicketEntity ticket = ticketMapper.selectByBusinessId(contactId, null);
        if (ticket == null) throw new BusinessException(404, "未找到关联工单");
        return getTicketDetail(ticket.getId());
    }

    private void addApprovalRecord(Long ticketId, String nodeName, Long approverId,
                                    String action, String fromStatus, String toStatus,
                                    String comment) {
        var record = new TicketApprovalMapper.TicketApprovalEntity();
        record.setId(ID_SEQ.incrementAndGet());
        record.setTicketId(ticketId);
        record.setNodeName(nodeName);
        record.setApproverId(approverId);
        record.setAction(action);
        record.setFromStatus(fromStatus);
        record.setToStatus(toStatus);
        record.setApprovalComment(comment);
        approvalMapper.insert(record);
    }
}
