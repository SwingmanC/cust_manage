package com.custmanage.server.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TicketApprovalMapper {

    int insert(TicketApprovalEntity entity);

    List<TicketApprovalEntity> selectByTicketId(@Param("ticketId") Long ticketId);

    class TicketApprovalEntity {
        private Long id;
        private Long ticketId;
        private String nodeName;
        private Long approverId;
        private String action;
        private String fromStatus;
        private String toStatus;
        private String approvalComment;
        private String approvalTime;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getTicketId() { return ticketId; }
        public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
        public String getNodeName() { return nodeName; }
        public void setNodeName(String nodeName) { this.nodeName = nodeName; }
        public Long getApproverId() { return approverId; }
        public void setApproverId(Long approverId) { this.approverId = approverId; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getFromStatus() { return fromStatus; }
        public void setFromStatus(String fromStatus) { this.fromStatus = fromStatus; }
        public String getToStatus() { return toStatus; }
        public void setToStatus(String toStatus) { this.toStatus = toStatus; }
        public String getApprovalComment() { return approvalComment; }
        public void setApprovalComment(String approvalComment) { this.approvalComment = approvalComment; }
        public String getApprovalTime() { return approvalTime; }
        public void setApprovalTime(String approvalTime) { this.approvalTime = approvalTime; }
    }
}
