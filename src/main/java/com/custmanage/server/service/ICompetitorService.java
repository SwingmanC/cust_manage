package com.custmanage.server.service;

import com.custmanage.server.common.PageResponse;
import com.custmanage.server.vo.CompetitorVO;

import java.util.List;
import java.util.Map;

public interface ICompetitorService {

    PageResponse<CompetitorVO> queryList(Long groupId, String tab, String keyword, int pageNum, int pageSize);

    CompetitorVO getDetail(Long id);

    Map<String, Object> create(Map<String, Object> params);

    Map<String, Object> update(Long id, Map<String, Object> params);

    Map<String, Object> delete(Long id);

    List<Map<String, Object>> getHistory(Long id);

    /** 工单详情 */
    Map<String, Object> getTicketDetail(Long ticketId);

    /** 审核通过 */
    void approveTicket(Long ticketId, String comment);

    /** 审核驳回 */
    void rejectTicket(Long ticketId, String reason);

    /** 按竞对记录ID查工单 */
    Map<String, Object> getTicketByCompetitorId(Long competitorId);
}
