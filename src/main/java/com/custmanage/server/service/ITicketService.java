package com.custmanage.server.service;

import java.util.List;
import java.util.Map;

public interface ITicketService {

    /** 工单详情（含快照 + 流转记录） */
    Map<String, Object> getTicketDetail(Long ticketId);

    /** 审核通过 */
    void approveTicket(Long ticketId, String comment);

    /** 审核驳回 */
    void rejectTicket(Long ticketId, String reason);

    /** 按通讯录ID查工单 */
    Map<String, Object> getTicketByContactId(Long contactId);
}
