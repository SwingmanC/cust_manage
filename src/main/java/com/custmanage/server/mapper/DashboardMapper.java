package com.custmanage.server.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {

    // ---- 按时间统计 ----

    /** 集团总数 */
    long countTotalGroups();

    /** 当月收入完成 */
    Map<String, Object> sumMonthRevenue(@Param("month") String month);

    /** 全年累计收入 */
    Map<String, Object> sumAnnualRevenue(@Param("year") String year);

    /** 分BU收入 */
    List<Map<String, Object>> revenueByBu(@Param("month") String month);

    /** 分业务收入 */
    List<Map<String, Object>> revenueByService(@Param("month") String month);

    /** 全年收入按月趋势（今年） */
    List<Map<String, Object>> revenueTrendByMonth(@Param("year") String year);

    /** BU业绩：按业务汇总（信息化业务/BC融合区块） */
    List<Map<String, Object>> buPerfByService(@Param("month") String month);

    /** BU业绩：按BU汇总 */
    List<Map<String, Object>> buPerfByBuService(@Param("month") String month, @Param("svcCodes") List<String> svcCodes);

    // ---- 按BU统计 ----

    /** 指定BU各月收入 */
    List<Map<String, Object>> buMonthlyRevenue(@Param("buOrgId") Long buOrgId, @Param("year") String year);

    /** 指定BU各月业绩 */
    List<Map<String, Object>> buMonthlyPerf(@Param("buOrgId") Long buOrgId, @Param("serviceId") Long serviceId, @Param("year") String year);

    // ---- 通用 ----

    /** BU列表 */
    List<Map<String, Object>> selectBuOptions();

    /** 要客集团数 */
    long countKeyCustomerGroups();

    /** 统付集团数 */
    long countUnifiedPaymentGroups();
}
