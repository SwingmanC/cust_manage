package com.custmanage.server.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CompetitorSnapshotMapper {

    int insert(CompetitorSnapshotEntity entity);

    List<CompetitorSnapshotEntity> selectByTicketId(@Param("ticketId") Long ticketId);

    class CompetitorSnapshotEntity {
        private Long id;
        private Long ticketId;
        private Long competitorRecordId;
        private Long groupId;
        private String changeType;
        private Long beforeServiceId, afterServiceId;
        private String beforeCompetitorName, afterCompetitorName;
        private java.math.BigDecimal beforeQuantity, afterQuantity;
        private String beforeStatMonth, afterStatMonth;
        private String beforeRemark, afterRemark;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getTicketId() { return ticketId; }
        public void setTicketId(Long v) { this.ticketId = v; }
        public Long getCompetitorRecordId() { return competitorRecordId; }
        public void setCompetitorRecordId(Long v) { this.competitorRecordId = v; }
        public Long getGroupId() { return groupId; }
        public void setGroupId(Long v) { this.groupId = v; }
        public String getChangeType() { return changeType; }
        public void setChangeType(String v) { this.changeType = v; }
        public Long getBeforeServiceId() { return beforeServiceId; }
        public void setBeforeServiceId(Long v) { this.beforeServiceId = v; }
        public Long getAfterServiceId() { return afterServiceId; }
        public void setAfterServiceId(Long v) { this.afterServiceId = v; }
        public String getBeforeCompetitorName() { return beforeCompetitorName; }
        public void setBeforeCompetitorName(String v) { this.beforeCompetitorName = v; }
        public String getAfterCompetitorName() { return afterCompetitorName; }
        public void setAfterCompetitorName(String v) { this.afterCompetitorName = v; }
        public java.math.BigDecimal getBeforeQuantity() { return beforeQuantity; }
        public void setBeforeQuantity(java.math.BigDecimal v) { this.beforeQuantity = v; }
        public java.math.BigDecimal getAfterQuantity() { return afterQuantity; }
        public void setAfterQuantity(java.math.BigDecimal v) { this.afterQuantity = v; }
        public String getBeforeStatMonth() { return beforeStatMonth; }
        public void setBeforeStatMonth(String v) { this.beforeStatMonth = v; }
        public String getAfterStatMonth() { return afterStatMonth; }
        public void setAfterStatMonth(String v) { this.afterStatMonth = v; }
        public String getBeforeRemark() { return beforeRemark; }
        public void setBeforeRemark(String v) { this.beforeRemark = v; }
        public String getAfterRemark() { return afterRemark; }
        public void setAfterRemark(String v) { this.afterRemark = v; }
    }
}
