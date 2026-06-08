package com.custmanage.server.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TicketMapper {

    int insert(TicketEntity ticket);

    int updateStatus(@Param("ticketId") Long ticketId,
                     @Param("status") String status,
                     @Param("handlerId") Long handlerId);

    TicketEntity selectById(@Param("ticketId") Long ticketId);

    /** 按业务对象查询工单 */
    TicketEntity selectByBusinessId(@Param("businessId") Long businessId,
                                     @Param("ticketType") String ticketType);

    /** 查询流程中的工单列表（用于通讯录流程中Tab） */
    List<TicketEntity> selectPendingTickets(@Param("groupIds") List<Long> groupIds,
                                            @Param("ticketType") String ticketType);

    class TicketEntity {
        private Long id;
        private String ticketNo;
        private Long businessId;
        private Long groupId;
        private String ticketType;
        private String ticketStatus;
        private Long submitterId;
        private Long currentHandlerId;
        private String submitTime;
        private String finishTime;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTicketNo() { return ticketNo; }
        public void setTicketNo(String ticketNo) { this.ticketNo = ticketNo; }
        public Long getBusinessId() { return businessId; }
        public void setBusinessId(Long businessId) { this.businessId = businessId; }
        public Long getGroupId() { return groupId; }
        public void setGroupId(Long groupId) { this.groupId = groupId; }
        public String getTicketType() { return ticketType; }
        public void setTicketType(String ticketType) { this.ticketType = ticketType; }
        public String getTicketStatus() { return ticketStatus; }
        public void setTicketStatus(String ticketStatus) { this.ticketStatus = ticketStatus; }
        public Long getSubmitterId() { return submitterId; }
        public void setSubmitterId(Long submitterId) { this.submitterId = submitterId; }
        public Long getCurrentHandlerId() { return currentHandlerId; }
        public void setCurrentHandlerId(Long currentHandlerId) { this.currentHandlerId = currentHandlerId; }
        public String getSubmitTime() { return submitTime; }
        public void setSubmitTime(String submitTime) { this.submitTime = submitTime; }
        public String getFinishTime() { return finishTime; }
        public void setFinishTime(String finishTime) { this.finishTime = finishTime; }
    }
}
