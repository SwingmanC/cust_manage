package com.custmanage.server.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ContactSnapshotMapper {

    int insert(ContactSnapshotEntity entity);

    /** 查询工单关联的通讯录快照 */
    List<ContactSnapshotEntity> selectByTicketId(@Param("ticketId") Long ticketId);

    class ContactSnapshotEntity {
        private Long id;
        private Long ticketId;
        private Long contactId;
        private Long groupId;
        private String changeType;
        private String beforeContactName;
        private String afterContactName;
        private String beforeContactPhone;
        private String afterContactPhone;
        private String beforeDepartmentName;
        private String afterDepartmentName;
        private String beforePositionName;
        private String afterPositionName;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getTicketId() { return ticketId; }
        public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
        public Long getContactId() { return contactId; }
        public void setContactId(Long contactId) { this.contactId = contactId; }
        public Long getGroupId() { return groupId; }
        public void setGroupId(Long groupId) { this.groupId = groupId; }
        public String getChangeType() { return changeType; }
        public void setChangeType(String changeType) { this.changeType = changeType; }
        public String getBeforeContactName() { return beforeContactName; }
        public void setBeforeContactName(String v) { this.beforeContactName = v; }
        public String getAfterContactName() { return afterContactName; }
        public void setAfterContactName(String v) { this.afterContactName = v; }
        public String getBeforeContactPhone() { return beforeContactPhone; }
        public void setBeforeContactPhone(String v) { this.beforeContactPhone = v; }
        public String getAfterContactPhone() { return afterContactPhone; }
        public void setAfterContactPhone(String v) { this.afterContactPhone = v; }
        public String getBeforeDepartmentName() { return beforeDepartmentName; }
        public void setBeforeDepartmentName(String v) { this.beforeDepartmentName = v; }
        public String getAfterDepartmentName() { return afterDepartmentName; }
        public void setAfterDepartmentName(String v) { this.afterDepartmentName = v; }
        public String getBeforePositionName() { return beforePositionName; }
        public void setBeforePositionName(String v) { this.beforePositionName = v; }
        public String getAfterPositionName() { return afterPositionName; }
        public void setAfterPositionName(String v) { this.afterPositionName = v; }
    }
}
